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

    @Column(length = 2000)
    private String description;

    private String localisation;

    private LocalDateTime dateSignalement;

    @Enumerated(EnumType.STRING)
    private StatutIncidentEnum statut;

    // RELATIONS
    @ManyToOne(optional = false)
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Utilisateur agent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categorie_id")
    private CategorieIncident categorie;

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
