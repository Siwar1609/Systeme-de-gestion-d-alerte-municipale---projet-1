package com.example.demo.services.admin;

import com.example.demo.dto.admin.AgentCreationRequest;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import com.example.demo.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAgentService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AgentPasswordService agentPasswordService;

    /**
     * Prépare la liste des agents pour l'affichage
     */
    public void prepareAgentsList(Model model) {
        List<Utilisateur> agents = getAllAgents();
        model.addAttribute("agents", agents);
    }

    /**
     * Gère la création d'un agent
     */
    @Transactional
    public String handleAgentCreation(AgentCreationRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Vérification si l'email existe déjà
            if (utilisateurRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà : " + request.getEmail());
            }

            // Génération d'un mot de passe temporaire
            String tempPassword = generateTempPassword();

            // Création de l'agent
            Utilisateur agent = new Utilisateur();
            agent.setEmail(request.getEmail());
            agent.setNom(request.getNom());
            agent.setMotDePasse(passwordEncoder.encode(tempPassword));
            agent.setRole(RoleEnum.AGENT_MUNICIPAL);
            agent.setCompteActive(true);
            agent.setTokenVerification(null);

            // Sauvegarde
            Utilisateur savedAgent = utilisateurRepository.save(agent);

            // Envoi des identifiants par email
            emailService.envoyerIdentifiantsAgent(savedAgent, tempPassword);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Agent créé avec succès ! Les identifiants ont été envoyés par email.");
            return "redirect:/admin/agents";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents/create";

        } catch (RuntimeException e) {
            return handleAgentCreationException(e, request, redirectAttributes);
        }
    }

    /**
     * Gère le changement de statut d'un agent
     */
    @Transactional
    public String handleToggleAgentStatus(Long agentId, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur agent = findAgentById(agentId);
            agent.setCompteActive(!agent.isCompteActive());
            utilisateurRepository.save(agent);

            redirectAttributes.addFlashAttribute("success",
                    " Statut de l'agent modifié avec succès.");
            return "redirect:/admin/agents";

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    " Erreur lors du changement de statut: " + e.getMessage());
        }
        return "redirect:/admin/agents";
    }

    /**
     * Gère la réinitialisation du mot de passe d'un agent
     */
    @Transactional
    public String handleResetAgentPassword(Long agentId, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur agent = findAgentById(agentId);
            String newPassword = agentPasswordService.resetAgentPassword(agent);

            redirectAttributes.addFlashAttribute("info",
                    "🔑 Mot de passe réinitialisé. Nouveau mot de passe: " + newPassword);
            redirectAttributes.addFlashAttribute("passwordInfo",
                    "Note: Le nouveau mot de passe a été envoyé par email à l'agent.");
            return "redirect:/admin/agents";

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "  " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "  Erreur lors de la réinitialisation: " + e.getMessage());
        }
        return "redirect:/admin/agents";
    }

    /**
     * Récupère tous les agents
     */
    public List<Utilisateur> getAllAgents() {
        return utilisateurRepository.findByRole(RoleEnum.AGENT_MUNICIPAL);
    }

    /**
     * Trouve un agent par son ID
     */
    private Utilisateur findAgentById(Long agentId) {
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent non trouvé avec l'id : " + agentId));

        if (agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            throw new IllegalStateException("L'utilisateur avec l'id " + agentId + " n'est pas un agent municipal");
        }

        return agent;
    }

    /**
     * Gère les exceptions spécifiques lors de la création d'agent
     */
    private String handleAgentCreationException(RuntimeException e, AgentCreationRequest request,
                                                RedirectAttributes redirectAttributes) {
        String errorMsg = e.getMessage();

        if (errorMsg.contains("email") || errorMsg.contains("Email") ||
                errorMsg.contains("SMTP") || errorMsg.contains("mail")) {

            redirectAttributes.addFlashAttribute("warning",
                    "Agent créé mais problème d'envoi d'email. " +
                            "Les identifiants apparaissent dans les logs de l'application.");
            return "redirect:/admin/agents";
        } else {
            redirectAttributes.addFlashAttribute("error", " " + errorMsg);
            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents/create";
        }
    }

    /**
     * Génère un mot de passe temporaire
     */
    private String generateTempPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}