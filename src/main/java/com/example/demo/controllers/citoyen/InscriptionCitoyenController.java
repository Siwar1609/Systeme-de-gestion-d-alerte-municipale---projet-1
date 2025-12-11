package com.example.demo.controllers.citoyen;

import com.example.demo.dto.InscriptionRequest;
import com.example.demo.services.InscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/citoyens")
@RequiredArgsConstructor
public class InscriptionCitoyenController {

    private final InscriptionService inscriptionService;

    /**
     * Affiche le formulaire d'inscription
     */
    @GetMapping("/inscription")
    public String showInscriptionForm(Model model) {
        if (!model.containsAttribute("inscriptionRequest")) {
            model.addAttribute("inscriptionRequest", new InscriptionRequest());
        }
        model.addAttribute("pageTitle", "Inscription Citoyen");
        return "citoyens/inscription";
    }

    /**
     * Traite l'inscription
     */
    @PostMapping("/inscription")
    public String processInscription(
            @Valid @ModelAttribute("inscriptionRequest") InscriptionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Inscription Citoyen - Erreur");
            return "citoyens/inscription";
        }

        // Vérification manuelle de la confirmation du mot de passe
        if (!request.getMotDePasse().equals(request.getConfirmationMotDePasse())) {
            bindingResult.rejectValue("confirmationMotDePasse", "error.inscriptionRequest",
                    "Les mots de passe ne correspondent pas");
            model.addAttribute("pageTitle", "Inscription Citoyen - Erreur");
            return "citoyens/inscription";
        }

        try {
            // Inscription du citoyen
            inscriptionService.inscrireCitoyen(
                    request.getEmail(),
                    request.getNom(),
                    request.getMotDePasse()
            );

            redirectAttributes.addFlashAttribute("success",
                    "Inscription réussie ! Un email de vérification a été envoyé à " + request.getEmail());
            return "redirect:/citoyens/inscription-success";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Inscription Citoyen - Erreur");
            return "citoyens/inscription";
        }
    }

    /**
     * Page de succès après inscription
     */
    @GetMapping("/inscription-success")
    public String showInscriptionSuccess(Model model) {
        model.addAttribute("pageTitle", "Inscription Réussie");
        return "citoyens/inscription-success";
    }

    /**
     * Vérification de l'email (quand l'utilisateur clique sur le lien dans l'email)
     */
    @GetMapping("/verifier-email")
    public String verifierEmail(@RequestParam String token, Model model) {
        boolean verifie = inscriptionService.verifierEmail(token);

        model.addAttribute("pageTitle", "Vérification d'Email");
        if (verifie) {
            model.addAttribute("success", "Email vérifié avec succès ! Vous pouvez maintenant vous connecter.");
        } else {
            model.addAttribute("error", "Lien de vérification invalide ou expiré.");
        }

        return "citoyens/verification-email";
    }

    /**
     * Page pour renvoyer l'email de vérification
     */
    @GetMapping("/renvoyer-verification")
    public String showRenvoyerVerification(Model model) {
        model.addAttribute("pageTitle", "Renvoyer la vérification");
        return "citoyens/renvoyer-verification";
    }

    @PostMapping("/renvoyer-verification")
    public String processRenvoyerVerification(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        try {
            inscriptionService.renvoyerEmailVerification(email);
            redirectAttributes.addFlashAttribute("success",
                    "Un nouvel email de vérification a été envoyé à " + email);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/citoyens/renvoyer-verification";
    }
}