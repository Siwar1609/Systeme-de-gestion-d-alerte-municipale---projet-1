package com.example.demo.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String zoneGeographique;


    @OneToMany(mappedBy = "service")
    private List<Utilisateur> agents = new ArrayList<>();

}
