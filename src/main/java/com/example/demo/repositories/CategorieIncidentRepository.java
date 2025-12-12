package com.example.demo.repositories;

import com.example.demo.models.CategorieIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieIncidentRepository extends JpaRepository<CategorieIncident, Long> {
}
