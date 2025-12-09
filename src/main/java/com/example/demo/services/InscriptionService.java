package com.example.demo.services;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ... VOS MÉTHODES EXISTANTES ...

    /**
     * Trouver un utilisateur par email
     */
    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email.toLowerCase().trim());
    }

    /**
     * Vérifier le mot de passe
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Authentifier un utilisateur (pour le login)
     */
    public Optional<Utilisateur> authentifierUtilisateur(String email, String motDePasse) {
        Optional<Utilisateur> utilisateurOpt = findByEmail(email);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();

            // Vérifier le mot de passe
            if (verifyPassword(motDePasse, utilisateur.getMotDePasse())) {
                return Optional.of(utilisateur);
            }
        }

        return Optional.empty();
    }

    /**
     * Vérifier si un utilisateur peut se connecter (compte activé, etc.)
     */
    public boolean peutSeConnecter(Utilisateur utilisateur) {
        // Pour les citoyens, vérifier que le compte est activé
        if (utilisateur.getRole() == RoleEnum.CITOYEN) {
            return utilisateur.isCompteActive();
        }

        // Pour les autres rôles (agent, admin), permettre la connexion directement
        return true;
    }

    // ... VOS AUTRES MÉTHODES EXISTANTES ...

    @Transactional
    public Utilisateur inscrireCitoyen(String email, String nom, String motDePasse) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        // Créer le nouvel utilisateur
        Utilisateur citoyen = new Utilisateur();
        citoyen.setEmail(email.toLowerCase().trim());
        citoyen.setNom(nom.trim());
        citoyen.setMotDePasse(passwordEncoder.encode(motDePasse));
        citoyen.setRole(RoleEnum.CITOYEN);

        // Générer le token de vérification
        String tokenVerification = genererTokenVerification();
        citoyen.setTokenVerification(tokenVerification);
        citoyen.setDateExpirationToken(LocalDateTime.now().plusHours(24));
        citoyen.setCompteActive(false);

        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(citoyen);

        //  CORRECTION : Gestion d'erreur pour l'email
        try {
            emailService.envoyerEmailVerification(email, tokenVerification);
            log.info(" Email de vérification envoyé avec succès à : {}", email);
        } catch (Exception e) {
            // On log l'erreur mais on ne bloque pas l'inscription
            log.warn("️ Email non envoyé mais utilisateur créé. Token: {}", tokenVerification);
            log.info("🔗 Pour tester: http://localhost:8080/api/citoyens/verifier-email?token={}", tokenVerification);
            // On ne throw pas d'exception pour permettre le test
        }

        return utilisateurSauvegarde;
    }

    @Transactional
    public boolean verifierEmail(String token) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByTokenVerification(token);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();

            // Vérifier si le token n'a pas expiré
            if (utilisateur.getDateExpirationToken().isAfter(LocalDateTime.now())) {
                utilisateur.setCompteActive(true);
                utilisateur.setTokenVerification(null);
                utilisateur.setDateExpirationToken(null);
                utilisateurRepository.save(utilisateur);

                // Envoyer l'email de bienvenue
                try {
                    emailService.envoyerEmailBienvenue(utilisateur.getEmail(), utilisateur.getNom());
                } catch (Exception e) {
                    log.warn("Email de bienvenue non envoyé, mais compte activé");
                }

                return true;
            }
        }
        return false;
    }

    @Transactional
    public void renvoyerEmailVerification(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);

        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Aucun compte trouvé avec cet email");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        if (utilisateur.isCompteActive()) {
            throw new RuntimeException("Ce compte est déjà vérifié");
        }

        // Générer un nouveau token
        String nouveauToken = genererTokenVerification();
        utilisateur.setTokenVerification(nouveauToken);
        utilisateur.setDateExpirationToken(LocalDateTime.now().plusHours(24));
        utilisateurRepository.save(utilisateur);

        // Renvoyer l'email
        try {
            emailService.envoyerEmailVerification(email, nouveauToken);
        } catch (Exception e) {
            log.warn("Email de vérification non renvoyé, mais nouveau token généré: {}", nouveauToken);
            throw new RuntimeException("Erreur lors de l'envoi de l'email, mais nouveau token généré: " + nouveauToken);
        }
    }

    public boolean verifierEmailExiste(String email) {
        return utilisateurRepository.existsByEmail(email.toLowerCase().trim());
    }

    private String genererTokenVerification() {
        return UUID.randomUUID().toString();
    }
}