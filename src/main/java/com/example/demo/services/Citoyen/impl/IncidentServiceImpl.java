package com.example.demo.services.Citoyen.impl;

import com.example.demo.models.CategorieIncident;
import com.example.demo.models.Incident;
import com.example.demo.models.Quartier;
import com.example.demo.repositories.CategorieIncidentRepository;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.QuartierRepository;
import com.example.demo.services.Citoyen.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final CategorieIncidentRepository categorieRepository;
    private final QuartierRepository quartierRepository;

    @Override
    public void creerIncident(Incident incident, MultipartFile[] photos) {

        // Récupération de la catégorie et du quartier via les IDs
        CategorieIncident categorie = categorieRepository.findById(incident.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

        Quartier quartier = quartierRepository.findById(incident.getQuartierId())
                .orElseThrow(() -> new RuntimeException("Quartier non trouvé"));

        incident.setCategorie(categorie);
        incident.setQuartier(quartier);

        // Sauvegarde dans la base
        incidentRepository.save(incident);

        // TODO : Gérer l’upload des photos si nécessaire
    }
}
