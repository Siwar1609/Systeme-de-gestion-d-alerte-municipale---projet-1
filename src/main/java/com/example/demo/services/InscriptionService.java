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

    // ===============================
    // LOGIN CLASSIQUE
    // ===============================

    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email.toLowerCase().trim());
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<Utilisateur> authentifierUtilisateur(String email, String motDePasse) {
        Optional<Utilisateur> utilisateurOpt = findByEmail(email);
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            if (verifyPassword(motDePasse, utilisateur.getMotDePasse())) {
                return Optional.of(utilisateur);
            }
        }
        return Optional.empty();
    }

    public boolean peutSeConnecter(Utilisateur utilisateur) {
        if (utilisateur.getRole() == RoleEnum.CITOYEN) {
            return utilisateur.isCompteActive();
        }
        return true; // Agents et Admins peuvent se connecter directement
    }

    // ===============================
    // INSCRIPTION CITOYEN
    // ===============================

    @Transactional
    public Utilisateur inscrireCitoyen(String email, String nom, String motDePasse) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        Utilisateur citoyen = new Utilisateur();
        citoyen.setEmail(email.toLowerCase().trim());
        citoyen.setNom(nom.trim());
        citoyen.setMotDePasse(passwordEncoder.encode(motDePasse));
        citoyen.setRole(RoleEnum.CITOYEN);

        // Générer token de vérification
        String tokenVerification = genererTokenVerification();
        citoyen.setTokenVerification(tokenVerification);
        citoyen.setDateExpirationToken(LocalDateTime.now().plusHours(24));
        citoyen.setCompteActive(false);

        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(citoyen);

        // Envoyer email de vérification
        try {
            emailService.envoyerEmailVerification(email, tokenVerification);
            log.info("Email de vérification envoyé avec succès à : {}", email);
        } catch (Exception e) {
            log.warn("Email non envoyé mais utilisateur créé. Token: {}", tokenVerification);
            log.info("🔗 Pour tester: http://localhost:8080/api/citoyens/verifier-email?token={}", tokenVerification);
        }

        return utilisateurSauvegarde;
    }

    @Transactional
    public boolean verifierEmail(String token) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByTokenVerification(token);
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            if (utilisateur.getDateExpirationToken().isAfter(LocalDateTime.now())) {
                utilisateur.setCompteActive(true);
                utilisateur.setTokenVerification(null);
                utilisateur.setDateExpirationToken(null);
                utilisateurRepository.save(utilisateur);

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

        String nouveauToken = genererTokenVerification();
        utilisateur.setTokenVerification(nouveauToken);
        utilisateur.setDateExpirationToken(LocalDateTime.now().plusHours(24));
        utilisateurRepository.save(utilisateur);

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

    // ===============================
    // OAUTH2 GOOGLE (optionnel)
    // ===============================

    @Transactional
    public Utilisateur loginOuCreerViaGoogle(String email, String nom) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email.toLowerCase().trim());

        // Si l'utilisateur existe → login
        if (utilisateurOpt.isPresent()) {
            return utilisateurOpt.get();
        }

        // Sinon → création automatique (citoyen)
        Utilisateur citoyen = new Utilisateur();
        citoyen.setEmail(email.toLowerCase().trim());
        citoyen.setNom(nom);
        citoyen.setRole(RoleEnum.CITOYEN);
        citoyen.setCompteActive(true); // Google a déjà vérifié l'email
        citoyen.setMotDePasse(null);   // Pas de mot de passe local

        return utilisateurRepository.save(citoyen);
    }
}
