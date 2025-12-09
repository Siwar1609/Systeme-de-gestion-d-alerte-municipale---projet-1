package com.example.demo.models;

import com.example.demo.models.enums.StatutIncidentEnum;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Enumerated(EnumType.STRING)
    private StatutIncidentEnum statut;

    private LocalDateTime dateSignalement;

    private LocalDateTime dateCloture;

    private String localisation;

    // -------- RELATIONS --------

    // Citoyen qui signale l'incident
    @ManyToOne
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    // Agent municipal assigné
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Utilisateur agent;

    // Catégorie d’incident
    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private CategorieIncident categorie;

    // Quartier
    @ManyToOne
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;

    // Photos associées
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Photo> photos = new ArrayList<>();

    // -------- GETTERS & SETTERS --------
    // (générer automatiquement)
}
