package com.example.demo.repositories;


import com.example.demo.models.Rapport;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.TypeRapportEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface RapportRepository extends JpaRepository<Rapport, Long> {


    List<Rapport> findByIncidentId(Long incidentId);


    List<Rapport> findByAdmin(Utilisateur admin);


    List<Rapport> findByTypeRapport(TypeRapportEnum typeRapport);


    Rapport findTopByIncidentIdOrderByDateDesc(Long incidentId);
}