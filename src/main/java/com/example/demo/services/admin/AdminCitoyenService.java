package com.example.demo.services.admin;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import com.example.demo.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCitoyenService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Prépare la liste des citoyens pour l'affichage
     */
    public void prepareCitoyensList(Model model) {
        List<Utilisateur> citoyens = getAllCitoyens();
        model.addAttribute("citoyens", citoyens);
    }

    /**
     * Gère le changement de statut d'un citoyen
     */
    @Transactional
    public String handleToggleCitoyenStatus(Long citoyenId, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur citoyen = findCitoyenById(citoyenId);
            citoyen.setCompteActive(!citoyen.isCompteActive());
            utilisateurRepository.save(citoyen);

            redirectAttributes.addFlashAttribute("success",
                    " Statut du citoyen modifié: " +
                            (citoyen.isCompteActive() ? "Activé" : "Désactivé"));
            return "redirect:/admin/citoyens";

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    " Erreur lors du changement de statut: " + e.getMessage());
        }
        return "redirect:/admin/citoyens";
    }

    /**
     * Gère la suppression d'un citoyen
     */
    @Transactional
    public String handleDeleteCitoyen(Long citoyenId, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur citoyen = findCitoyenById(citoyenId);
            utilisateurRepository.delete(citoyen);

            redirectAttributes.addFlashAttribute("success", " Citoyen supprimé avec succès");
            return "redirect:/admin/citoyens";

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", " " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/citoyens";
    }

    /**
     * Récupère tous les citoyens
     */
    public List<Utilisateur> getAllCitoyens() {
        return utilisateurRepository.findByRole(RoleEnum.CITOYEN);
    }

    /**
     * Trouve un citoyen par son ID
     */
    private Utilisateur findCitoyenById(Long citoyenId) {
        Utilisateur citoyen = utilisateurRepository.findById(citoyenId)
                .orElseThrow(() -> new IllegalArgumentException("Citoyen non trouvé"));

        if (citoyen.getRole() != RoleEnum.CITOYEN) {
            throw new IllegalStateException("Cet utilisateur n'est pas un citoyen");
        }

        return citoyen;
    }
}