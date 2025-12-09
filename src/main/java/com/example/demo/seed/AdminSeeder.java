package com.example.demo.seed;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "siwarlab34@gmail.com";

        // Vérifier si l'admin existe déjà
        if (!utilisateurRepository.existsByEmail(adminEmail)) {
            Utilisateur admin = new Utilisateur();
            admin.setEmail(adminEmail);
            admin.setNom("Administrateur Principal");
            admin.setMotDePasse(passwordEncoder.encode("Admin123!"));
            admin.setRole(RoleEnum.ADMINISTRATEUR);
            admin.setCompteActive(true);
            admin.setTokenVerification(null);

            utilisateurRepository.save(admin);
            log.info("✅ ADMIN initial créé avec succès !");
            log.info("📧 Email: {}", adminEmail);
            log.info("🔑 Mot de passe: Admin123!");
        } else {
            log.info("✅ ADMIN déjà présent dans la base de données");
        }
    }
}