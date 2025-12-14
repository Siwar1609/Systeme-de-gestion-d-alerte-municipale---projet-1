package com.example.demo.controllers.admin;

import com.example.demo.models.Quartier;
import com.example.demo.services.quartier.QuartierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/quartiers")
@RequiredArgsConstructor
public class QuartierController {

    private final QuartierService quartierService;

    // ----------- LISTE DES QUARTIERS -----------
    @GetMapping
    public String listQuartiers(Model model) {
        var quartiers = quartierService.getAllQuartiers();
        model.addAttribute("pageTitle", "Gestion des Quartiers");
        model.addAttribute("quartiers", quartiers);
        model.addAttribute("totalQuartiers", quartiers.size());
        return "admin/quartier/list";
    }

    // ----------- FORMULAIRE DE CRÉATION -----------
    @GetMapping("/create")
    public String createQuartierForm(Model model) {
        model.addAttribute("pageTitle", "Créer un Quartier");
        model.addAttribute("quartier", new Quartier());
        return "admin/quartier/form";
    }

    // ----------- CRÉATION D’UN QUARTIER -----------
    @PostMapping
    public String createQuartier(@ModelAttribute Quartier quartier,
                                 RedirectAttributes redirectAttributes) {
        try {
            quartierService.createQuartier(quartier);
            redirectAttributes.addFlashAttribute(
                    "success", "Quartier créé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Erreur lors de la création : " + e.getMessage());
        }
        return "redirect:/admin/quartiers";
    }

    // ----------- SUPPRESSION -----------
    @GetMapping("/{id}/delete")
    public String deleteQuartier(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            quartierService.deleteQuartier(id);
            redirectAttributes.addFlashAttribute(
                    "success", "Quartier supprimé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/quartiers";
    }

    // ----------- DÉTAIL D’UN QUARTIER -----------
    @GetMapping("/{id}")
    public String viewQuartier(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Quartier quartier = quartierService.getQuartierById(id);
            model.addAttribute("pageTitle", "Détails du Quartier");
            model.addAttribute("quartier", quartier);
            return "admin/quartiers/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Quartier introuvable.");
            return "redirect:/admin/quartiers";
        }
    }
}