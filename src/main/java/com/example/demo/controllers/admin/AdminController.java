package com.example.demo.controllers.admin;

import com.example.demo.dto.admin.AgentCreationRequest;
import com.example.demo.services.admin.AdminDashboardService;
import com.example.demo.services.admin.AdminAgentService;
import com.example.demo.services.admin.AdminCitoyenService;
import com.example.demo.services.admin.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminDashboardService adminDashboardService;
    private final AdminAgentService adminAgentService;
    private final AdminCitoyenService adminCitoyenService;

    // ==================== MIDDLEWARE ====================

    @ModelAttribute
    public void addCommonAttributes(HttpSession session, Model model) {
        if (adminService.isAdmin(session)) {
            model.addAttribute("userNom", session.getAttribute("userNom"));
            model.addAttribute("userEmail", session.getAttribute("userEmail"));
        }
    }

    /**
     * Vérifie l'accès admin et redirige si nécessaire
     */
    private String checkAdminAccess(HttpSession session, Model model) {
        return adminService.checkAdminAccess(session, model);
    }

    // ==================== DASHBOARD ADMIN ====================

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            adminDashboardService.prepareDashboardData(model);
            model.addAttribute("pageTitle", "Tableau de Bord Administrateur");
            return "admin/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du tableau de bord: " + e.getMessage());
            return "error/server-error";
        }
    }

    // ==================== GESTION AGENTS ====================

    @GetMapping("/agents")
    public String listAgents(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            adminAgentService.prepareAgentsList(model);
            model.addAttribute("pageTitle", "Gestion des Agents Municipaux");
            return "admin/agents/list";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des agents: " + e.getMessage());
            return "error/server-error";
        }
    }

    @GetMapping("/agents/create")
    public String showCreateAgentForm(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        if (!model.containsAttribute("agentRequest")) {
            model.addAttribute("agentRequest", new AgentCreationRequest());
        }
        model.addAttribute("pageTitle", "Créer un Agent Municipal");
        return "admin/agents/create";
    }

    @PostMapping("/agents/create")
    public String createAgent(
            @Valid @ModelAttribute("agentRequest") AgentCreationRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.agentRequest",
                    bindingResult
            );
            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents/create";
        }

        return adminAgentService.handleAgentCreation(request, redirectAttributes);
    }

    @PostMapping("/agents/{id}/toggle-status")
    public String toggleAgentStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        return adminAgentService.handleToggleAgentStatus(id, redirectAttributes);
    }

    @PostMapping("/agents/{id}/reset-password")
    public String resetAgentPassword(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        return adminAgentService.handleResetAgentPassword(id, redirectAttributes);
    }

    // ==================== GESTION CITOYENS ====================

    @GetMapping("/citoyens")
    public String listCitoyens(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            adminCitoyenService.prepareCitoyensList(model);
            model.addAttribute("pageTitle", "Gestion des Citoyens");
            return "admin/citoyens/list";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des citoyens: " + e.getMessage());
            return "error/server-error";
        }
    }

    @PostMapping("/citoyens/{id}/toggle-status")
    public String toggleCitoyenStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        return adminCitoyenService.handleToggleCitoyenStatus(id, redirectAttributes);
    }

    @PostMapping("/citoyens/{id}/delete")
    public String deleteCitoyen(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        return adminCitoyenService.handleDeleteCitoyen(id, redirectAttributes);
    }

    // ==================== PROFIL ADMIN ====================

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            adminDashboardService.prepareAdminProfile(session, model);
            model.addAttribute("pageTitle", "Mon Profil Administrateur");
            return "admin/profile";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du profil: " + e.getMessage());
            return "error/server-error";
        }
    }

    // ==================== LOGOUT ADMIN ====================

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=admin";
    }

    // ==================== REDIRECTION ====================

    @GetMapping("/")
    public String redirectAdmin(HttpSession session) {
        if (adminService.isAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/login";
    }
}