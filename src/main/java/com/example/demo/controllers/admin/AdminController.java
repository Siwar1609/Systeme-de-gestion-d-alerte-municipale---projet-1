package com.example.demo.controllers.admin;

import com.example.demo.dto.admin.AgentCreationRequest;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import com.example.demo.services.admin.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UtilisateurRepository utilisateurRepository;

    // ==================== MIDDLEWARE ====================

    /**
     * Vérifie si l'utilisateur est administrateur
     */
    private boolean isAdmin(HttpSession session) {
        if (session == null) return false;
        Object roleObj = session.getAttribute("userRole");
        return roleObj instanceof RoleEnum && roleObj == RoleEnum.ADMINISTRATEUR;
    }

    /**
     * Redirige si l'utilisateur n'est pas admin
     */
    private String checkAdminAccess(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            model.addAttribute("pageTitle", "Accès Refusé");
            model.addAttribute("error", "Accès réservé aux administrateurs");
            return "error/access-denied";
        }
        return null;
    }

    // ==================== DASHBOARD ADMIN ====================

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            // Statistiques
            long totalCitoyens = adminService.getAllCitoyens().size();
            long totalAgents = adminService.getAllAgents().size();
            long citoyensActifs = utilisateurRepository.findByRoleAndCompteActive(RoleEnum.CITOYEN, true).size();
            long agentsActifs = utilisateurRepository.findByRoleAndCompteActive(RoleEnum.AGENT_MUNICIPAL, true).size();

            model.addAttribute("totalCitoyens", totalCitoyens);
            model.addAttribute("totalAgents", totalAgents);
            model.addAttribute("citoyensActifs", citoyensActifs);
            model.addAttribute("agentsActifs", agentsActifs);
            model.addAttribute("userNom", session.getAttribute("userNom"));
            model.addAttribute("userEmail", session.getAttribute("userEmail"));
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
            List<Utilisateur> agents = adminService.getAllAgents();
            model.addAttribute("agents", agents);
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

        // Vérifier l'accès admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        // Gérer les erreurs de validation
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.agentRequest",
                    bindingResult
            );
            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents/create";
        }

        try {
            // Créer l'agent
            adminService.creerAgent(request);

            // Succès
            redirectAttributes.addFlashAttribute("success",
                    " Agent créé avec succès ! Les identifiants ont été envoyés par email.");

            return "redirect:/admin/agents";

        } catch (IllegalArgumentException e) {
            // Erreur de validation (email déjà existant)
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents/create";

        } catch (RuntimeException e) {
            // Autres erreurs (SMTP, etc.)
            String errorMsg = e.getMessage();

            if (errorMsg.contains("email") || errorMsg.contains("Email") ||
                    errorMsg.contains("SMTP") || errorMsg.contains("mail")) {

                redirectAttributes.addFlashAttribute("warning",
                        "️ Agent créé mais problème d'envoi d'email. " +
                                "Les identifiants apparaissent dans les logs de l'application.");
            } else {
                redirectAttributes.addFlashAttribute("error", " " + errorMsg);
            }

            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents/create";
        }
    }

    @PostMapping("/agents/{id}/toggle-status")
    public String toggleAgentStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        try {
            adminService.toggleAgentStatus(id);
            redirectAttributes.addFlashAttribute("success",
                    " Statut de l'agent modifié avec succès.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    " Erreur lors du changement de statut: " + e.getMessage());
        }

        return "redirect:/admin/agents";
    }

    @PostMapping("/agents/{id}/reset-password")
    public String resetAgentPassword(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        try {
            String newPassword = adminService.resetAgentPassword(id);

            redirectAttributes.addFlashAttribute("info",
                    "🔑 Mot de passe réinitialisé. Nouveau mot de passe: " + newPassword);
            redirectAttributes.addFlashAttribute("passwordInfo",
                    "Note: Le nouveau mot de passe a été envoyé par email à l'agent.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    " Erreur lors de la réinitialisation: " + e.getMessage());
        }

        return "redirect:/admin/agents";
    }

    // ==================== GESTION CITOYENS ====================

    @GetMapping("/citoyens")
    public String listCitoyens(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            List<Utilisateur> citoyens = adminService.getAllCitoyens();
            model.addAttribute("citoyens", citoyens);
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

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        try {
            Utilisateur citoyen = utilisateurRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Citoyen non trouvé"));

            if (citoyen.getRole() != RoleEnum.CITOYEN) {
                throw new IllegalStateException("Cet utilisateur n'est pas un citoyen");
            }

            citoyen.setCompteActive(!citoyen.isCompteActive());
            utilisateurRepository.save(citoyen);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Statut du citoyen modifié: " +
                            (citoyen.isCompteActive() ? "Activé" : "Désactivé"));

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    " Erreur lors du changement de statut: " + e.getMessage());
        }

        return "redirect:/admin/citoyens";
    }

    @PostMapping("/citoyens/{id}/delete")
    public String deleteCitoyen(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
            return "redirect:/login";
        }

        try {
            Utilisateur citoyen = utilisateurRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Citoyen non trouvé"));

            if (citoyen.getRole() != RoleEnum.CITOYEN) {
                throw new IllegalStateException("Cet utilisateur n'est pas un citoyen");
            }

            utilisateurRepository.delete(citoyen);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Citoyen supprimé avec succès");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    " Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/admin/citoyens";
    }

    // ==================== PROFIL ADMIN ====================

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        String error = checkAdminAccess(session, model);
        if (error != null) return error;

        try {
            String adminEmail = (String) session.getAttribute("userEmail");
            Utilisateur admin = utilisateurRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

            model.addAttribute("admin", admin);
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

    // ==================== REDIRECTION SI ADMIN DÉJÀ CONNECTÉ ====================

    @GetMapping("/")
    public String redirectAdmin(HttpSession session) {
        if (isAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/login";
    }
}