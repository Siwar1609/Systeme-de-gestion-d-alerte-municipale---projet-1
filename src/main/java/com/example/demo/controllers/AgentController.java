package com.example.demo.controllers;
import com.example.demo.models.enums.RoleEnum;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Vérifier si l'utilisateur est un agent
        RoleEnum userRole = (RoleEnum) session.getAttribute("userRole");

        if (userRole != RoleEnum.AGENT_MUNICIPAL) {
            model.addAttribute("error", "Accès réservé aux agents municipaux");
            return "error/access-denied";
        }

        // Récupérer les infos de session
        String nom = (String) session.getAttribute("userNom");
        String email = (String) session.getAttribute("userEmail");

        model.addAttribute("pageTitle", "Tableau de Bord Agent Municipal");
        model.addAttribute("userNom", nom);
        model.addAttribute("userEmail", email);

        return "agent/dashboard";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        RoleEnum userRole = (RoleEnum) session.getAttribute("userRole");

        if (userRole != RoleEnum.AGENT_MUNICIPAL) {
            model.addAttribute("error", "Accès non autorisé");
            return "error/access-denied";
        }

        model.addAttribute("pageTitle", "Mon Profil Agent");
        return "agent/profile";
    }
}