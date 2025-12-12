package com.example.demo.services.Citoyen.impl;

import com.example.demo.models.Quartier;
import com.example.demo.repositories.QuartierRepository;
import com.example.demo.services.Citoyen.QuartierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartierServiceImpl implements QuartierService {

    @Autowired
    private QuartierRepository quartierRepository;

    @Override
    public List<Quartier> findAll() {
        return quartierRepository.findAll();
    }
}
