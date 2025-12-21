package com.example.demo.services;

import com.example.demo.models.Incident;
import com.example.demo.repositories.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatistiquesService {

    private final IncidentRepository incidentRepository;

    /**
     * Compte les incidents par catégorie en utilisant les clusters
     */
    public Map<String, Long> incidentsParCategorie() {
        // La requête doit être adaptée pour utiliser la relation Incident -> Cluster -> Categorie
        List<Object[]> results = incidentRepository.countIncidentsParCategorieParCluster();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    /**
     * Compte les incidents par quartier (inchangé)
     */
    public Map<String, Long> incidentsParQuartier() {
        List<Object[]> results = incidentRepository.countIncidentsParQuartier();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }
}
