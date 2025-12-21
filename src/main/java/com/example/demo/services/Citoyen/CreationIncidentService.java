package com.example.demo.services.Citoyen;

import com.example.demo.models.Incident;
import com.example.demo.models.IncidentCluster;
import com.example.demo.repositories.IncidentClusterRepository;
import com.example.demo.repositories.IncidentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreationIncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentClusterRepository clusterRepository;

    // ~5 mètres en degrés (~0.000045°)
    private static final double DELTA = 0.000045;

    @Transactional
    public Incident creerIncident(Incident incident) {

        double lat = incident.getLatitude();
        double lng = incident.getLongitude();

        IncidentCluster cluster = clusterRepository
                .findClusterDansRayon(
                        incident.getCategorie(),
                        lat - DELTA,
                        lat + DELTA,
                        lng - DELTA,
                        lng + DELTA
                )
                .orElseGet(() -> creerCluster(incident));

        // rattacher l'incident au cluster
        incident.setCluster(cluster);

        // valeurs propres au citoyen
        incident.setDateSignalement(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    private IncidentCluster creerCluster(Incident incident) {
        IncidentCluster cluster = new IncidentCluster();
        cluster.setLatitude(incident.getLatitude());
        cluster.setLongitude(incident.getLongitude());
        cluster.setCategorie(incident.getCategorie());
        return clusterRepository.save(cluster);
    }
}
