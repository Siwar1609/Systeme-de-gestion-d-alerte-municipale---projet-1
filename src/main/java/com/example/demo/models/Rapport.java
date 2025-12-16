package com.example.demo.models;

import com.example.demo.models.enums.TypeRapportEnum;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport")
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    @Lob
    private String contenu;

    @Enumerated(EnumType.STRING)
    private TypeRapportEnum typeRapport;

    // Incident concerné
    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    // Administrateur qui génère le rapport
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Utilisateur admin;

    // -------- GETTERS & SETTERS --------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public TypeRapportEnum getTypeRapport() {
        return typeRapport;
    }

    public void setTypeRapport(TypeRapportEnum typeRapport) {
        this.typeRapport = typeRapport;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public Utilisateur getAdmin() {
        return admin;
    }

    public void setAdmin(Utilisateur admin) {
        this.admin = admin;
    }
}