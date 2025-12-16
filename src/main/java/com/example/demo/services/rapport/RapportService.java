package com.example.demo.services.rapport;

import com.example.demo.models.Incident;
import com.example.demo.models.Rapport;
import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.TypeRapportEnum;
import com.example.demo.repositories.RapportRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Service
public class RapportService {

    @Autowired
    private RapportRepository rapportRepository;

    public void genererRapportPourIncident(
            Incident incident,
            TypeRapportEnum typeRapport,
            Utilisateur admin,
            HttpServletResponse response
    ) throws IOException {

        // ✅ PLUS DE LIMITATION : génération autorisée plusieurs fois
        Rapport rapport = new Rapport();
        rapport.setIncident(incident);
        rapport.setAdmin(admin);
        rapport.setDate(LocalDateTime.now());
        rapport.setTypeRapport(typeRapport);

        String contenu = """
                ID : %d
                Titre : %s
                Description : %s
                Localisation : %s
                Priorité : %s
                Statut : %s
                Feedback : %s
                """.formatted(
                incident.getId(),
                incident.getTitre(),
                incident.getDescription(),
                incident.getLocalisation(),
                incident.getPriorite(),
                incident.getStatut(),
                incident.getFeedbackCitoyen()
        );

        rapport.setContenu(contenu);
        rapportRepository.save(rapport); // ✔ historique conservé

        if (typeRapport == TypeRapportEnum.CSV) {
            genererCsv(incident, response);
        } else {
            genererPdf(incident, response);
        }
    }

    // ======================= PDF =======================
    private void genererPdf(Incident incident, HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=rapport_incident_" + incident.getId() + ".pdf"
        );

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

            document.add(new Paragraph("RAPPORT D’INCIDENT", titreFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("ID : " + incident.getId()));
            document.add(new Paragraph("Titre : " + incident.getTitre()));
            document.add(new Paragraph("Description : " + incident.getDescription()));
            document.add(new Paragraph("Localisation : " + incident.getLocalisation()));
            document.add(new Paragraph("Priorité : " + incident.getPriorite()));
            document.add(new Paragraph("Statut : " + incident.getStatut()));
            document.add(new Paragraph("Feedback citoyen : " + incident.getFeedbackCitoyen()));
            document.add(new Paragraph(" "));

            // ======== PHOTOS ========
            if (incident.getNomsPhotos() != null && !incident.getNomsPhotos().isEmpty()) {

                document.add(new Paragraph("Photos de l’incident :", sectionFont));
                document.add(new Paragraph(" "));

                String[] photos = incident.getNomsPhotos().split(";");

                for (String photo : photos) {
                    try {
                        String imagePath = "uploads/incidents/" + photo.trim();
                        Image img = Image.getInstance(imagePath);

                        img.scaleToFit(400, 300);
                        img.setAlignment(Image.ALIGN_CENTER);

                        document.add(img);
                        document.add(new Paragraph(" "));
                    } catch (Exception e) {
                        document.add(new Paragraph("Image introuvable : " + photo));
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        } finally {
            document.close();
        }
    }

    // ======================= CSV =======================
    private void genererCsv(Incident incident, HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=rapport_incident_" + incident.getId() + ".csv"
        );

        PrintWriter writer = response.getWriter();
        writer.println("ID,Titre,Description,Localisation,Priorité,Statut,Feedback");
        writer.println(
                incident.getId() + "," +
                        incident.getTitre() + "," +
                        incident.getDescription() + "," +
                        incident.getLocalisation() + "," +
                        incident.getPriorite() + "," +
                        incident.getStatut() + "," +
                        incident.getFeedbackCitoyen()
        );
        writer.flush();
    }
}
