package com.example.demo.repositories;

import com.example.demo.models.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<MunicipalService, Long> {
}
