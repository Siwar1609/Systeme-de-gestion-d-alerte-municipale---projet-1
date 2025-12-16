package com.example.demo.services.incidentworkflow;

import com.example.demo.models.FiltreIncident;
import com.example.demo.models.Incident;
import com.example.demo.models.Notification;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.PrioriteIncidentEnum;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.UtilisateurRepository;
import com.example.demo.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentWorkFlowService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;


    // RECHERCHE agent
    public Page<Incident> rechercherIncidentsAgent(Long agentId, FiltreIncident filtre, Pageable pageable) {
        Specification<Incident> spec = filtre.toSpecification();

        // Limiter aux incidents de cet agent
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("agent").get("id"), agentId));

        return incidentRepository.findAll(spec, pageable);
    }
    //Recherche admin
    public Page<Incident> rechercherIncidents(FiltreIncident filtre, Pageable pageable) {
        Specification<Incident> spec = filtre.toSpecification();
        return incidentRepository.findAll(spec, pageable);
    }
    //Recherche Citoyen
    public Page<Incident> rechercherIncidentsCitoyen(Long citoyenId, FiltreIncident filtre, Pageable pageable) {
        Specification<Incident> spec = filtre.toSpecification();
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("citoyen").get("id"), citoyenId));
        return incidentRepository.findAll(spec, pageable);
    }


    // ========================= ADMIN =========================


    public void assignerIncident(Long incidentId,
                                 Long agentId,
                                 PrioriteIncidentEnum priorite,
                                 Utilisateur admin) {

        if (admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            throw new RuntimeException("Accès interdit : administrateur requis");
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (incident.getStatut() != StatutIncidentEnum.SIGNALE) {
            throw new RuntimeException("Incident déjà pris en charge");
        }

        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        if (agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            throw new RuntimeException("Utilisateur sélectionné n'est pas un agent municipal");
        }

        // Vérification service ↔ catégorie
        if (agent.getService() != null && incident.getCategorie() != null) {
            if (!agent.getService().getNom().equals(incident.getCategorie().getNom())) {
                throw new RuntimeException("L'agent ne traite pas cette catégorie d'incident");
            }
        }

        incident.setAgent(agent);

        if (priorite != null) {
            incident.setPriorite(priorite);
        }

        incident.setStatut(StatutIncidentEnum.EN_COURS_DE_CHARGE);
        incidentRepository.save(incident);
        //NOTIFIER L'AGENT PAR EMAIL
        notifierAgentAssignation(incident, agent);
    }
    private void notifierAgentAssignation(Incident incident, Utilisateur agent) {
        // Notification en base
        Notification notification = new Notification();
        notification.setUtilisateur(agent);
        notification.setMessage("Nouvel incident assigné : #" + incident.getId() + " - " + incident.getTitre());
        notification.setType("ASSIGNATION");
        notification.setDate(LocalDateTime.now());
        notificationRepository.save(notification);

        // Email à l'agent
        emailService.envoyerEmailAssignationIncident(incident, agent);
    }

    public void modifierPrioriteIncident(Long incidentId,
                                         PrioriteIncidentEnum priorite,
                                         Utilisateur admin) {

        if (admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            throw new RuntimeException("Accès interdit : administrateur requis");
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        incident.setPriorite(priorite);
        incidentRepository.save(incident);
    }


    // ========================= AGENT =========================


    public List<Incident> getIncidentsAssignes(Utilisateur agent) {
        if (agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            throw new RuntimeException("Accès interdit");
        }
        return incidentRepository.findByAgentId(agent.getId());
    }

    public void commencerResolution(Long incidentId, Utilisateur agent) {

        Incident incident = verifierIncidentAgent(
                incidentId,
                agent,
                StatutIncidentEnum.EN_COURS_DE_CHARGE
        );

        incident.setStatut(StatutIncidentEnum.EN_RESOLUTION);
        incidentRepository.save(incident);

        notifierCitoyen(
                incident,
                "Votre incident est en cours de résolution",
                StatutIncidentEnum.EN_RESOLUTION
        );
    }

    public void marquerResolu(Long incidentId, Utilisateur agent) {

        Incident incident = verifierIncidentAgent(
                incidentId,
                agent,
                StatutIncidentEnum.EN_RESOLUTION
        );

        incident.setStatut(StatutIncidentEnum.RESOLU);
        incidentRepository.save(incident);

        notifierCitoyen(
                incident,
                "Votre incident a été résolu",
                StatutIncidentEnum.RESOLU
        );
    }

    // =========================================================
    // ======================== CITOYEN ========================
    // =========================================================

    public Incident getIncidentByIdAndCitoyen(Long id, Long citoyenId) {
        return incidentRepository.findByIdAndCitoyenId(id, citoyenId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));
    }
    public void cloturerIncident(Long incidentId, Utilisateur citoyen,String feedback,
                                 Integer note) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (incident.getCitoyen() == null ||
                !incident.getCitoyen().getId().equals(citoyen.getId())) {
            throw new RuntimeException("Accès interdit");
        }

        if (incident.getStatut() != StatutIncidentEnum.RESOLU) {
            throw new RuntimeException("L'incident n'est pas encore résolu");
        }

        incident.setStatut(StatutIncidentEnum.CLOTURE);

        incident.setFeedbackCitoyen(feedback);
        incident.setNoteCitoyen(note);
        incidentRepository.save(incident);
    }

    // =========================================================
    // ========================= UTILS =========================
    // =========================================================

    public List<Incident> getTousLesIncidents() {
        return incidentRepository.findAll();
    }

    public List<Utilisateur> getTousLesAgents() {
        return utilisateurRepository.findByRole(RoleEnum.AGENT_MUNICIPAL);
    }

    private Incident verifierIncidentAgent(Long incidentId,
                                           Utilisateur agent,
                                           StatutIncidentEnum statutAttendu) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (incident.getAgent() == null ||
                !incident.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Incident non assigné à vous");
        }

        if (incident.getStatut() != statutAttendu) {
            throw new RuntimeException("Transition invalide");
        }

        return incident;
    }

    private void notifierCitoyen(Incident incident,
                                 String message,
                                 StatutIncidentEnum statut) {

        Utilisateur citoyen = incident.getCitoyen();
        if (citoyen == null) return;

        // Notification
        Notification notification = new Notification();
        notification.setUtilisateur(citoyen);
        notification.setMessage(message + " (Incident #" + incident.getId() + ")");
        notification.setType("INCIDENT");
        notification.setDate(LocalDateTime.now());
        notificationRepository.save(notification);

        // Email
        emailService.envoyerEmailChangementStatutIncident(incident, statut);
    }
    // Nombre d'incidents en attente (signalés mais pas encore pris en charge)
    public long countIncidentsEnAttente() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatut() == StatutIncidentEnum.SIGNALE)
                .count();
    }

    public long countIncidentsResolus() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatut() == StatutIncidentEnum.RESOLU)
                .count();
    }
    public Incident getIncidentById(Long id) {
        return incidentRepository.findById(id).orElseThrow(() -> new RuntimeException("Incident introuvable"));
    }

    public Incident getIncidentByAgentId(Long id, Long agentId) {
        return incidentRepository.findByIdAndAgentId(id, agentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));
    }




}
