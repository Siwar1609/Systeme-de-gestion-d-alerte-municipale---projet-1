package com.example.demo.services.agent;

import com.example.demo.models.Utilisateur;
import com.example.demo.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentPasswordService {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Réinitialise le mot de passe d'un agent
     */
    @Transactional
    public String resetAgentPassword(Utilisateur agent) {
        String newPassword = generateTempPassword();
        agent.setMotDePasse(passwordEncoder.encode(newPassword));

        emailService.envoyerReinitialisationMotDePasse(agent, newPassword);
        return newPassword;
    }

    /**
     * Génère un mot de passe temporaire
     */
    private String generateTempPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}