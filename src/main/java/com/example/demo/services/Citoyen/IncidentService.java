package com.example.demo.services.Citoyen;

import com.example.demo.models.Incident;
import org.springframework.web.multipart.MultipartFile;

public interface IncidentService {
    void creerIncident(Incident incident, MultipartFile[] photos);
}
