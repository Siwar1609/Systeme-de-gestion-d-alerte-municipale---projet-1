package com.example.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citoyens")
public class CitoyenController {

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {

        // Récupérer le nom stocké en session
        String userNom = (String) session.getAttribute("userNom");

        model.addAttribute("pageTitle", "Tableau de bord Citoyen");
        model.addAttribute("userNom", userNom);


        return "citoyens/dashboard";
    }
}
