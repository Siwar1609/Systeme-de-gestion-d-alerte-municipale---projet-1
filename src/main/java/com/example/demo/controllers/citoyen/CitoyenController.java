package com.example.demo.controllers.citoyen;

import com.example.demo.models.*;
import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.repositories.UtilisateurRepository;
import com.example.demo.services.Citoyen.IncidentService;
import com.example.demo.services.Citoyen.CategorieIncidentService;
import com.example.demo.services.incidentworkflow.IncidentWorkFlowService;
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
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private IncidentWorkFlowService incidentWorkflowService;

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
        System.out.println("Errors ? " + result.hasErrors());
        result.getAllErrors().forEach(e -> System.out.println(e));
        if (result.hasErrors()) {
            model.addAttribute("categories", categorieService.findAll());
            model.addAttribute("quartiers", quartierService.getAllQuartiers());
            return "citoyens/incident-form";
        }
        System.out.println("creerIncident appelé");

        // Récupérer le citoyen depuis la session
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            System.out.println("Aucun utilisateur dans la session !");
            return "redirect:/login";
        }
        System.out.println("Citoyen dans contrôleur = " + citoyen.getId());
        incident.setCitoyen(citoyen);
        incident.setCitoyen(citoyen);

        // 2) Récupérer la catégorie et le quartier à partir des IDs bindés
        if (incident.getCategorieId() != null) {
            CategorieIncident categorie = categorieIncidentRepository
                    .findById(incident.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            incident.setCategorie(categorie);
        }

        if (incident.getQuartierId() != null) {
            Quartier quartier = quartierRepository
                    .findById(incident.getQuartierId())
                    .orElseThrow(() -> new RuntimeException("Quartier introuvable"));
            incident.setQuartier(quartier);
        }

        System.out.println("Citoyen dans contrôleur = " +
                (incident.getCitoyen() != null ? incident.getCitoyen().getId() : "null"));
        // Sauvegarde via le service
        incidentService.creerIncident(incident, photos);

        redirectAttributes.addFlashAttribute("message", "Incident signalé avec succès !");
        return "redirect:/citoyens/dashboard";
    }
    @GetMapping("/incidents/mes-signalements")
    public String mesSignalements(Model model, HttpSession session) {
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Mes signalements");
        model.addAttribute("userNom", session.getAttribute("userNom"));

        // récupérer les incidents du citoyen connecté
        var incidents = incidentService.findByCitoyenId(citoyen.getId());
        model.addAttribute("incidents", incidents);

        return "citoyens/mes-signalements";
    }
    @GetMapping("/incidents/{id}/modifier")
    public String afficherFormModification(@PathVariable Long id,
                                           HttpSession session,
                                           Model model) {
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        Incident incident = incidentService.findByIdAndCitoyen(id, citoyen.getId());

        model.addAttribute("incident", incident);
        model.addAttribute("categories", categorieService.findAll());
        model.addAttribute("quartiers", quartierService.getAllQuartiers());
        model.addAttribute("pageTitle", "Modifier un incident");

        return "citoyens/incident-edit-form";
    }

    @PostMapping("/incidents/{id}/modifier")
    public String modifierIncident(@PathVariable Long id,
                                   @Valid @ModelAttribute("incident") Incident incident,
                                   BindingResult result,
                                   @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        // sécuriser : forcer l'id et le citoyen
        incident.setId(id);
        incident.setCitoyen(citoyen);

        if (result.hasErrors()) {
            model.addAttribute("categories", categorieService.findAll());
            model.addAttribute("quartiers", quartierService.getAllQuartiers());
            model.addAttribute("pageTitle", "Modifier un incident");
            return "citoyens/incident-edit-form";
        }

        // rebind catégorie et quartier à partir des IDs comme pour la création
        if (incident.getCategorieId() != null) {
            CategorieIncident cat = categorieIncidentRepository
                    .findById(incident.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            incident.setCategorie(cat);
        }
        if (incident.getQuartierId() != null) {
            Quartier q = quartierRepository
                    .findById(incident.getQuartierId())
                    .orElseThrow(() -> new RuntimeException("Quartier introuvable"));
            incident.setQuartier(q);
        }

        incidentService.mettreAJourIncident(incident, photos);

        redirectAttributes.addFlashAttribute("success",
                "Incident mis à jour avec succès");
        return "redirect:/citoyens/incidents/mes-signalements";
    }
    @PostMapping("/incidents/{id}/supprimer")
    public String supprimerIncident(@PathVariable Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        try {
            incidentService.supprimerIncidentPourCitoyen(id, citoyen.getId());
            redirectAttributes.addFlashAttribute("success",
                    "Incident supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer cet incident.");
        }

        return "redirect:/citoyens/incidents/mes-signalements";
    }
    @GetMapping("/profil")
    public String afficherProfil(Model model, HttpSession session) {
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Mon profil");
        model.addAttribute("utilisateur", citoyen);
        model.addAttribute("userNom", session.getAttribute("userNom"));
        return "citoyens/profil";
    }
    @GetMapping("/profil/modifier")
    public String afficherFormProfil(Model model, HttpSession session) {
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Modifier mon profil");
        model.addAttribute("utilisateur", citoyen);
        return "citoyens/profil-edit";
    }

    @PostMapping("profil/modifier")
    public String modifierProfil(@ModelAttribute("utilisateur") Utilisateur formUser,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        // sécurité : forcer l'id et le rôle depuis la session
        formUser.setId(citoyen.getId());
        formUser.setRole(citoyen.getRole());

        // mise à jour en base via ton repository
        Utilisateur existant = utilisateurRepository.findById(citoyen.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        existant.setNom(formUser.getNom());
        // adapte ici en fonction de ta classe Utilisateur :
        // existant.setPrenom(formUser.getPrenom());
        existant.setEmail(formUser.getEmail());
        // ne touche pas au mot de passe ici

        Utilisateur updated = utilisateurRepository.save(existant);

        // mettre à jour la session
        session.setAttribute("utilisateur", updated);
        session.setAttribute("userNom", updated.getNom());
        session.setAttribute("userEmail", updated.getEmail());

        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/citoyens/profil";
    }

    //Cloturer incident
    @GetMapping("incidents/{id}/feedback")
    public String afficherFormFeedback(@PathVariable Long id,
                                       HttpSession session,
                                       Model model) {
        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        Incident incident = incidentWorkflowService.getIncidentByIdAndCitoyen(id, citoyen.getId());
        if (incident.getStatut() != StatutIncidentEnum.RESOLU) {
            return "redirect:/citoyens/incidents/mes-signalements";
        }

        model.addAttribute("incident", incident);
        model.addAttribute("pageTitle", "Clôturer l'incident");
        return "citoyens/incident-feedback";
    }

    // Nouveau endpoint : soumettre feedback + clôturer (POST)
    @PostMapping("incidents/{id}/feedback")
    public String soumettreFeedback(@PathVariable Long id,
                                    @RequestParam("feedback") String feedback,
                                    @RequestParam(value = "note", required = false) Integer note,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        Utilisateur citoyen = (Utilisateur) session.getAttribute("utilisateur");
        if (citoyen == null) {
            return "redirect:/login";
        }

        try {
            incidentWorkflowService.cloturerIncident(id, citoyen, feedback, note);
            redirectAttributes.addFlashAttribute("success",
                    "Merci pour votre avis, l'incident est maintenant clôturé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de clôturer cet incident : " + e.getMessage());
        }

        return "redirect:/citoyens/incidents/mes-signalements";
    }




}