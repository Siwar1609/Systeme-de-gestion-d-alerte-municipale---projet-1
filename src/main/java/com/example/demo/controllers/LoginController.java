package com.example.demo.controllers;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.services.InscriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private final InscriptionService inscriptionService;

    /**
     * Page de connexion

     */
    @GetMapping("/login")
    public String showLoginForm(Model model,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout) {

        model.addAttribute("pageTitle", "Connexion");

        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("success", "Vous avez été déconnecté avec succès");
        }

        return "login";
    }

    /**
     * Traitement de la connexion
     *  Redirection dashboard UNIQUEMENT après login
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Utilisateur> userOpt =
                    inscriptionService.authentifierUtilisateur(email, password);

            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Email ou mot de passe incorrect");
                return "redirect:/login?error=true";
            }

            Utilisateur user = userOpt.get();

            if (!inscriptionService.peutSeConnecter(user)) {
                redirectAttributes.addFlashAttribute("error", "Compte non autorisé");
                return "redirect:/login";
            }

            // Stocker la session
            session.setAttribute("utilisateur", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNom", user.getNom());
            session.setAttribute("userRole", user.getRole());

            session.setMaxInactiveInterval(8 * 60 * 60);

            // ✅ Redirection seulement APRÈS login
            return redirectBasedOnRole(user.getRole());

        } catch (Exception e) {
            log.error("Erreur lors de la connexion", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la connexion");
            return "redirect:/login?error=true";
        }
    }

    /**
     * Redirection basée sur le rôle (APRÈS LOGIN UNIQUEMENT)
     */
    private String redirectBasedOnRole(RoleEnum role) {
        switch (role) {
            case CITOYEN:
                return "redirect:/citoyens/dashboard";
            case AGENT_MUNICIPAL:
                return "redirect:/agent/dashboard";
            case ADMINISTRATEUR:
                return "redirect:/admin/dashboard";
            default:
                return "redirect:/login?error=role";
        }
    }

    /**
     * Déconnexion
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
