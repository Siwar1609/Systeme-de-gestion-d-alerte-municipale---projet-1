package com.example.demo.models;

import com.example.demo.models.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;

    @Column(nullable = false)
    private String motDePasse;

    // CHAMPS POUR LA VÉRIFICATION D'EMAIL
    private String tokenVerification;

    private LocalDateTime dateExpirationToken;

    private boolean compteActive = false;
    @ManyToOne
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;  // Si role = CITOYEN

    @ManyToOne
    @JoinColumn(name = "service_id")
    private MunicipalService service;  // Si role = AGENT

    // RELATIONS JPA
    @OneToMany(mappedBy = "citoyen", cascade = CascadeType.ALL)
    private List<Incident> incidentsSignales = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<Incident> incidentsAssignes = new ArrayList<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Rapport> rapports = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();
}