package com.example.demo.controllers.admin;

import com.example.demo.models.Incident;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.models.enums.TypeRapportEnum;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.services.rapport.RapportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RapportController {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private RapportService rapportService;

    @PostMapping("/admin/incidents/genererRapport")
    public void genererRapport(
            @RequestParam Long incidentId,
            @RequestParam TypeRapportEnum typeRapport,
            @AuthenticationPrincipal Utilisateur admin,
            HttpServletResponse response
    ) throws Exception {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        // ✅ RÈGLE MÉTIER FINALE
        if (incident.getStatut() != StatutIncidentEnum.CLOTURE ||
                incident.getFeedbackCitoyen() == null ||
                incident.getFeedbackCitoyen().isEmpty()) {

            throw new RuntimeException(
                    "Rapport autorisé uniquement pour incident clôturé avec feedback"
            );
        }

        rapportService.genererRapportPourIncident(
                incident,
                typeRapport,
                admin,
                response
        );
    }
}
