package com.example.demo.repositories;

import com.example.demo.models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.example.demo.models.enums.StatutIncidentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {
    List<Incident> findByCitoyenIdOrderByDateSignalementDesc(Long citoyenId);

    Optional<Incident> findByIdAndCitoyenId(Long id, Long citoyenId);
    void deleteByIdAndCitoyenId(Long id, Long citoyenId);
    boolean existsByIdAndCitoyenId(Long id, Long citoyenId);

  //agent

    // Incidents assignés à un agent
    List<Incident> findByAgentId(Long agentId);
    Optional<Incident> findByIdAndAgentId(Long id, Long agentId);

 // rapports

    // Incidents clôturés avec feedback (rapport autorisé)
    @Query("""
        SELECT i
        FROM Incident i
        WHERE i.statut = :statut
          AND i.feedbackCitoyen IS NOT NULL
          AND i.feedbackCitoyen <> ''
    """)
    List<Incident> findIncidentsCloturesAvecFeedback(
            @Param("statut") StatutIncidentEnum statut
    );


    //  Nombre d'incidents par catégorie
    @Query("""
        SELECT i.categorie.nom, COUNT(i)
        FROM Incident i
        GROUP BY i.categorie.nom
    """)
    List<Object[]> countIncidentsParCategorie();


    @Query("""
        SELECT i.quartier.nom, COUNT(i)
        FROM Incident i
        GROUP BY i.quartier.nom
    """)
    List<Object[]> countIncidentsParQuartier();



}
