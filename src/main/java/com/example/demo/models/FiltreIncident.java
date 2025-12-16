package com.example.demo.models;

import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.repositories.IncidentRepository;
import jakarta.persistence.criteria.Join;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FiltreIncident {

    private String statut;
    private String localisation;
    private String categorieNom;
    private String quartierNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String titre;

    public Specification<Incident> toSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Filtre par statut
            if (statut != null && !statut.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("statut"),
                        StatutIncidentEnum.valueOf(statut.toUpperCase())
                ));
            }

            // Filtre par localisation
            if (localisation != null && !localisation.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("localisation")),
                        "%" + localisation.toLowerCase() + "%"
                ));
            }

            // Filtre par titre
            if (titre != null && !titre.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("titre")),
                        "%" + titre.toLowerCase() + "%"
                ));
            }

            // Filtre par nom de catégorie
            if (categorieNom != null && !categorieNom.trim().isEmpty()) {
                Join<Incident, CategorieIncident> categorieJoin = root.join("categorie");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(categorieJoin.get("nom")),
                        "%" + categorieNom.toLowerCase() + "%"
                ));
            }

            // Filtre par nom de quartier
            if (quartierNom != null && !quartierNom.trim().isEmpty()) {
                Join<Incident, Quartier> quartierJoin = root.join("quartier");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(quartierJoin.get("nom")),
                        "%" + quartierNom.toLowerCase() + "%"
                ));
            }

            // Filtre par date début
            if (dateDebut != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateSignalement").as(LocalDateTime.class),
                        dateDebut.atStartOfDay()
                ));
            }

            // Filtre par date fin
            if (dateFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateSignalement").as(LocalDateTime.class),
                        dateFin.atTime(23, 59, 59)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
