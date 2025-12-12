package com.example.demo.repositories;

import com.example.demo.models.Quartier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuartierRepository extends JpaRepository<Quartier, Long> {
}
