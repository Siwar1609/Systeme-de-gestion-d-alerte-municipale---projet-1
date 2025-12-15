package com.example.demo.controllers.agents;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
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
@RequestMapping("/agent/incidents")
@RequiredArgsConstructor
public class AgentIncidentController {

    private final IncidentWorkFlowService incidentWorkflowService;

    // ========================= Liste des incidents assignés =========================
    @GetMapping
    public String mesIncidents(Model model, HttpSession session) {
        Utilisateur agent = (Utilisateur) session.getAttribute("utilisateur");

        if (agent == null || agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            return "redirect:/login";
        }

        List<Incident> incidents = incidentWorkflowService.getIncidentsAssignes(agent);

        model.addAttribute("pageTitle", "Mes incidents assignés");
        model.addAttribute("incidents", incidents);
        model.addAttribute("userNom", agent.getNom());

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
}
