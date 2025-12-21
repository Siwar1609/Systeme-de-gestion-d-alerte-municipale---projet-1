package com.example.demo.controllers.citoyen;

import com.example.demo.models.*;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.repositories.CategorieIncidentRepository;
import com.example.demo.repositories.QuartierRepository;
import com.example.demo.repositories.UtilisateurRepository;
import com.example.demo.services.Citoyen.CreationIncidentService;
import com.example.demo.services.Citoyen.CategorieIncidentService;
import com.example.demo.services.Citoyen.IncidentService;
import com.example.demo.services.incidentworkflow.IncidentWorkFlowService;
import com.example.demo.services.quartier.QuartierService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/citoyens")
public class CitoyenController {

    // ================= SERVICES =================

    @Autowired private IncidentService incidentService;
    @Autowired private CreationIncidentService creationIncidentService;
    @Autowired private CategorieIncidentService categorieService;
    @Autowired private QuartierService quartierService;
    @Autowired private IncidentWorkFlowService incidentWorkflowService;

    // ================= REPOSITORIES =================

    @Autowired private CategorieIncidentRepository categorieIncidentRepository;
    @Autowired private QuartierRepository quartierRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    // ================= DASHBOARD =================

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "Tableau de bord Citoyen");
        model.addAttribute("userNom", session.getAttribute("userNom"));
        return "citoyens/dashboard";
    }

    // ================= CREATION INCIDENT =================

    @GetMapping("/incidents/nouveau")
    public String nouveauIncident(Model model, HttpSession session) {

        Incident incident = new Incident();
        incident.setDateSignalement(LocalDateTime.now());
        incident.setStatut(StatutIncidentEnum.SIGNALE);

        model.addAttribute("incident", incident);
        model.addAttribute("categories", categorieService.findAll());
        model.addAttribute("quartiers", quartierService.getAllQuartiers());
        model.addAttribute("userNom", session.getAttribute("userNom"));

        return "citoyens/incident-form";
    }

    @PostMapping("/incidents/creer")
    public String creerIncident(
            @Valid @ModelAttribute("incident") Incident incident,
            BindingResult result,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1️⃣ Validation formulaire
        if (result.hasErrors()) {
            model.addAttribute("categories", categorieService.findAll());
            model.addAttribute("quartiers", quartierService.getAllQuartiers());
            return "citoyens/incident-form";
        }

        // 2️⃣ Citoyen connecté
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }
        incident.setCitoyen(citoyen);

        // 3️⃣ Rebind catégorie
        if (incident.getCategorieId() != null) {
            CategorieIncident categorie = categorieIncidentRepository
                    .findById(incident.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            incident.setCategorie(categorie);
        }

        // 4️⃣ Rebind quartier
        if (incident.getQuartierId() != null) {
            Quartier quartier = quartierRepository
                    .findById(incident.getQuartierId())
                    .orElseThrow(() -> new RuntimeException("Quartier introuvable"));
            incident.setQuartier(quartier);
        }

        // 5️⃣ Création INCIDENT + CLUSTER (LOGIQUE MÉTIER)
        creationIncidentService.creerIncident(incident);

        redirectAttributes.addFlashAttribute(
                "success",
                "Incident signalé avec succès. Il a été rattaché à un incident existant si nécessaire."
        );

        return "redirect:/citoyens/dashboard";
    }

    // ================= MES SIGNALEMENTS =================

    @GetMapping("/incidents/mes-signalements")
    public String mesSignalements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSignalement") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String categorieNom,
            Model model,
            HttpSession session) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null || citoyen.getRole() != RoleEnum.CITOYEN) {
            return "redirect:/login";
        }

        FiltreIncident filtre = new FiltreIncident();
        filtre.setStatut(statut);
        filtre.setLocalisation(localisation);
        filtre.setCategorieNom(categorieNom);

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortField
        );

        Page<Incident> incidents = incidentWorkflowService
                .rechercherIncidentsCitoyen(citoyen.getId(), filtre, pageable);

        model.addAttribute("incidents", incidents);
        model.addAttribute("filtre", filtre);
        model.addAttribute("currentPage", incidents.getNumber());
        model.addAttribute("totalPages", incidents.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("categories", categorieService.findAll());
        model.addAttribute("pageTitle", "Mes signalements");
        model.addAttribute("userNom", session.getAttribute("userNom"));

        return "citoyens/mes-signalements";
    }

    // ================= FEEDBACK / CLOTURE =================

    @GetMapping("/incidents/{id}/feedback")
    public String afficherFormFeedback(@PathVariable Long id,
                                       HttpSession session,
                                       Model model) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) return "redirect:/login";

        Incident incident = incidentWorkflowService
                .getIncidentByIdAndCitoyen(id, citoyen.getId());

        if (incident.getCluster().getStatut() != StatutIncidentEnum.RESOLU) {
            return "redirect:/citoyens/incidents/mes-signalements";
        }

        model.addAttribute("incident", incident);
        model.addAttribute("pageTitle", "Clôturer l'incident");

        return "citoyens/incident-feedback";
    }

    @PostMapping("/incidents/{id}/feedback")
    public String soumettreFeedback(@PathVariable Long id,
                                    @RequestParam("feedback") String feedback,
                                    @RequestParam(value = "note", required = false) Integer note,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) return "redirect:/login";

        incidentWorkflowService.cloturerIncident(id, citoyen, feedback, note);

        redirectAttributes.addFlashAttribute(
                "success",
                "Merci pour votre retour. L'incident est clôturé."
        );

        return "redirect:/citoyens/incidents/mes-signalements";
    }
}
