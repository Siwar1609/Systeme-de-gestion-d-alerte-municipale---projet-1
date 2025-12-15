package com.example.demo.services.incidentworkflow;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.models.enums.PrioriteIncidentEnum;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentWorkFlowService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ================= ADMIN =================
    public void assignerIncident(Long incidentId, Long agentId, PrioriteIncidentEnum priorite, Utilisateur admin) {
        if (admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            throw new RuntimeException("Accès interdit : vous devez être administrateur");
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

        if (!agent.getService().getNom().equals(incident.getCategorie().getNom())) {
            throw new RuntimeException("L'agent choisi n'est pas dans le service traitant cette catégorie d'incident");
        }

        incident.setAgent(agent);

        // Modifier la priorité uniquement si elle est fournie
        if (priorite != null) {
            incident.setPriorite(priorite);
        }

        incident.setStatut(StatutIncidentEnum.EN_COURS_DE_CHARGE);
        incidentRepository.save(incident);
    }



    // ================= AGENT =================
    public void commencerResolution(Long incidentId, Utilisateur agent) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (incident.getAgent() == null || !incident.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Non autorisé : cet incident n'est pas assigné à vous");
        }

        if (incident.getStatut() != StatutIncidentEnum.EN_COURS_DE_CHARGE) {
            throw new RuntimeException("Transition invalide : l'incident n'est pas en charge");
        }

        incident.setStatut(StatutIncidentEnum.EN_RESOLUTION);
        incidentRepository.save(incident);
    }

    public void marquerResolu(Long incidentId, Utilisateur agent) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (incident.getAgent() == null || !incident.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Non autorisé : cet incident n'est pas assigné à vous");
        }

        if (incident.getStatut() != StatutIncidentEnum.EN_RESOLUTION) {
            throw new RuntimeException("Transition invalide : l'incident n'est pas en cours de résolution");
        }

        incident.setStatut(StatutIncidentEnum.RESOLU);
        incidentRepository.save(incident);
    }

    // ================= CITOYEN =================
    public void cloturerIncident(Long incidentId, Utilisateur citoyen) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (incident.getCitoyen() == null || !incident.getCitoyen().getId().equals(citoyen.getId())) {
            throw new RuntimeException("Non autorisé : vous n'êtes pas le déclarant de cet incident");
        }

        if (incident.getStatut() != StatutIncidentEnum.RESOLU) {
            throw new RuntimeException("L'incident n'est pas encore résolu");
        }

        incident.setStatut(StatutIncidentEnum.CLOTURE);
        incidentRepository.save(incident);
    }
    public void modifierPrioriteIncident(Long incidentId, PrioriteIncidentEnum priorite, Utilisateur admin) {
        if (admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            throw new RuntimeException("Accès interdit : vous devez être administrateur");
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        // On peut modifier la priorité même si l'incident est déjà assigné
        incident.setPriorite(priorite);
        incidentRepository.save(incident);
    }

    // ================= UTILS =================
    public List<Incident> getTousLesIncidents() {
        return incidentRepository.findAll();
    }

    public List<Utilisateur> getTousLesAgents() {
        return utilisateurRepository.findByRole(RoleEnum.AGENT_MUNICIPAL);
    }
}
