package com.example.demo.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    /**
     * Page d'accueil publique (Landing Page)
     */
    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("pageTitle", "Plateforme Signalement Municipal");
        model.addAttribute("welcomeMessage", "Votre ville, votre voix");
        return "index";
    }

    /**
     * Page "À propos" publique
     */
    @GetMapping("/about")
    public String showAboutPage(Model model) {
        model.addAttribute("pageTitle", "À propos");
        model.addAttribute("description", "Plateforme de signalement d'incidents municipaux");
        return "about";
    }

    /**
     * Page "Contact" publique
     */
    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("pageTitle", "Contact");
        return "contact";
    }

    /**
     * Page "Fonctionnalités" publique
     */
    @GetMapping("/features")
    public String showFeaturesPage(Model model) {
        model.addAttribute("pageTitle", "Fonctionnalités");
        return "features";
    }
}