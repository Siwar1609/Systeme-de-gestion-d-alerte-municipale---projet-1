package com.example.demo.controllers.admin;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.models.enums.PrioriteIncidentEnum;
import com.example.demo.services.incidentworkflow.IncidentWorkFlowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentWorkFlowService incidentWorkflowService;

    // ----------- LISTE DES INCIDENTS -----------
    @GetMapping
    public String afficherIncidents(Model model, HttpSession session) {
        Utilisateur admin = (Utilisateur) session.getAttribute("utilisateur");

        // Sécurité : accès uniquement aux admins
        if (admin == null || admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            return "redirect:/login";
        }

        List<Incident> incidents = incidentWorkflowService.getTousLesIncidents();
        List<Utilisateur> agents = incidentWorkflowService.getTousLesAgents();

        model.addAttribute("pageTitle", "État des incidents");
        model.addAttribute("userNom", admin.getNom());
        model.addAttribute("incidents", incidents);
        model.addAttribute("agents", agents);

        return "admin/incidents/incidents";
    }
    @PostMapping("/modifierPriorite")
    public String modifierPriorite(
            @RequestParam Long incidentId,
            @RequestParam PrioriteIncidentEnum priorite,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Utilisateur admin = (Utilisateur) session.getAttribute("utilisateur");

        if (admin == null || admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            redirectAttributes.addFlashAttribute("error", "Accès refusé ou session expirée.");
            return "redirect:/login";
        }

        try {
            incidentWorkflowService.modifierPrioriteIncident(incidentId, priorite, admin);
            redirectAttributes.addFlashAttribute("success", "Priorité modifiée avec succès !");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/incidents";
    }

    // ----------- ASSIGNATION D’UN INCIDENT -----------
    @PostMapping("/assigner")

    public String assignerIncident(
            @RequestParam Long incidentId,
            @RequestParam Long agentId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Utilisateur admin = (Utilisateur) session.getAttribute("utilisateur");

        if (admin == null || admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            redirectAttributes.addFlashAttribute("error", "Accès refusé ou session expirée.");
            return "redirect:/login";
        }

        try {
            // On passe null pour la priorité car elle n'est pas modifiée ici
            incidentWorkflowService.assignerIncident(incidentId, agentId, null, admin);
            redirectAttributes.addFlashAttribute("success", "Incident assigné avec succès !");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/incidents";
    }




}
