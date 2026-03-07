package Controllers.club;

import Controllers.FeedbackDialogController;
import Entite.Club;
import Service.club.ServiceClub;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import Service.ServiceDemande;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ClubUserController {

    @FXML
    private FlowPane clubsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> villeFilter;
    @FXML
    private Button btnReset;

    private ServiceClub serviceClub;
    private final ServiceDemande serviceDemande = new ServiceDemande();
    private List<Club> allClubs;

    public ClubUserController() {
        this.serviceClub = new ServiceClub();
    }

    @FXML
    public void initialize() {
        clubsContainer.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        loadAllClubs();
        setupFilters();
    }

    private void loadAllClubs() {
        try {
            allClubs = serviceClub.readAll();
            displayClubs(allClubs);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Impossible de charger les clubs: " + e.getMessage()).showAndWait();
        }
    }

    private void setupFilters() {
        if (allClubs != null) {
            List<String> villes = allClubs.stream()
                    .map(Club::getVille)
                    .filter(v -> v != null && !v.isEmpty())
                    .distinct().sorted()
                    .collect(Collectors.toList());
            villeFilter.getItems().add("Toutes les villes");
            villeFilter.getItems().addAll(villes);
        }
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        villeFilter.setOnAction(e -> applyFilters());
        btnReset.setOnAction(e -> {
            searchField.clear();
            villeFilter.getSelectionModel().clearSelection();
            villeFilter.setPromptText("Toutes les villes");
            displayClubs(allClubs);
        });
    }

    private void applyFilters() {
        if (allClubs == null)
            return;
        String search = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String ville = villeFilter.getValue();
        List<Club> filtered = allClubs.stream()
                .filter(c -> {
                    if (!search.isEmpty()) {
                        String nom = c.getNom() != null ? c.getNom().toLowerCase() : "";
                        String desc = c.getDescription() != null ? c.getDescription().toLowerCase() : "";
                        if (!nom.contains(search) && !desc.contains(search))
                            return false;
                    }
                    if (ville != null && !ville.isEmpty() && !"Toutes les villes".equals(ville)) {
                        if (c.getVille() == null || !c.getVille().equals(ville))
                            return false;
                    }
                    return true;
                }).collect(Collectors.toList());
        displayClubs(filtered);
    }

    private void displayClubs(List<Club> clubs) {
        clubsContainer.getChildren().clear();
        if (clubs.isEmpty()) {
            Label empty = new Label("Aucun club trouvé");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8; -fx-padding: 40;");
            clubsContainer.getChildren().add(empty);
            return;
        }
        for (Club club : clubs) {
            clubsContainer.getChildren().add(createClubCard(club));
        }
    }

    // ── Club Card ────────────────────────────────────────

    private VBox createClubCard(Club club) {
        VBox card = new VBox(0);
        card.getStyleClass().add("home-club-card");
        card.setPrefWidth(260);
        card.setMaxWidth(260);

        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(150);
        imgPane.setStyle("-fx-background-color: #0a4a4b; -fx-background-radius: 12 12 0 0;");

        loadClubImage(imgPane, club.getPhotoClub(), 260, 150);

        if (club.getVille() != null && !club.getVille().isEmpty()) {
            Label villeBadge = new Label(club.getVille());
            villeBadge.setStyle(
                    "-fx-background-color: rgba(5,53,54,0.8); -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 6;");
            StackPane.setAlignment(villeBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(villeBadge, new Insets(8, 8, 0, 0));
            imgPane.getChildren().add(villeBadge);
        }

        VBox body = new VBox(6);
        body.setPadding(new Insets(12, 14, 14, 14));

        Label lblNom = new Label(club.getNom());
        lblNom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #053536;");

        String desc = club.getDescription() != null ? club.getDescription() : "";
        if (desc.length() > 60)
            desc = desc.substring(0, 60) + "…";
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        lblDesc.setWrapText(true);

        int total = club.getCapacite();
        int remaining = club.getPlacesRestantes();
        double ratio = total > 0 ? (double) (total - remaining) / total : 0;

        HBox capacityRow = new HBox(6);
        capacityRow.setAlignment(Pos.CENTER_LEFT);
        Label capIcon = new Label("\uE7FB");
        capIcon.setStyle("-fx-font-family: 'Material Icons'; -fx-font-size: 14px; -fx-text-fill: #FFBB02;");
        Label capText = new Label(remaining + " / " + total + " places");
        capText.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (remaining > 0 ? "#059669" : "#dc2626")
                + "; -fx-font-weight: bold;");
        capacityRow.getChildren().addAll(capIcon, capText);

        HBox progressBar = new HBox();
        progressBar.setPrefHeight(4);
        progressBar.setMaxHeight(4);
        progressBar.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 4;");
        Region filled = new Region();
        filled.setPrefWidth(230 * ratio);
        filled.setMaxHeight(4);
        filled.setStyle(
                "-fx-background-color: " + (remaining > 0 ? "#FFBB02" : "#dc2626") + "; -fx-background-radius: 4;");
        progressBar.getChildren().add(filled);

        Button btnDetail = new Button("Voir les détails");
        btnDetail.getStyleClass().add("home-club-btn");
        btnDetail.setMaxWidth(Double.MAX_VALUE);
        btnDetail.setOnAction(e -> showClubDetail(club));

        body.getChildren().addAll(lblNom, lblDesc, capacityRow, progressBar, btnDetail);
        card.getChildren().addAll(imgPane, body);
        return card;
    }

    // ── Inline Detail View ──────────────────────────────

    private void showClubDetail(Club club) {
        BorderPane rootPane;
        try {
            rootPane = (BorderPane) clubsContainer.getScene().getRoot();
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

        // ── Breadcrumb ──
        HBox breadcrumb = new HBox(6);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.setPadding(new Insets(16, 30, 16, 30));
        breadcrumb.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Label backIcon = new Label("\uE5C4");
        backIcon.setStyle(
                "-fx-font-family: 'Material Icons'; -fx-font-size: 18px; -fx-text-fill: #053536; -fx-cursor: hand;");
        Label backLabel = new Label("Clubs");
        backLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #053536; -fx-cursor: hand; -fx-underline: true;");
        Label sep = new Label("  /  ");
        sep.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        Label currentLabel = new Label(club.getNom());
        currentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        javafx.event.EventHandler<javafx.scene.input.MouseEvent> goBack = e -> {
            try {
                javafx.scene.Parent clubsView = javafx.fxml.FXMLLoader
                        .load(getClass().getResource("/club/club_user.fxml"));
                root.setCenter(clubsView);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        backIcon.setOnMouseClicked(goBack);
        backLabel.setOnMouseClicked(goBack);
        breadcrumb.getChildren().addAll(backIcon, backLabel, sep, currentLabel);

        // ── Header Image ──
        StackPane headerImg = new StackPane();
        headerImg.setMinHeight(250);
        headerImg.setPrefHeight(250);
        headerImg.setStyle("-fx-background-color: #053536;");

        if (club.getPhotoClub() != null && !club.getPhotoClub().isEmpty()) {
            try {
                java.io.InputStream stream = getClass().getResourceAsStream(club.getPhotoClub());
                if (stream != null) {
                    Image img = new Image(stream, 900, 250, false, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitHeight(250);
                    iv.fitWidthProperty().bind(scroll.widthProperty());
                    iv.setPreserveRatio(false);
                    iv.setOpacity(0.75);
                    headerImg.getChildren().add(iv);
                }
            } catch (Exception ignored) {
            }
        }

        VBox overlay = new VBox(4);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 0, 24, 30));
        overlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(5,53,54,0.8) 0%, transparent 100%);");

        Label titleOverlay = new Label(club.getNom());
        titleOverlay.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox subtitleRow = new HBox(16);
        subtitleRow.setAlignment(Pos.CENTER_LEFT);
        if (club.getVille() != null)
            subtitleRow.getChildren().add(infoChipLight("\uE55F", club.getVille()));
        if (club.getTelephone() != null)
            subtitleRow.getChildren().add(infoChipLight("\uE0CD", club.getTelephone()));
        if (club.getEmail() != null)
            subtitleRow.getChildren().add(infoChipLight("\uE0BE", club.getEmail()));

        overlay.getChildren().addAll(titleOverlay, subtitleRow);
        StackPane.setAlignment(overlay, Pos.BOTTOM_LEFT);
        headerImg.getChildren().add(overlay);

        // ── Content Body (2 columns) ──
        HBox contentBody = new HBox(24);
        contentBody.setPadding(new Insets(24, 30, 30, 30));

        // Left column
        VBox leftCol = new VBox(18);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        VBox aboutBox = styledCard("À propos",
                club.getDescription() != null ? club.getDescription() : "Aucune description disponible.");

        VBox addressBox = new VBox(8);
        addressBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        Label addrTitle = new Label("Adresse");
        addrTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #053536;");
        HBox addrRow = new HBox(8);
        addrRow.setAlignment(Pos.CENTER_LEFT);
        Label addrIcon = new Label("\uE55F");
        addrIcon.setStyle("-fx-font-family: 'Material Icons'; -fx-font-size: 18px; -fx-text-fill: #FFBB02;");
        Label addrText = new Label((club.getAdresse() != null ? club.getAdresse() : "") +
                (club.getVille() != null ? ", " + club.getVille() : ""));
        addrText.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        addrText.setWrapText(true);
        addrRow.getChildren().addAll(addrIcon, addrText);
        addressBox.getChildren().addAll(addrTitle, addrRow);

        leftCol.getChildren().addAll(aboutBox, addressBox);

        // Right column: actions
        VBox rightCol = new VBox(14);
        rightCol.setPrefWidth(280);
        rightCol.setMinWidth(280);
        rightCol.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");

        Label capTitle = new Label("Capacité");
        capTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #053536;");

        int total = club.getCapacite();
        int remaining = club.getPlacesRestantes();
        int occupied = total - remaining;
        double ratio = total > 0 ? (double) occupied / total : 0;

        Label capVal = new Label(occupied + " / " + total);
        capVal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #053536;");
        Label capSub = new Label("membres inscrits");
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

        Button btnJoin = new Button("Rejoindre le Club");
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setStyle(
                "-fx-background-color: #053536; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");

        Entite.User currentUser = Utils.SessionManager.getCurrentUser();
        if (currentUser != null) {
            String statut = serviceDemande.getStatutDemande("CLUB", club.getIdClub(), currentUser.getIdUser());
            if ("EN_ATTENTE".equals(statut)) {
                btnJoin.setText("Demande en attente...");
                btnJoin.setStyle(
                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8;");
                btnJoin.setDisable(true);
            } else if ("APPROUVEE".equals(statut)) {
                btnJoin.setText("Déjà membre");
                btnJoin.setStyle(
                        "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8;");
                btnJoin.setDisable(true);
            }
        }
        btnJoin.setOnAction(e -> handleJoin(club, btnJoin));

        Button btnFeedback = new Button("Laisser un avis");
        btnFeedback.setMaxWidth(Double.MAX_VALUE);
        btnFeedback.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #053536; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 24; -fx-border-color: #053536; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        btnFeedback.setOnAction(e -> ouvrirFeedbackPopup(club.getIdClub(), "CLUB"));

        rightCol.getChildren().addAll(capTitle, capVal, capSub, progBar, capBadge, btnJoin, btnFeedback);
        contentBody.getChildren().addAll(leftCol, rightCol);

        page.getChildren().addAll(breadcrumb, headerImg, contentBody);
        scroll.setContent(page);
        root.setCenter(scroll);
    }

    // ── Helpers ──────────────────────────────────────────

    private void loadClubImage(StackPane pane, String photoPath, double w, double h) {
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                java.io.InputStream stream = getClass().getResourceAsStream(photoPath);
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

    private VBox styledCard(String title, String content) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #053536;");
        Label c = new Label(content);
        c.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        c.setWrapText(true);
        box.getChildren().addAll(t, c);
        return box;
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

    private void handleJoin(Club club, Button btnJoin) {
        if (club.getPlacesRestantes() > 0) {
            Entite.User u = Utils.SessionManager.getCurrentUser();
            if (u == null) {
                new Alert(AlertType.WARNING, "Vous devez être connecté pour rejoindre un club.").showAndWait();
                return;
            }
            if (serviceDemande.aDejaDemande("CLUB", club.getIdClub(), u.getIdUser())) {
                new Alert(AlertType.INFORMATION, "Vous avez déjà une demande en attente pour ce club.").showAndWait();
                return;
            }
            boolean ok = serviceDemande.demanderRejoindreClub(
                    club.getIdClub(), club.getNom(),
                    u.getIdUser(), u.getNom() + " " + u.getPrenom(), u.getEmail());
            if (ok) {
                btnJoin.setText("Demande envoyée...");
                btnJoin.setDisable(true);
                btnJoin.setStyle(
                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8;");
            } else {
                new Alert(AlertType.ERROR, "Erreur lors de l'envoi de la demande.").showAndWait();
            }
        } else {
            new Alert(AlertType.ERROR, "Désolé, il n'y a plus de places disponibles.").showAndWait();
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
