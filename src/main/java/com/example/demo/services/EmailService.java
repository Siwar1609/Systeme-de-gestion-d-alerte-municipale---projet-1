package com.example.demo.services;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    /**
     * Envoie les identifiants à un nouvel agent municipal
     */
    public void envoyerIdentifiantsAgent(Utilisateur agent, String motDePasseTemporaire) {
        try {
            String sujet = " Vos identifiants Agent Municipal - Plateforme Signalement";
            String contenu = String.format("""
                    Bonjour %s,

                    Votre compte Agent Municipal a été créé avec succès.

                    🔐 Identifiants :
                    • Email : %s
                    • Mot de passe temporaire : %s

                    🌐 Connexion : %s/login

                    Veuillez changer votre mot de passe dès votre première connexion.

                    Cordialement,
                    Plateforme Signalement Municipal
                    """, agent.getNom(), agent.getEmail(), motDePasseTemporaire, frontendUrl);

            envoyerEmail(agent.getEmail(), sujet, contenu);

        } catch (Exception e) {
            log.error(" ERREUR SMTP pour l'agent {}", agent.getEmail(), e);
            log.warn(" Email non envoyé mais utilisateur créé. Mot de passe temporaire : {}", motDePasseTemporaire);
            System.out.println(" Mot de passe temporaire pour test : " + motDePasseTemporaire);
        }
    }

    /**
     * Réinitialisation du mot de passe
     */
    public void envoyerReinitialisationMotDePasse(Utilisateur utilisateur, String nouveauMotDePasse) {
        try {
            String sujet = " Réinitialisation Mot de Passe - " + getRoleString(utilisateur.getRole());
            String contenu = String.format("""
                    Bonjour,

                    Votre mot de passe a été réinitialisé.

                    🔐 Nouveau mot de passe :
                    %s

                    Veuillez vous connecter et le changer immédiatement.

                    Cordialement.
                    """, nouveauMotDePasse);

            envoyerEmail(utilisateur.getEmail(), sujet, contenu);

        } catch (Exception e) {
            log.error("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }

    /**
     * Email de vérification pour l'inscription citoyen
     */
    public void envoyerEmailVerification(String email, String token) {
        try {
            String sujet = " Vérification de votre compte - Plateforme Signalement";
            String lienVerification = frontendUrl + "/citoyens/verifier-email?token=" + token;
            String contenu = String.format("""
                    Bonjour,

                    Merci de vous être inscrit.

                    Veuillez vérifier votre email en cliquant sur le lien suivant :

                    %s

                    Ce lien expire dans 24 heures.

                    Cordialement,
                    Plateforme Signalement Municipal
                    """, lienVerification);

            envoyerEmail(email, sujet, contenu);

        } catch (Exception e) {
            log.error("Impossible d'envoyer l'email de vérification à {}", email, e);
            throw new RuntimeException(e); // tu peux laisser throw si tu veux catcher côté service
        }
    }

    /**
     * Email de bienvenue après validation du compte
     */
    public void envoyerEmailBienvenue(String email, String nom) {
        try {
            String sujet = " Bienvenue sur la Plateforme Signalement";
            String contenu = String.format("""
                    Bonjour %s,

                    Votre compte a été activé avec succès.

                    Vous pouvez désormais vous connecter et signaler vos incidents.

                    🌐 Connexion : %s/login

                    Cordialement,
                    Plateforme Signalement Municipal
                    """, nom, frontendUrl);

            envoyerEmail(email, sujet, contenu);

        } catch (Exception e) {
            log.error(" Impossible d'envoyer l'email de bienvenue à {}", email, e);
        }
    }

    /**
     * Méthode interne pour envoyer un mail simple
     */
    private void envoyerEmail(String destinataire, String sujet, String contenu) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(contenu);
        message.setFrom(emailFrom);
        mailSender.send(message);
        log.info(" Email envoyé à {}", destinataire);
    }

    private String getRoleString(RoleEnum role) {
        return switch (role) {
            case ADMINISTRATEUR -> "Administrateur";
            case AGENT_MUNICIPAL -> "Agent Municipal";
            case CITOYEN -> "Citoyen";
        };
    }
}
