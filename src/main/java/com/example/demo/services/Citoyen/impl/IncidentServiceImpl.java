package com.example.demo.services.Citoyen;

import com.example.demo.models.Incident;
import com.example.demo.repositories.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public void creerIncident(Incident incident, MultipartFile[] photos) {
        // (upload photos plus tard si besoin)
        incidentRepository.save(incident);
    }
}
