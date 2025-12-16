package com.example.demo.repositories;

import com.example.demo.models.Incident;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {
    List<Incident> findByCitoyenIdOrderByDateSignalementDesc(Long citoyenId);
    List<Incident> findByAgentId(Long agentId);
    Optional<Incident> findByIdAndCitoyenId(Long id, Long citoyenId);
    void deleteByIdAndCitoyenId(Long id, Long citoyenId);
    boolean existsByIdAndCitoyenId(Long id, Long citoyenId);
    Optional<Incident> findByIdAndAgentId(Long id, Long agentId);

}

