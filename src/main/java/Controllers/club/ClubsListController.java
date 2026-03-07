package Controllers.club;

import Entite.Club;
import Service.club.ClubService;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ClubsListController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> villeFilter;
    @FXML
    private FlowPane clubsFlowPane;
    @FXML
    private Label emptyLabel;

    private final ClubService clubService = new ClubService();

    @FXML
    public void initialize() {
        loadVilleFilter();
        loadClubs();

        // Real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterClubs());
        villeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterClubs());
    }

    private void loadVilleFilter() {
        villeFilter.getItems().clear();
        villeFilter.getItems().add("Toutes");
        villeFilter.getItems().addAll(clubService.getDistinctVilles());
        villeFilter.setValue("Toutes");
    }

    private void loadClubs() {
        try {
            List<Club> clubs = clubService.readAll();
            displayClubs(clubs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterClubs() {
        String keyword = searchField.getText();
        String ville = villeFilter.getValue();
        List<Club> filtered = clubService.searchClubs(keyword, ville);
        displayClubs(filtered);
    }

    private void displayClubs(List<Club> clubs) {
        clubsFlowPane.getChildren().clear();

        if (clubs.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (Club club : clubs) {
            clubsFlowPane.getChildren().add(buildClubCard(club));
        }
    }

    private VBox buildClubCard(Club club) {
        VBox card = new VBox(0);
        card.getStyleClass().add("club-list-card");
        card.setPrefWidth(280);
        card.setMinHeight(380);
        card.setMaxHeight(380);

        // ── Club image ──
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        String photoPath = club.getPhotoClub();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        getClass().getResourceAsStream(photoPath));
                imageView.setImage(img);
            } catch (Exception e) {
                // fallback: no image
            }
        }
        // Rounded top corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(280, 140);
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        imageView.setClip(clip);

        // ── Card body ──
        VBox body = new VBox(6);
        body.getStyleClass().add("club-list-card-body");
        body.setPadding(new Insets(12, 18, 16, 18));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Club name
        Label nameLabel = new Label(club.getNom());
        nameLabel.getStyleClass().add("club-list-card-name");

        // Ville
        Label villeLabel = new Label(club.getVille() != null ? club.getVille() : "");
        villeLabel.getStyleClass().add("club-list-card-ville");

        // Description (truncated)
        String desc = club.getDescription() != null ? club.getDescription() : "";
        if (desc.length() > 100)
            desc = desc.substring(0, 100) + "...";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("club-list-card-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(36);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Stats chips row
        int members = clubService.getMemberCount(club.getIdClub());
        int coaches = clubService.getCoachCount(club.getIdClub());
        int seances = clubService.getSeanceCount(club.getIdClub());
        double avg = clubService.getAvgSatisfaction(club.getIdClub());

        HBox statsRow = new HBox(10);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                buildStatChip(members + " membres"),
                buildStatChip(coaches + " coachs"),
                buildStatChip(seances + " séances"));

        // Rating + Button row
        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        String ratingText = avg > 0 ? String.format("★ %.1f", avg) : "★ N/A";
        Label ratingLabel = new Label(ratingText);
        ratingLabel.getStyleClass().add("club-list-card-rating");

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.getStyleClass().add("club-details-btn");
        detailsBtn.setOnAction(e -> navigateToDetail(club, detailsBtn));

        bottomRow.getChildren().addAll(ratingLabel, bottomSpacer, detailsBtn);

        body.getChildren().addAll(nameLabel, villeLabel, descLabel, spacer, statsRow, bottomRow);
        card.getChildren().addAll(imageView, body);

        return card;
    }

    private Label buildStatChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("stat-chip");
        return chip;
    }

    private void navigateToDetail(Club club, javafx.scene.Node source) {
        try {
            javafx.scene.Scene scene = source.getScene();
            BorderPane root = (BorderPane) scene.getRoot();

            ClubDetailController detailCtrl = new ClubDetailController(club);
            javafx.scene.Node detailPage = detailCtrl.buildPage(() -> {
                // Back to clubs: fire the sidebar Clubs button
                javafx.scene.Node sidebar = root.getLeft();
                if (sidebar != null) {
                    javafx.scene.control.Button btnClubs = (javafx.scene.control.Button) sidebar.lookup("#btnClubs");
                    if (btnClubs != null)
                        btnClubs.fire();
                }
            });

            root.setCenter(detailPage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
