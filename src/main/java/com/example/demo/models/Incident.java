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
    private String localisation;

    @Enumerated(EnumType.STRING)
    private StatutIncidentEnum statut;

    private LocalDateTime dateSignalement;

    // ================= RELATIONS =================

    // Citoyen qui signale
    @ManyToOne(optional = false)
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    // Agent municipal assigné
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Utilisateur agent;

    // Catégorie
    @ManyToOne(optional = false)
    @JoinColumn(name = "categorie_id")
    private CategorieIncident categorie;

    // Quartier
    @ManyToOne(optional = false)
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;

    // Photos
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Photo> photos = new ArrayList<>();

    // ================= CHAMPS TRANSIENT =================

    @Transient
    private Long categorieId;

    @Transient
    private Long quartierId;

    // ================= GETTERS & SETTERS =================

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

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
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

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public Long getQuartierId() {
        return quartierId;
    }

    public void setQuartierId(Long quartierId) {
        this.quartierId = quartierId;
    }
}
