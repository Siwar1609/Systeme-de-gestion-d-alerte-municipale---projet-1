package com.example.demo.config;

import com.example.demo.models.CategorieIncident;
import com.example.demo.repositories.CategorieIncidentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategorieIncidentRepository categorieIncidentRepository;

    public DataInitializer(CategorieIncidentRepository categorieIncidentRepository) {
        this.categorieIncidentRepository = categorieIncidentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Vérifie si les catégories existent déjà pour éviter les doublons
        if (categorieIncidentRepository.count() == 0) {
            CategorieIncident infra = new CategorieIncident();
            infra.setNom("Infrastructure");
            infra.setType("Infrastructure");
            infra.setDescription("Problèmes liés à la voirie, routes, bâtiments...");

            CategorieIncident proprete = new CategorieIncident();
            proprete.setNom("Propreté");
            proprete.setType("Propreté");
            proprete.setDescription("Problèmes de déchets, nettoyage des rues...");

            CategorieIncident espacesVerts = new CategorieIncident();
            espacesVerts.setNom("Espaces Verts");
            espacesVerts.setType("Espaces Verts");
            espacesVerts.setDescription("Problèmes dans les parcs, jardins, espaces verts...");

            CategorieIncident eclairage = new CategorieIncident();
            eclairage.setNom("Éclairage Public");
            eclairage.setType("Éclairage Public");
            eclairage.setDescription("Problèmes d’éclairage, lampadaires cassés...");

            categorieIncidentRepository.save(infra);
            categorieIncidentRepository.save(proprete);
            categorieIncidentRepository.save(espacesVerts);
            categorieIncidentRepository.save(eclairage);

            System.out.println("4 catégories d'incidents ajoutées !");
        }
    }
}
