package com.example.demo.services.Citoyen.impl;
import com.example.demo.models.CategorieIncident;
import com.example.demo.repositories.CategorieIncidentRepository;
import com.example.demo.services.Citoyen.CategorieIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieIncidentServiceImpl implements CategorieIncidentService {

    @Autowired
    private CategorieIncidentRepository categorieIncidentRepository;

    @Override
    public List<CategorieIncident> findAll() {
        return categorieIncidentRepository.findAll();
    }
}
