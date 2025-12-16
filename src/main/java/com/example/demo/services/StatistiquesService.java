package com.example.demo.services;

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

    // Retourne un Map<CategoryName, Count>
    public Map<String, Long> incidentsParCategorie() {
        List<Object[]> results = incidentRepository.countIncidentsParCategorie();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    // Retourne un Map<QuartierName, Count>
    public Map<String, Long> incidentsParQuartier() {
        List<Object[]> results = incidentRepository.countIncidentsParQuartier();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }
}
