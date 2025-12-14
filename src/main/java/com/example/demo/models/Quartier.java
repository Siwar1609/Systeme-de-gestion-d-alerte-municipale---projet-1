package com.example.demo.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quartier")
public class Quartier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
    }

    public List<Utilisateur> getCitoyens() {
        return citoyens;
    }

    public void setCitoyens(List<Utilisateur> citoyens) {
        this.citoyens = citoyens;
    }

    @OneToMany(mappedBy = "quartier")
    private List<Incident> incidents = new ArrayList<>();
    @OneToMany(mappedBy = "quartier")
    private List<Utilisateur> citoyens = new ArrayList<>();

    // -------- GETTERS & SETTERS --------
}