package com.example.demo.services.quartier;

import com.example.demo.models.Quartier;

import java.util.List;

public interface QuartierService {

    Quartier createQuartier(Quartier quartier);

    List<Quartier> getAllQuartiers();

    Quartier getQuartierById(Long id);

    void deleteQuartier(Long id);
}