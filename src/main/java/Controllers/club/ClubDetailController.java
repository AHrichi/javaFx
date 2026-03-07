package Controllers.club;

import Entite.Club;
import Entite.Seance;
import Service.club.ClubService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds the Club Detail page programmatically.
 * Called from ClubsListController when "Voir Détails" is clicked.
 */
public class ClubDetailController {

    private final Club club;
    private final ClubService clubService = new ClubService();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ClubDetailController(Club club) {
        this.club = club;
    }

    /**
     * Build and return the complete detail page node.
     */
    public Node buildPage(Runnable onBackToClubs) {
        VBox page = new VBox(0);
        page.getStyleClass().add("club-detail-page");

        // ── Breadcrumb ──────────────────────────────────
        HBox breadcrumb = new HBox(6);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.getStyleClass().add("club-detail-breadcrumb");
        breadcrumb.setPadding(new Insets(18, 28, 10, 28));

        Button backLink = new Button("Clubs");
        backLink.getStyleClass().add("breadcrumb-link");
        backLink.setOnAction(e -> onBackToClubs.run());

        Label sep1 = new Label("/");
        sep1.getStyleClass().add("breadcrumb-sep");

        Label clubNameCrumb = new Label(club.getNom());
        clubNameCrumb.getStyleClass().add("breadcrumb-link");

        Label sep2 = new Label("/");
        sep2.getStyleClass().add("breadcrumb-sep");

        Label detailsCrumb = new Label("Détails");
        detailsCrumb.getStyleClass().add("breadcrumb-current");

        breadcrumb.getChildren().addAll(backLink, sep1, clubNameCrumb, sep2, detailsCrumb);

        // ── Header with image ───────────────────────────
        StackPane header = buildHeader();

        // ── Info cards row ──────────────────────────────
        HBox infoCards = buildInfoCards();

        // ── Description section ─────────────────────────
        VBox descSection = buildDescriptionSection();

        // ── Activités section ───────────────────────────
        VBox activitesSection = buildActivitesSection();

        page.getChildren().addAll(breadcrumb, header, infoCards, descSection, activitesSection);

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    // ── Header ──────────────────────────────────────────

    private StackPane buildHeader() {
        StackPane header = new StackPane();
        header.getStyleClass().add("club-detail-header");
        header.setPrefHeight(220);
        header.setMaxWidth(Double.MAX_VALUE);

        // Background image
        String photo = club.getPhotoClub();
        if (photo != null && !photo.isEmpty()) {
            try {
                ImageView bgImage = new ImageView(
                        new Image(getClass().getResourceAsStream(photo)));
                bgImage.setFitHeight(220);
                bgImage.setPreserveRatio(false);
                bgImage.fitWidthProperty().bind(header.widthProperty());
                bgImage.setSmooth(true);
                bgImage.setOpacity(0.35);
                header.getChildren().add(bgImage);
            } catch (Exception ignored) {
            }
        }

        // Overlay text
        VBox overlay = new VBox(6);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(20));

        Label name = new Label(club.getNom());
        name.getStyleClass().add("club-detail-name");

        Label ville = new Label(club.getVille() != null ? club.getVille() : "");
        ville.getStyleClass().add("club-detail-ville");

        Label email = new Label(club.getEmail() != null ? club.getEmail() : "");
        email.getStyleClass().add("club-detail-email");

        overlay.getChildren().addAll(name, ville, email);
        header.getChildren().add(overlay);

        return header;
    }

    // ── Info cards row ──────────────────────────────────

    private HBox buildInfoCards() {
        int members = clubService.getMemberCount(club.getIdClub());
        int coaches = clubService.getCoachCount(club.getIdClub());
        int seances = clubService.getSeanceCount(club.getIdClub());
        double avg = clubService.getAvgSatisfaction(club.getIdClub());

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(24, 28, 8, 28));

        row.getChildren().addAll(
                buildInfoCard("group", String.valueOf(members), "Membres"),
                buildInfoCard("sports_kabaddi", String.valueOf(coaches), "Coachs"),
                buildInfoCard("event", String.valueOf(seances), "Séances"),
                buildInfoCard("star", avg > 0 ? String.format("%.1f/5", avg) : "N/A", "Satisfaction"));

        return row;
    }

    private VBox buildInfoCard(String icon, String value, String label) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("club-detail-info-card");
        card.setPrefWidth(160);
        card.setPadding(new Insets(16, 12, 16, 12));

        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("club-detail-info-icon");

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("club-detail-info-value");

        Label labelLbl = new Label(label);
        labelLbl.getStyleClass().add("club-detail-info-label");

        card.getChildren().addAll(iconLbl, valueLbl, labelLbl);
        return card;
    }

    // ── Description section ─────────────────────────────

    private VBox buildDescriptionSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(16, 28, 8, 28));

        Label title = new Label("À propos du club");
        title.getStyleClass().add("club-detail-section-title");

        String desc = club.getDescription() != null ? club.getDescription() : "Aucune description disponible.";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("club-detail-desc-text");
        descLabel.setWrapText(true);

        // Additional info row
        HBox details = new HBox(24);
        details.setPadding(new Insets(8, 0, 0, 0));

        if (club.getAdresse() != null) {
            Label addr = new Label("📍 " + club.getAdresse() + ", " + club.getVille());
            addr.getStyleClass().add("club-detail-meta");
            details.getChildren().add(addr);
        }
        if (club.getTelephone() != null) {
            Label phone = new Label("📞 " + club.getTelephone());
            phone.getStyleClass().add("club-detail-meta");
            details.getChildren().add(phone);
        }
        if (club.getDateCreation() != null) {
            Label date = new Label("📅 Créé le " + club.getDateCreation().toLocalDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            date.getStyleClass().add("club-detail-meta");
            details.getChildren().add(date);
        }

        section.getChildren().addAll(title, descLabel, details);
        return section;
    }

    // ── Activités section ───────────────────────────────

    private VBox buildActivitesSection() {
        VBox section = new VBox(14);
        section.setPadding(new Insets(16, 28, 28, 28));

        Label title = new Label("Activités & Séances");
        title.getStyleClass().add("club-detail-section-title");

        section.getChildren().add(title);

        List<Seance> seances = clubService.getSeancesByClub(club.getIdClub());

        if (seances.isEmpty()) {
            Label empty = new Label("Aucune séance programmée pour ce club.");
            empty.getStyleClass().add("club-detail-empty");
            section.getChildren().add(empty);
        } else {
            FlowPane grid = new FlowPane(16, 16);
            grid.setPadding(new Insets(4, 0, 0, 0));

            for (Seance s : seances) {
                grid.getChildren().add(buildSeanceCard(s));
            }
            section.getChildren().add(grid);
        }

        return section;
    }

    private VBox buildSeanceCard(Seance s) {
        VBox card = new VBox(6);
        card.getStyleClass().add("seance-card");
        card.setPrefWidth(260);
        card.setPadding(new Insets(16, 18, 16, 18));

        // Title
        Label titleLbl = new Label(s.getTitre());
        titleLbl.getStyleClass().add("seance-card-title");

        // Type + Niveau badge row
        HBox badgeRow = new HBox(8);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(s.getTypeSeance() != null ? s.getTypeSeance() : "");
        typeBadge.getStyleClass().add("seance-badge-type");

        Label niveauBadge = new Label(s.getNiveau() != null ? s.getNiveau() : "");
        niveauBadge.getStyleClass().add("seance-badge-niveau");

        badgeRow.getChildren().addAll(typeBadge, niveauBadge);

        // Description
        String desc = s.getDescription() != null ? s.getDescription() : "";
        if (desc.length() > 60)
            desc = desc.substring(0, 60) + "...";
        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("seance-card-desc");
        descLbl.setWrapText(true);

        // Coach
        String coachName = clubService.getCoachName(s.getIdCoach());
        Label coachLbl = new Label("Coach: " + coachName);
        coachLbl.getStyleClass().add("seance-card-Coach");

        // Date + Capacité
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        String dateStr = s.getDateDebut() != null ? s.getDateDebut().format(dateFmt) : "—";
        Label dateLbl = new Label("📅 " + dateStr);
        dateLbl.getStyleClass().add("seance-card-meta");

        Label capaciteLbl = new Label("👥 Max " + s.getCapaciteMax());
        capaciteLbl.getStyleClass().add("seance-card-meta");

        metaRow.getChildren().addAll(dateLbl, capaciteLbl);

        // Status
        Label statusLbl = new Label(s.getStatut() != null ? s.getStatut() : "");
        statusLbl.getStyleClass().add("seance-status-" +
                (s.getStatut() != null ? s.getStatut().toLowerCase().replace("é", "e") : ""));
        statusLbl.getStyleClass().add("seance-status-badge");

        card.getChildren().addAll(titleLbl, badgeRow, descLbl, coachLbl, metaRow, statusLbl);
        return card;
    }
}
