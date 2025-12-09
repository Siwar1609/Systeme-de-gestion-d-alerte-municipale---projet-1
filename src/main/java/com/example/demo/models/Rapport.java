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
}
