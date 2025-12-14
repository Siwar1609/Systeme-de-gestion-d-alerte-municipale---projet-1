package com.example.demo.services.service;

import com.example.demo.models.MunicipalService;
import com.example.demo.repositories.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    @Override
    public MunicipalService createService(MunicipalService service) {
        return serviceRepository.save(service);
    }

    @Override
    public List<MunicipalService> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    public MunicipalService findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service introuvable"));
    }

    @Override
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new EntityNotFoundException("Service introuvable");
        }
        serviceRepository.deleteById(id);
    }
}
