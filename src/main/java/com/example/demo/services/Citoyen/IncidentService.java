package com.example.demo.services.Citoyen;

import com.example.demo.models.Incident;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IncidentService {
    void creerIncident(Incident incident, MultipartFile[] photos);
    List<Incident> findByCitoyenId(Long citoyenId);
    Incident findByIdAndCitoyen(Long incidentId, Long citoyenId);
    Incident mettreAJourIncident(Incident incident, MultipartFile[] nouvellesPhotos);
    void supprimerIncidentPourCitoyen(Long incidentId, Long citoyenId);

}
