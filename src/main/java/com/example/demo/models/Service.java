package com.example.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String zoneGeographique;

    // -------- GETTERS & SETTERS --------
}
