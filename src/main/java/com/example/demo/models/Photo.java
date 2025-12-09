package com.example.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateUpload;

    private String cheminFichier;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    // -------- GETTERS & SETTERS --------
}
