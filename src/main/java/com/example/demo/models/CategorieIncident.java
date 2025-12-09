package com.example.demo.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie_incident")
public class CategorieIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

    // Liste des incidents
    @OneToMany(mappedBy = "categorie")
    private List<Incident> incidents = new ArrayList<>();

    // Type : Infrastructure, Propreté, etc.
    private String type;

    // -------- GETTERS & SETTERS --------
}
