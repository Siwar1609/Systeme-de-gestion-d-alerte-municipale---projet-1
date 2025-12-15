package com.example.demo.models;

import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.models.enums.PrioriteIncidentEnum; // <-- nouvel import
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PrioriteIncidentEnum priorite;

    private Double latitude;
    private Double longitude;

    @Transient
    private Long categorieId;

    @Transient
    private Long quartierId;

    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }

    public Long getQuartierId() { return quartierId; }
    public void setQuartierId(Long quartierId) { this.quartierId = quartierId; }

    // RELATIONS
    @ManyToOne
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

    private String nomsPhotos;

    public Utilisateur getCitoyen() { return citoyen; }
    public void setCitoyen(Utilisateur citoyen) { this.citoyen = citoyen; }


    public PrioriteIncidentEnum getPriorite() { return priorite; }
    public void setPriorite(PrioriteIncidentEnum priorite) { this.priorite = priorite; }
}
