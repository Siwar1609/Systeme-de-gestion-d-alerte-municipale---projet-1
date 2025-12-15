package com.example.demo.services.Citoyen.impl;

import com.example.demo.models.Incident;
import com.example.demo.models.enums.StatutIncidentEnum;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.services.Citoyen.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;
    @Value("${app.uploads.incidents-dir}")
    private String uploadDir;

    @Override
    public void creerIncident(Incident incident, MultipartFile[] photos) {
        // 1) Initialiser les infos métier
        //incident.setDateSignalement(LocalDateTime.now());
        incident.setStatut(StatutIncidentEnum.SIGNALE);

        // 2) Sauvegarder d'abord l'incident pour avoir un ID
        Incident saved = incidentRepository.save(incident);

        // 3) Dossier racine des uploads (déjà préparé plus haut)
        Path uploadRoot = Paths.get("uploads/incidents");
        try {
            if (!Files.exists(uploadRoot)) {
                Files.createDirectories(uploadRoot);
            }

            List<String> noms = new ArrayList<>();

            if (photos != null) {
                for (MultipartFile file : photos) {
                    if (file.isEmpty()) {
                        continue;
                    }

                    // Sécuriser le nom de fichier
                    String originalName = Paths.get(file.getOriginalFilename())
                            .getFileName()
                            .toString(); // évite les chemins bizarres

                    String fileName = "incident_" + saved.getId() + "_" +
                            System.currentTimeMillis() + "_" + originalName;

                    Path destination = uploadRoot.resolve(fileName);
                    System.out.println("Upload dir = " + uploadRoot.toAbsolutePath());

                    // 4) Copie physique sur le disque
                    Files.copy(file.getInputStream(), destination,
                            StandardCopyOption.REPLACE_EXISTING);

                    noms.add(fileName);
                }
            }

            // 5) Enregistrer les noms de fichiers dans l'entité
            if (!noms.isEmpty()) {
                saved.setNomsPhotos(String.join(";", noms));
                incidentRepository.save(saved);
            }

        } catch (IOException e) {
            // À améliorer: logger proprement, éventuellement remonter une exception métier
            e.printStackTrace();
        }
    }
    @Override
    public List<Incident> findByCitoyenId(Long citoyenId) {
        return incidentRepository.findByCitoyenIdOrderByDateSignalementDesc(citoyenId);
    }
    @Override
    public Incident findByIdAndCitoyen(Long incidentId, Long citoyenId) {
        return incidentRepository
                .findByIdAndCitoyenId(incidentId, citoyenId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable ou non autorisé"));
    }

    @Override
    public Incident mettreAJourIncident(Incident incidentModifie, MultipartFile[] photos) {
        Incident existant = incidentRepository.findById(incidentModifie.getId())
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        // mettre à jour uniquement certains champs
        existant.setTitre(incidentModifie.getTitre());
        existant.setDescription(incidentModifie.getDescription());
        existant.setLocalisation(incidentModifie.getLocalisation());
        existant.setCategorie(incidentModifie.getCategorie());
        existant.setQuartier(incidentModifie.getQuartier());
        // éventuellement statut si tu l'autorises

        // gérer éventuellement de nouvelles photos (optionnel)
        // (reprendre ta logique de sauvegarde de fichiers)
        Path uploadRoot = Paths.get("uploads/incidents");
        try {
            if (!Files.exists(uploadRoot)) {
                Files.createDirectories(uploadRoot);
            }

            // Liste des noms déjà enregistrés
            List<String> noms = new ArrayList<>();
            if (existant.getNomsPhotos() != null && !existant.getNomsPhotos().isEmpty()) {
                noms.addAll(Arrays.asList(existant.getNomsPhotos().split(";")));
            }

            // Ajouter les nouvelles photos
            if (photos != null) {
                for (MultipartFile file : photos) {
                    if (file.isEmpty()) {
                        continue;
                    }

                    String originalName = Paths.get(file.getOriginalFilename())
                            .getFileName()
                            .toString();

                    String fileName = "incident_" + existant.getId() + "_" +
                            System.currentTimeMillis() + "_" + originalName;

                    Path destination = uploadRoot.resolve(fileName);
                    Files.copy(file.getInputStream(), destination,
                            StandardCopyOption.REPLACE_EXISTING);

                    noms.add(fileName);
                }
            }

            // Mettre à jour le champ nomsPhotos si on a au moins une photo
            if (!noms.isEmpty()) {
                existant.setNomsPhotos(String.join(";", noms));
            }

        } catch (IOException e) {
            e.printStackTrace();
            // à toi de voir si tu lances une RuntimeException ici
        }

        return incidentRepository.save(existant);
    }
    @Override
    public void supprimerIncidentPourCitoyen(Long incidentId, Long citoyenId) {
        // sécurité : vérifier que l'incident appartient au citoyen
        boolean existe = incidentRepository.existsByIdAndCitoyenId(incidentId, citoyenId);
        if (!existe) {
            throw new RuntimeException("Incident introuvable ou non autorisé");
        }
        incidentRepository.deleteByIdAndCitoyenId(incidentId, citoyenId);
    }



}