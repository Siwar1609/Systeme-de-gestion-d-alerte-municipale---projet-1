package com.example.demo.controllers;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/oauth2/success")
    public String oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User,
                                HttpSession session) {
        String email = oauth2User.getAttribute("email");

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseGet(() -> {
                    Utilisateur nouveau = new Utilisateur();
                    nouveau.setEmail(email);
                    nouveau.setNom(oauth2User.getAttribute("name"));
                    nouveau.setRole(RoleEnum.CITOYEN);
                    nouveau.setCompteActive(true);
                    return utilisateurRepository.save(nouveau);
                });

        session.setAttribute("utilisateur", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userNom", user.getNom());
        session.setAttribute("userRole", user.getRole());
        session.setMaxInactiveInterval(8 * 60 * 60);

        return "redirect:/citoyens/dashboard";
    }}
