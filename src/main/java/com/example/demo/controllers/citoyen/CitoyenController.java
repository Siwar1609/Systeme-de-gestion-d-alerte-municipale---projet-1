package com.example.demo.controllers.citoyen;

import com.example.demo.models.*;
import com.example.demo.services.Citoyen.IncidentService;
import com.example.demo.services.Citoyen.CategorieIncidentService;
import com.example.demo.services.quartier.QuartierService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/citoyens")
public class CitoyenController {

    @Autowired private IncidentService incidentService;
    @Autowired private CategorieIncidentService categorieService;
    @Autowired private QuartierService quartierService;

    @Autowired
    private com.example.demo.repositories.CategorieIncidentRepository categorieIncidentRepository;

    @Autowired
    private com.example.demo.repositories.QuartierRepository quartierRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "Tableau de bord Citoyen");
        model.addAttribute("userNom", session.getAttribute("userNom"));
        return "citoyens/dashboard";
    }

    @GetMapping("/incidents/nouveau")
    public String nouveauIncident(Model model, HttpSession session) {
        Incident incident = new Incident();
        incident.setDateSignalement(java.time.LocalDateTime.now());
        incident.setStatut(com.example.demo.models.enums.StatutIncidentEnum.SIGNALE);

        model.addAttribute("incident", incident);
        model.addAttribute("categories", categorieService.findAll());
        model.addAttribute("quartiers", quartierService.getAllQuartiers());
        model.addAttribute("userNom", session.getAttribute("userNom"));
        return "citoyens/incident-form";
    }

    @PostMapping("/incidents/creer")
    public String creerIncident(
            @Valid @ModelAttribute("incident") Incident incident,
            BindingResult result,
            @RequestParam("photos") MultipartFile[] photos,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categorieService.findAll());
            model.addAttribute("quartiers", quartierService.getAllQuartiers());
            return "citoyens/incident-form";
        }

        // Récupérer le citoyen depuis la session
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        incident.setCitoyen(citoyen);

        // Récupérer les entités Categorie et Quartier
        CategorieIncident categorie = categorieIncidentRepository
                .findById(incident.getCategorie().getId())
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
        incident.setCategorie(categorie);

        Quartier quartier = quartierRepository
                .findById(incident.getQuartier().getId())
                .orElseThrow(() -> new RuntimeException("Quartier introuvable"));
        incident.setQuartier(quartier);

        // Sauvegarde via le service
        incidentService.creerIncident(incident, photos);

        redirectAttributes.addFlashAttribute("message", "Incident signalé avec succès !");
        return "redirect:/citoyens/dashboard";
    }
}
