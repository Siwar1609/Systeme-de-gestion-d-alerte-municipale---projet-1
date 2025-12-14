package com.example.demo.models;

import com.example.demo.models.enums.StatutIncidentEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    private StatutIncidentEnum statut;
    private LocalDateTime dateSignalement;

    private LocalDateTime dateCloture;

    private String localisation;

    private Double latitude;
    private Double longitude;


    // -------- RELATIONS --------

    // Citoyen qui signale l'incident
    @ManyToOne(optional = false)
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    // Agent municipal assigné
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Utilisateur agent;

    // Catégorie d’incident
    @ManyToOne(optional = false)
    @JoinColumn(name = "categorie_id")
    private CategorieIncident categorie;

    // Quartier
    @ManyToOne(optional = false)
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;

    // Photos
    @ElementCollection
    @CollectionTable(name = "incident_photos", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "photo_path")
    private List<String> photos = new ArrayList<>();

    public void addPhoto(String photoPath) {
        this.photos.add(photoPath);
    }

}