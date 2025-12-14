package com.example.demo.services.quartier;

import com.example.demo.models.Quartier;
import com.example.demo.repositories.QuartierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuartierServiceImpl implements QuartierService {

    private final QuartierRepository quartierRepository;

    @Override
    public Quartier createQuartier(Quartier quartier) {
        return quartierRepository.save(quartier);
    }

    @Override
    public List<Quartier> getAllQuartiers() {
        return quartierRepository.findAll();
    }

    @Override
    public Quartier getQuartierById(Long id) {
        return quartierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quartier introuvable"));
    }

    @Override
    public void deleteQuartier(Long id) {
        if (!quartierRepository.existsById(id)) {
            throw new EntityNotFoundException("Quartier introuvable");
        }
        quartierRepository.deleteById(id);
    }
}