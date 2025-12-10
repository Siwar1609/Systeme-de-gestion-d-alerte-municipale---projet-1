package com.example.demo.services.admin;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Prépare les données pour le tableau de bord
     */
    public void prepareDashboardData(Model model) {
        long totalCitoyens = utilisateurRepository.findByRole(RoleEnum.CITOYEN).size();
        long totalAgents = utilisateurRepository.findByRole(RoleEnum.AGENT_MUNICIPAL).size();
        long citoyensActifs = utilisateurRepository.findByRoleAndCompteActive(RoleEnum.CITOYEN, true).size();
        long agentsActifs = utilisateurRepository.findByRoleAndCompteActive(RoleEnum.AGENT_MUNICIPAL, true).size();

        model.addAttribute("totalCitoyens", totalCitoyens);
        model.addAttribute("totalAgents", totalAgents);
        model.addAttribute("citoyensActifs", citoyensActifs);
        model.addAttribute("agentsActifs", agentsActifs);
    }

    /**
     * Prépare les données du profil admin
     */
    public void prepareAdminProfile(HttpSession session, Model model) {
        String adminEmail = (String) session.getAttribute("userEmail");
        Utilisateur admin = utilisateurRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));
        model.addAttribute("admin", admin);
    }
}