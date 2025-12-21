package com.example.demo.repositories;

import com.example.demo.models.IncidentCluster;
import com.example.demo.models.CategorieIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IncidentClusterRepository extends JpaRepository<IncidentCluster, Long> {

    /**
     * Recherche exacte : même latitude, longitude et catégorie
     */
    Optional<IncidentCluster> findByLatitudeAndLongitudeAndCategorie(
            Double latitude, Double longitude, CategorieIncident categorie
    );

    /**
     * Recherche d’un cluster existant dans un rayon GPS (~5 mètres)
     * même catégorie + latitude/longitude dans un intervalle
     */
    @Query("""
        SELECT c
        FROM IncidentCluster c
        WHERE c.categorie = :categorie
          AND c.latitude BETWEEN :latMin AND :latMax
          AND c.longitude BETWEEN :lngMin AND :lngMax
    """)
    Optional<IncidentCluster> findClusterDansRayon(
            @Param("categorie") CategorieIncident categorie,
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lngMin") Double lngMin,
            @Param("lngMax") Double lngMax
    );

    /**
     * Optionnel : méthode utilitaire pour trouver un cluster autour d’une coordonnée
     * avec ±5 mètres (approximation en degrés)
     */
    default Optional<IncidentCluster> findClusterProche(
            CategorieIncident categorie,
            Double latitude,
            Double longitude
    ) {
        // Approximation : 1 mètre ≈ 0.000009 degrés
        double delta = 0.000045; // ~5 mètres
        return findClusterDansRayon(
                categorie,
                latitude - delta,
                latitude + delta,
                longitude - delta,
                longitude + delta
        );
    }
}
