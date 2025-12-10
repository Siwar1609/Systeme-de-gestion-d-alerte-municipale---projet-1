package com.example.demo.services.admin;

import com.example.demo.models.enums.RoleEnum;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class AdminService {

    /**
     * Vérifie si l'utilisateur est administrateur
     */
    public boolean isAdmin(HttpSession session) {
        if (session == null) return false;
        Object roleObj = session.getAttribute("userRole");
        return roleObj instanceof RoleEnum && roleObj == RoleEnum.ADMINISTRATEUR;
    }

    /**
     * Redirige si l'utilisateur n'est pas admin
     */
    public String checkAdminAccess(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            model.addAttribute("pageTitle", "Accès Refusé");
            model.addAttribute("error", "Accès réservé aux administrateurs");
            return "error/access-denied";
        }
        return null;
    }
}