package com.example.demo.services.Citoyen.impl;

import com.example.demo.models.Incident;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.services.Citoyen.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public void creerIncident(Incident incident, MultipartFile[] photos) {
        incidentRepository.save(incident);
        // tu ajouteras la logique des photos plus tard
    }
}


