package com.example.demo.services.service;

import com.example.demo.models.MunicipalService;
import java.util.List;

public interface ServiceService {
    MunicipalService createService(MunicipalService service);
    List<MunicipalService> getAllServices();
    MunicipalService findById(Long id);
    void deleteService(Long id);
}
