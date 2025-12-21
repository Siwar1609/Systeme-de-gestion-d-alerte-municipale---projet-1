package com.example.demo.models;

import com.example.demo.models.CategorieIncident;
import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.PrioriteIncidentEnum;
import com.example.demo.models.enums.StatutIncidentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "incident_cluster",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"latitude", "longitude", "categorie_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categorie_id")
    private CategorieIncident categorie;

    @Enumerated(EnumType.STRING)
    private StatutIncidentEnum statut = StatutIncidentEnum.SIGNALE;

    @Enumerated(EnumType.STRING)
    private PrioriteIncidentEnum priorite;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Utilisateur agent;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "cluster")
    private List<Incident> incidents = new ArrayList<>();
}
