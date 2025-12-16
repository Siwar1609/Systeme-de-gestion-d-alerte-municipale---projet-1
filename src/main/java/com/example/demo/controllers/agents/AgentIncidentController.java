package com.example.demo.controllers.agents;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.services.incidentworkflow.IncidentWorkFlowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.demo.models.FiltreIncident;

import java.util.List;

@Controller
@RequestMapping("/agent/incidents")
@RequiredArgsConstructor
public class AgentIncidentController {

    private final IncidentWorkFlowService incidentWorkflowService;

    // ========================= Liste des incidents assignés =========================
    @GetMapping
    public String mesIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSignalement") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String categorieNom,
            Model model,
            HttpSession session) {

        Utilisateur agent = (Utilisateur) session.getAttribute("utilisateur");

        if (agent == null || agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            return "redirect:/login";
        }

        // ✅ NOUVEAU : création du filtre
        FiltreIncident filtre = new FiltreIncident();
        filtre.setStatut(statut);
        filtre.setLocalisation(localisation);
        filtre.setCategorieNom(categorieNom);

        // ✅ NOUVEAU : pagination + tri
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);

        // ✅ NOUVEAU : recherche paginée
        Page<Incident> incidents = incidentWorkflowService
                .rechercherIncidentsAgent(agent.getId(), filtre, pageable);

        // ✅ TES attributs existants + NOUVEAUX
        model.addAttribute("pageTitle", "Mes incidents assignés");
        model.addAttribute("incidents", incidents);  // maintenant c'est une Page<Incident>
        model.addAttribute("userNom", agent.getNom());

        // ✅ NOUVEAUX attributs pour le template
        model.addAttribute("filtre", filtre);
        model.addAttribute("currentPage", incidents.getNumber());
        model.addAttribute("totalPages", incidents.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "agent/incidents";
    }

    // ========================= Dashboard =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Utilisateur agent = (Utilisateur) session.getAttribute("utilisateur");

        if (agent == null || agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            return "redirect:/login";
        }

        model.addAttribute("userNom", agent.getNom());
        model.addAttribute("userEmail", agent.getEmail());
        model.addAttribute("nbEnAttente", incidentWorkflowService.countIncidentsEnAttente());
        model.addAttribute("nbResolus", incidentWorkflowService.countIncidentsResolus());

        return "agent/dashboard";
    }

    // ========================= Commencer le traitement d'un incident =========================
    @PostMapping("/commencer")
    public String commencerIncident(@RequestParam Long incidentId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Utilisateur agent = (Utilisateur) session.getAttribute("utilisateur");

        if (agent == null || agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            redirectAttributes.addFlashAttribute("error", "Accès refusé");
            return "redirect:/login";
        }

        try {
            incidentWorkflowService.commencerResolution(incidentId, agent);
            redirectAttributes.addFlashAttribute("success", "Traitement de l'incident commencé !");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/agent/incidents";
    }

    // ========================= Marquer résolu =========================
    @PostMapping("/resolu")
    public String marquerResolu(@RequestParam Long incidentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Utilisateur agent = (Utilisateur) session.getAttribute("utilisateur");

        if (agent == null || agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            redirectAttributes.addFlashAttribute("error", "Accès refusé");
            return "redirect:/login";
        }

        try {
            incidentWorkflowService.marquerResolu(incidentId, agent);
            redirectAttributes.addFlashAttribute("success", "Incident marqué comme résolu !");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/agent/incidents";
    }
    @GetMapping("/{id}")
    public String detailIncidentAgent(@PathVariable Long id, HttpSession session, Model model) {
        Utilisateur agent = (Utilisateur) session.getAttribute("utilisateur");
        Incident incident = incidentWorkflowService.getIncidentByAgentId(id, agent.getId());
        model.addAttribute("incident", incident);
        model.addAttribute("pageTitle", "Détail incident");
        return "agent/incident-detail";
    }

}
