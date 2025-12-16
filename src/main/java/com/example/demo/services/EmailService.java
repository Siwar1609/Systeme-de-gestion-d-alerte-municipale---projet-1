package com.example.demo.services;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.models.enums.StatutIncidentEnum;
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

    // =========================================================
    // AGENT MUNICIPAL
    // =========================================================

    public void envoyerIdentifiantsAgent(Utilisateur agent, String motDePasseTemporaire) {
        try {
            String sujet = "Vos identifiants Agent Municipal - Plateforme Signalement";
            String contenu = String.format("""
                    Bonjour %s,

                    Votre compte Agent Municipal a été créé avec succès.

                    Identifiants :
                    • Email : %s
                    • Mot de passe temporaire : %s

                    Connexion : %s/login

                    Veuillez changer votre mot de passe dès votre première connexion.

                    Cordialement,
                    Plateforme Signalement Municipal
                    """, agent.getNom(), agent.getEmail(), motDePasseTemporaire, frontendUrl);

            envoyerEmail(agent.getEmail(), sujet, contenu);

        } catch (Exception e) {
            log.error("Erreur SMTP pour l'agent {}", agent.getEmail(), e);
        }
    }

    // =========================================================
    // MOT DE PASSE
    // =========================================================

    public void envoyerReinitialisationMotDePasse(Utilisateur utilisateur, String nouveauMotDePasse) {
        try {
            String sujet = "Réinitialisation Mot de Passe - " + getRoleString(utilisateur.getRole());
            String contenu = String.format("""
                    Bonjour,

                    Votre mot de passe a été réinitialisé.

                    Nouveau mot de passe :
                    %s

                    Veuillez le changer immédiatement après connexion.

                    Cordialement.
                    """, nouveauMotDePasse);

            envoyerEmail(utilisateur.getEmail(), sujet, contenu);

        } catch (Exception e) {
            log.error("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }

    // =========================================================
    // CITOYEN - INSCRIPTION
    // =========================================================

    public void envoyerEmailVerification(String email, String token) {
        String lienVerification = frontendUrl + "/citoyens/verifier-email?token=" + token;

        String sujet = "Vérification de votre compte - Plateforme Signalement";
        String contenu = String.format("""
                Bonjour,

                Merci de votre inscription.

                Veuillez vérifier votre email via ce lien :
                %s

                Cordialement,
                Plateforme Signalement Municipal
                """, lienVerification);

        envoyerEmail(email, sujet, contenu);
    }

    public void envoyerEmailBienvenue(String email, String nom) {
        String sujet = "Bienvenue sur la Plateforme Signalement";
        String contenu = String.format("""
                Bonjour %s,

                Votre compte est maintenant actif.

                Connexion : %s/login

                Cordialement,
                Plateforme Signalement Municipal
                """, nom, frontendUrl);

        envoyerEmail(email, sujet, contenu);
    }

    // =========================================================
    //  NOUVEAU : INCIDENT - CHANGEMENT DE STATUT
    // =========================================================

    public void envoyerEmailChangementStatutIncident(
            Incident incident,
            StatutIncidentEnum nouveauStatut
    ) {
        try {
            Utilisateur citoyen = incident.getCitoyen();

            if (citoyen == null || citoyen.getEmail() == null) return;

            String sujet = "Mise à jour de votre signalement #" + incident.getId();

            String contenu = String.format("""
                    Bonjour %s,

                    Le statut de votre signalement a changé.

                     Incident : %s
                     Catégorie : %s
                     Nouveau statut : %s

                    Vous pouvez consulter les détails via votre espace citoyen.

                    Cordialement,
                    Plateforme Signalement Municipal
                    """,
                    citoyen.getNom(),
                    incident.getTitre(),
                    incident.getCategorie().getNom(),
                    formatStatut(nouveauStatut)
            );

            envoyerEmail(citoyen.getEmail(), sujet, contenu);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email incident", e);
        }
    }

    // =========================================================
    // UTILITAIRE EMAIL
    // =========================================================

    private void envoyerEmail(String destinataire, String sujet, String contenu) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(contenu);
        message.setFrom(emailFrom);
        mailSender.send(message);
        log.info("Email envoyé à {}", destinataire);
    }

    private String getRoleString(RoleEnum role) {
        return switch (role) {
            case ADMINISTRATEUR -> "Administrateur";
            case AGENT_MUNICIPAL -> "Agent Municipal";
            case CITOYEN -> "Citoyen";
        };
    }

    private String formatStatut(StatutIncidentEnum statut) {
        return switch (statut) {
            case EN_COURS_DE_CHARGE -> "Pris en charge";
            case EN_RESOLUTION -> "En cours de résolution";
            case RESOLU -> "Résolu";
            case CLOTURE -> "Clôturé";
            case SIGNALE -> "Signalé";
        };
    }

    public void envoyerEmailAssignationIncident(Incident incident, Utilisateur agent) {
        String sujet = "Nouvel incident assigné - #" + incident.getId();
        String message = """
        Bonjour %s,
        
        Un nouvel incident vous a été assigné :
        
        Titre : %s
        Catégorie : %s
        Quartier : %s
        Priorité : %s
        Localisation : %s
        
        Lien direct : http://localhost:8080/admin/incidents/%d
        
        Merci,
        L'administrateur
        """.formatted(
                agent.getNom(),
                incident.getTitre(),
                incident.getCategorie().getNom(),
                incident.getQuartier() != null ? incident.getQuartier().getNom() : "—",
                incident.getPriorite(),
                incident.getLocalisation(),
                incident.getId()
        );

        envoyerEmail(agent.getEmail(), sujet, message);
    }

}
