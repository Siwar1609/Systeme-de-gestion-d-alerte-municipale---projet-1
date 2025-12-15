package com.example.demo.controllers.agents;

import com.example.demo.dto.agent.AgentUpdateDTO;
import com.example.demo.models.Quartier;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.MunicipalService;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.QuartierRepository;
import com.example.demo.repositories.ServiceRepository;
import com.example.demo.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final UtilisateurRepository utilisateurRepository;
    private final ServiceRepository serviceRepository;
    private final QuartierRepository quartierRepository;

    // ==================== DASHBOARD ====================
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        RoleEnum userRole = (RoleEnum) session.getAttribute("userRole");
        if (userRole != RoleEnum.AGENT_MUNICIPAL) {
            model.addAttribute("error", "Accès réservé aux agents municipaux");
            return "error/access-denied";
        }

        String nom = (String) session.getAttribute("userNom");
        String email = (String) session.getAttribute("userEmail");

        model.addAttribute("pageTitle", "Tableau de Bord Agent Municipal");
        model.addAttribute("userNom", nom);
        model.addAttribute("userEmail", email);

        return "agent/dashboard";
    }

    // ==================== AFFICHAGE PROFIL ====================
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        RoleEnum userRole = (RoleEnum) session.getAttribute("userRole");
        if (userRole != RoleEnum.AGENT_MUNICIPAL) {
            model.addAttribute("error", "Accès non autorisé");
            return "error/access-denied";
        }

        String email = (String) session.getAttribute("userEmail");
        Utilisateur agent = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));

        List<MunicipalService> services = serviceRepository.findAll();
        List<Quartier> quartiers = quartierRepository.findAll();

        model.addAttribute("pageTitle", "Mon Profil Agent");
        model.addAttribute("agent", agent);
        model.addAttribute("services", services);
        model.addAttribute("quartiers", quartiers);

        return "agent/profile";
    }

    // ==================== MODIFICATION PROFIL ====================
    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
                                @ModelAttribute AgentUpdateDTO agentUpdateDTO,
                                Model model) {

        RoleEnum userRole = (RoleEnum) session.getAttribute("userRole");
        if (userRole != RoleEnum.AGENT_MUNICIPAL) {
            model.addAttribute("error", "Accès non autorisé");
            return "error/access-denied";
        }

        String email = (String) session.getAttribute("userEmail");
        Utilisateur agent = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));

        // Mise à jour des informations
        agent.setNom(agentUpdateDTO.getNom());

        if (agentUpdateDTO.getServiceId() != null) {
            MunicipalService service = serviceRepository.findById(agentUpdateDTO.getServiceId())
                    .orElse(null);
            agent.setService(service);
        }

        if (agentUpdateDTO.getQuartierId() != null) {
            Quartier quartier = quartierRepository.findById(agentUpdateDTO.getQuartierId())
                    .orElse(null);
            agent.setQuartier(quartier);
        }

        utilisateurRepository.save(agent);

        // Mettre à jour le nom dans la session
        session.setAttribute("userNom", agent.getNom());

        return "redirect:/agent/profile?success";
    }
}
