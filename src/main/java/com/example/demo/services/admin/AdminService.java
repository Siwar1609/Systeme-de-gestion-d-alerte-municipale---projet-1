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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Créer un nouvel agent municipal
     */
    @Transactional
    public Utilisateur creerAgent(AgentCreationRequest request) {
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
        agent.setCompteActive(true); // Compte actif immédiatement
        agent.setTokenVerification(null);

        // Sauvegarde
        Utilisateur savedAgent = utilisateurRepository.save(agent);

        // Envoi des identifiants par email
        emailService.envoyerIdentifiantsAgent(savedAgent, tempPassword);

        return savedAgent;
    }

    /**
     * Réinitialiser le mot de passe d'un agent
     */
    @Transactional
    public String resetAgentPassword(Long agentId) {
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent non trouvé avec l'id : " + agentId));

        if (agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            throw new IllegalStateException("L'utilisateur avec l'id " + agentId + " n'est pas un agent municipal");
        }

        String newPassword = generateTempPassword();
        agent.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(agent);

        emailService.envoyerReinitialisationMotDePasse(agent, newPassword);
        return newPassword;
    }

    /**
     * Activer/désactiver un agent
     */
    @Transactional
    public void toggleAgentStatus(Long agentId) {
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent non trouvé avec l'id : " + agentId));

        if (agent.getRole() != RoleEnum.AGENT_MUNICIPAL) {
            throw new IllegalStateException("L'utilisateur avec l'id " + agentId + " n'est pas un agent municipal");
        }

        agent.setCompteActive(!agent.isCompteActive());
        utilisateurRepository.save(agent);

        // Optionnel : Envoyer un email de notification (si vous voulez implémenter)
        // Pour l'instant, on ne fait rien ou on log
        System.out.println("Statut agent " + agent.getEmail() + " changé à: " +
                (agent.isCompteActive() ? "ACTIF" : "INACTIF"));
    }

    /**
     * Lister tous les agents
     */
    public List<Utilisateur> getAllAgents() {
        return utilisateurRepository.findByRole(RoleEnum.AGENT_MUNICIPAL);
    }

    /**
     * Lister tous les citoyens
     */
    public List<Utilisateur> getAllCitoyens() {
        return utilisateurRepository.findByRole(RoleEnum.CITOYEN);
    }

    /**
     * Récupérer l'admin principal
     */
    public Utilisateur getAdminPrincipal() {
        return utilisateurRepository.findByEmail("admin@ville.fr")
                .orElseThrow(() -> new IllegalStateException("Admin principal non trouvé"));
    }

    /**
     * Vérifier si un email est celui de l'admin
     */
    public boolean isAdminEmail(String email) {
        return "admin@ville.fr".equalsIgnoreCase(email);
    }

    /**
     * Générer un mot de passe temporaire (8 caractères)
     */
    private String generateTempPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}