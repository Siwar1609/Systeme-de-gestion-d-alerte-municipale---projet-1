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

    private Double latitude;
    private Double longitude;

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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public StatutIncidentEnum getStatut() {
        return statut;
    }

    public void setStatut(StatutIncidentEnum statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(LocalDateTime dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public LocalDateTime getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDateTime dateCloture) {
        this.dateCloture = dateCloture;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public Utilisateur getCitoyen() {
        return citoyen;
    }

    public void setCitoyen(Utilisateur citoyen) {
        this.citoyen = citoyen;
    }

    public Utilisateur getAgent() {
        return agent;
    }

    public void setAgent(Utilisateur agent) {
        this.agent = agent;
    }

    public CategorieIncident getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieIncident categorie) {
        this.categorie = categorie;
    }

    public Quartier getQuartier() {
        return quartier;
    }

    public void setQuartier(Quartier quartier) {
        this.quartier = quartier;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
    @Transient
    private Long categorieId;

    @Transient
    private Long quartierId;

    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }

    public Long getQuartierId() { return quartierId; }
    public void setQuartierId(Long quartierId) { this.quartierId = quartierId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
