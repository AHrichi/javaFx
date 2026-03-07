package Controllers.evenement;

import Controllers.FeedbackDialogController;
import Entite.Evenement;
import Service.evenement.ServiceEvenement;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import Service.ServiceDemande;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EvenementUserController {

    @FXML
    private VBox evenementsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnReset;

    private ServiceEvenement serviceEvenement;
    private final ServiceDemande serviceDemande = new ServiceDemande();
    private List<Evenement> allEvents;

    public EvenementUserController() {
        this.serviceEvenement = new ServiceEvenement();
    }

    @FXML
    public void initialize() {
        evenementsContainer.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        loadAllEvents();
        setupFilters();
    }

    private void loadAllEvents() {
        try {
            java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
            allEvents = serviceEvenement.readAll().stream()
                    .filter(e -> e.getDateEvenement() != null && !e.getDateEvenement().before(today))
                    .collect(Collectors.toList());
            displayEvents(allEvents);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Impossible de charger les événements: " + e.getMessage()).showAndWait();
        }
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        btnReset.setOnAction(e -> {
            searchField.clear();
            displayEvents(allEvents);
        });
    }

    private void applyFilters() {
        if (allEvents == null)
            return;
        String search = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        List<Evenement> filtered = allEvents.stream()
                .filter(e -> {
                    if (!search.isEmpty()) {
                        String nom = e.getNom() != null ? e.getNom().toLowerCase() : "";
                        String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
                        if (!nom.contains(search) && !desc.contains(search))
                            return false;
                    }
                    return true;
                }).collect(Collectors.toList());
        displayEvents(filtered);
    }

    private void displayEvents(List<Evenement> events) {
        evenementsContainer.getChildren().clear();
        if (events.isEmpty()) {
            Label empty = new Label("Aucun événement trouvé");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8; -fx-padding: 40;");
            evenementsContainer.getChildren().add(empty);
            return;
        }
        for (Evenement evt : events) {
            evenementsContainer.getChildren().add(createEventCard(evt));
        }
    }

    // ── Event List Row (horizontal card) ─────────────────

    private HBox createEventCard(Evenement evt) {
        HBox card = new HBox(0);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        // ── Left: Date column ──
        VBox dateCol = new VBox(0);
        dateCol.setAlignment(Pos.CENTER);
        dateCol.setMinWidth(70);
        dateCol.setPrefWidth(70);
        dateCol.setStyle("-fx-background-color: #053536; -fx-background-radius: 12 0 0 12; -fx-padding: 16 0;");
        dateCol.setMinHeight(100);

        if (evt.getDateEvenement() != null) {
            LocalDate d = evt.getDateEvenement().toLocalDate();
            Label dayLabel = new Label(String.valueOf(d.getDayOfMonth()));
            dayLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #FFBB02;");
            Label monthLabel = new Label(d.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase());
            monthLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #ffffffcc;");
            Label yearLabel = new Label(String.valueOf(d.getYear()));
            yearLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #ffffff88;");
            dateCol.getChildren().addAll(dayLabel, monthLabel, yearLabel);
        }

        // ── Center: Info section ──
        VBox infoCol = new VBox(4);
        infoCol.setPadding(new Insets(14, 16, 14, 16));
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        Label lblNom = new Label(evt.getNom());
        lblNom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #053536;");

        String desc = evt.getDescription() != null ? evt.getDescription() : "";
        if (desc.length() > 90)
            desc = desc.substring(0, 90) + "…";
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        lblDesc.setWrapText(true);

        // Info chips
        HBox chipsRow = new HBox(12);
        chipsRow.setAlignment(Pos.CENTER_LEFT);
        chipsRow.setPadding(new Insets(4, 0, 0, 0));

        int total = evt.getCapaciteMax();
        int remaining = evt.getPlacesRestantes();

        HBox capChip = new HBox(4);
        capChip.setAlignment(Pos.CENTER_LEFT);
        Label capIcon = new Label("\uE7FB");
        capIcon.setStyle("-fx-font-family: 'Material Icons'; -fx-font-size: 13px; -fx-text-fill: #FFBB02;");
        Label capText = new Label(remaining + "/" + total + " places");
        capText.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (remaining > 0 ? "#059669" : "#dc2626")
                + "; -fx-font-weight: bold;");
        capChip.getChildren().addAll(capIcon, capText);

        // Price chip
        Label priceChip = new Label(evt.getPrix() > 0 ? String.format("%.0f DT", evt.getPrix()) : "Gratuit");
        priceChip.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " +
                (evt.getPrix() > 0 ? "#053536" : "#059669") +
                "; -fx-background-color: " + (evt.getPrix() > 0 ? "#fef3c7" : "#ecfdf5") +
                "; -fx-padding: 2 8; -fx-background-radius: 6;");

        chipsRow.getChildren().addAll(capChip, priceChip);
        infoCol.getChildren().addAll(lblNom, lblDesc, chipsRow);

        // ── Right: Button column ──
        VBox actionCol = new VBox(8);
        actionCol.setAlignment(Pos.CENTER);
        actionCol.setPadding(new Insets(14, 18, 14, 0));

        Button btnDetail = new Button("Voir les détails");
        btnDetail.getStyleClass().add("home-club-btn");
        btnDetail.setStyle(
                "-fx-background-color: #FFBB02; -fx-text-fill: #053536; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDetail.setOnAction(e -> showEventDetail(evt));

        actionCol.getChildren().add(btnDetail);

        card.getChildren().addAll(dateCol, infoCol, actionCol);
        return card;
    }

    // ── Inline Detail View ──────────────────────────────

    private void showEventDetail(Evenement evt) {
        BorderPane rootPane;
        try {
            rootPane = (BorderPane) evenementsContainer.getScene().getRoot();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        final BorderPane root = rootPane;

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #f4f7f9; -fx-background: #f4f7f9;");

        VBox page = new VBox(0);
        page.setStyle("-fx-background-color: #f4f7f9;");

        // Breadcrumb
        HBox breadcrumb = new HBox(6);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.setPadding(new Insets(16, 30, 16, 30));
        breadcrumb.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Label backIcon = new Label("\uE5C4");
        backIcon.setStyle(
                "-fx-font-family: 'Material Icons'; -fx-font-size: 18px; -fx-text-fill: #053536; -fx-cursor: hand;");
        Label backLabel = new Label("Événements");
        backLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #053536; -fx-cursor: hand; -fx-underline: true;");
        Label sep = new Label("  /  ");
        sep.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        Label currentLabel = new Label(evt.getNom());
        currentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        javafx.event.EventHandler<javafx.scene.input.MouseEvent> goBack = e -> {
            try {
                javafx.scene.Parent view = javafx.fxml.FXMLLoader
                        .load(getClass().getResource("/evenement/evenement_user.fxml"));
                root.setCenter(view);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        backIcon.setOnMouseClicked(goBack);
        backLabel.setOnMouseClicked(goBack);
        breadcrumb.getChildren().addAll(backIcon, backLabel, sep, currentLabel);

        // Header with dark teal background
        StackPane headerImg = new StackPane();
        headerImg.setMinHeight(220);
        headerImg.setPrefHeight(220);
        headerImg.setStyle("-fx-background-color: linear-gradient(to right, #053536, #0a5a5c);");

        // Event icon if no image
        Label heroIcon = new Label("\uE878");
        heroIcon.setStyle("-fx-font-family: 'Material Icons'; -fx-font-size: 80px; -fx-text-fill: #ffffff22;");
        headerImg.getChildren().add(heroIcon);

        VBox overlay = new VBox(8);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 0, 24, 30));

        Label titleOverlay = new Label(evt.getNom());
        titleOverlay.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox subRow = new HBox(16);
        subRow.setAlignment(Pos.CENTER_LEFT);
        if (evt.getDateEvenement() != null) {
            subRow.getChildren().add(infoChipLight("\uE878",
                    evt.getDateEvenement().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));
        }
        subRow.getChildren().add(infoChipLight("\uE7FB", evt.getCapaciteMax() + " places max"));
        subRow.getChildren().add(infoChipLight("\uE227",
                evt.getPrix() > 0 ? String.format("%.0f DT", evt.getPrix()) : "Gratuit"));

        overlay.getChildren().addAll(titleOverlay, subRow);
        StackPane.setAlignment(overlay, Pos.BOTTOM_LEFT);
        headerImg.getChildren().add(overlay);

        // Content body (2 columns)
        HBox contentBody = new HBox(24);
        contentBody.setPadding(new Insets(24, 30, 30, 30));

        // Left column
        VBox leftCol = new VBox(18);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        VBox aboutBox = new VBox(8);
        aboutBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        Label aboutTitle = new Label("Description");
        aboutTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #053536;");
        Label aboutText = new Label(evt.getDescription() != null ? evt.getDescription() : "Aucune description.");
        aboutText.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        aboutText.setWrapText(true);
        aboutBox.getChildren().addAll(aboutTitle, aboutText);

        leftCol.getChildren().add(aboutBox);

        // Right column: actions
        VBox rightCol = new VBox(14);
        rightCol.setPrefWidth(280);
        rightCol.setMinWidth(280);
        rightCol.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");

        Label capTitle = new Label("Inscription");
        capTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #053536;");

        int total = evt.getCapaciteMax();
        int remaining = evt.getPlacesRestantes();
        int occupied = total - remaining;
        double ratio = total > 0 ? (double) occupied / total : 0;

        Label capVal = new Label(occupied + " / " + total);
        capVal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #053536;");
        Label capSub = new Label("inscrits");
        capSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        HBox progBar = new HBox();
        progBar.setPrefHeight(8);
        progBar.setMaxHeight(8);
        progBar.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 4;");
        Region filledBar = new Region();
        filledBar.setPrefWidth(240 * ratio);
        filledBar.setMaxHeight(8);
        filledBar.setStyle(
                "-fx-background-color: " + (remaining > 0 ? "#FFBB02" : "#dc2626") + "; -fx-background-radius: 4;");
        progBar.getChildren().add(filledBar);

        Label capBadge = new Label(remaining > 0 ? remaining + " places restantes" : "Complet");
        capBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " +
                (remaining > 0 ? "#059669" : "#dc2626") + "; -fx-background-color: " +
                (remaining > 0 ? "#ecfdf5" : "#fef2f2") + "; -fx-padding: 6 14; -fx-background-radius: 8;");
        capBadge.setMaxWidth(Double.MAX_VALUE);
        capBadge.setAlignment(Pos.CENTER);

        // Price
        if (evt.getPrix() > 0) {
            Label priceLabel = new Label(String.format("Prix: %.0f DT", evt.getPrix()));
            priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFBB02;");
            rightCol.getChildren().add(priceLabel);
        }

        Button btnJoin = new Button("S'inscrire à l'événement");
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setStyle(
                "-fx-background-color: #053536; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");

        Entite.User currentUser = Utils.SessionManager.getCurrentUser();
        if (currentUser != null) {
            String statut = serviceDemande.getStatutDemande("EVENEMENT", evt.getIdEvenement(), currentUser.getIdUser());
            if ("EN_ATTENTE".equals(statut)) {
                btnJoin.setText("Demande en attente...");
                btnJoin.setStyle(
                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8;");
                btnJoin.setDisable(true);
            } else if ("APPROUVEE".equals(statut)) {
                btnJoin.setText("Déjà inscrit");
                btnJoin.setStyle(
                        "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8;");
                btnJoin.setDisable(true);
            }
        }
        btnJoin.setOnAction(e -> handleJoin(evt, btnJoin));

        Button btnFeedback = new Button("Laisser un avis");
        btnFeedback.setMaxWidth(Double.MAX_VALUE);
        btnFeedback.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #053536; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 24; -fx-border-color: #053536; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        btnFeedback.setOnAction(e -> ouvrirFeedbackPopup(evt.getIdEvenement(), "EVENEMENT"));

        rightCol.getChildren().addAll(capTitle, capVal, capSub, progBar, capBadge, btnJoin, btnFeedback);
        contentBody.getChildren().addAll(leftCol, rightCol);

        page.getChildren().addAll(breadcrumb, headerImg, contentBody);
        scroll.setContent(page);
        root.setCenter(scroll);
    }

    // ── Helpers ──────────────────────────────────────────

    private void loadEventImage(StackPane pane, String imagePath, double w, double h) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
                if (stream == null)
                    stream = getClass().getResourceAsStream("/images/" + imagePath);
                if (stream != null) {
                    Image img = new Image(stream, w, h, false, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(w);
                    iv.setFitHeight(h);
                    iv.setPreserveRatio(false);
                    Rectangle clip = new Rectangle(w, h);
                    clip.setArcWidth(24);
                    clip.setArcHeight(24);
                    iv.setClip(clip);
                    pane.getChildren().add(iv);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private HBox infoChipLight(String icon, String text) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER_LEFT);
        Label i = new Label(icon);
        i.setStyle("-fx-font-family: 'Material Icons'; -fx-font-size: 14px; -fx-text-fill: #FFBB02;");
        Label t = new Label(text);
        t.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffffcc;");
        chip.getChildren().addAll(i, t);
        return chip;
    }

    private void handleJoin(Evenement evt, Button btnJoin) {
        if (evt.getPlacesRestantes() > 0) {
            Entite.User u = Utils.SessionManager.getCurrentUser();
            if (u == null) {
                new Alert(AlertType.WARNING, "Vous devez être connecté.").showAndWait();
                return;
            }
            if (serviceDemande.aDejaDemande("EVENEMENT", evt.getIdEvenement(), u.getIdUser())) {
                new Alert(AlertType.INFORMATION, "Vous avez déjà une demande en attente.").showAndWait();
                return;
            }
            boolean ok = serviceDemande.demanderRejoindreEvenement(
                    evt.getIdEvenement(), evt.getNom(),
                    u.getIdUser(), u.getNom() + " " + u.getPrenom(), u.getEmail());
            if (ok) {
                btnJoin.setText("Demande envoyée...");
                btnJoin.setDisable(true);
                btnJoin.setStyle(
                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8;");
            } else {
                new Alert(AlertType.ERROR, "Erreur lors de l'envoi.").showAndWait();
            }
        } else {
            new Alert(AlertType.ERROR, "Événement complet.").showAndWait();
        }
    }

    private void ouvrirFeedbackPopup(int id, String type) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/feedback/FeedbackDialog.fxml"));
            VBox root = loader.load();
            FeedbackDialogController controller = loader.getController();
            controller.setData(id, type);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Laisser un avis");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Erreur lors de l'ouverture du formulaire de feedback.").show();
        }
    }
}
