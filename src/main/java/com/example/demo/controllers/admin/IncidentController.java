package com.example.demo.controllers.admin;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.models.enums.PrioriteIncidentEnum;
import com.example.demo.services.Citoyen.CategorieIncidentService;
import com.example.demo.services.incidentworkflow.IncidentWorkFlowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.demo.models.FiltreIncident;

import java.util.List;

@Controller
@RequestMapping("/admin/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentWorkFlowService incidentWorkflowService;
    @Autowired
    private CategorieIncidentService categorieService;

    // ----------- LISTE DES INCIDENTS -----------
    @GetMapping
    public String afficherIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSignalement") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String categorieNom,
            Model model,
            HttpSession session) {

        Utilisateur admin = (Utilisateur) session.getAttribute("utilisateur");
        if (admin == null || admin.getRole() != RoleEnum.ADMINISTRATEUR) {
            return "redirect:/login";
        }

        FiltreIncident filtre = new FiltreIncident();
        filtre.setStatut(statut);
        filtre.setLocalisation(localisation);
        filtre.setCategorieNom(categorieNom);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);

        Page<Incident> incidents = incidentWorkflowService.rechercherIncidents(filtre, pageable);

        model.addAttribute("pageTitle", "État des incidents");
        model.addAttribute("userNom", admin.getNom());
        model.addAttribute("incidents", incidents);
        model.addAttribute("categories", categorieService.findAll());
        model.addAttribute("agents", incidentWorkflowService.getTousLesAgents());

        model.addAttribute("filtre", filtre);
        model.addAttribute("currentPage", incidents.getNumber());
        model.addAttribute("totalPages", incidents.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

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