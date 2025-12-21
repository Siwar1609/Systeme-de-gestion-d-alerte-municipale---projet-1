package com.example.demo.repositories;

import com.example.demo.models.Incident;
import com.example.demo.models.enums.StatutIncidentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    // ====================== Citoyen ======================

    List<Incident> findByCitoyenIdOrderByDateSignalementDesc(Long citoyenId);

    Optional<Incident> findByIdAndCitoyenId(Long id, Long citoyenId);

    void deleteByIdAndCitoyenId(Long id, Long citoyenId);

    boolean existsByIdAndCitoyenId(Long id, Long citoyenId);

    // ====================== Agent ======================

    List<Incident> findByAgentId(Long agentId);

    Optional<Incident> findByIdAndAgentId(Long id, Long agentId);

    // ====================== Rapports / Statistiques ======================

    // Incidents clôturés avec feedback
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

    // Nombre d'incidents par catégorie
    @Query("""
        SELECT i.categorie.nom, COUNT(i)
        FROM Incident i
        GROUP BY i.categorie.nom
        ORDER BY COUNT(i) DESC
    """)
    List<Object[]> countIncidentsParCategorie();

    // Nombre d'incidents par quartier
    @Query("""
        SELECT i.quartier.nom, COUNT(i)
        FROM Incident i
        GROUP BY i.quartier.nom
        ORDER BY COUNT(i) DESC
    """)
    List<Object[]> countIncidentsParQuartier();

    // Nombre d'incidents par cluster et par catégorie
    @Query("""
        SELECT i.cluster.id, i.categorie.nom, COUNT(i)
        FROM Incident i
        GROUP BY i.cluster.id, i.categorie.nom
    """)
    List<Object[]> countIncidentsParCategorieParCluster();
}
