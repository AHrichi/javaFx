package Controllers.home;

import Entite.Club;
import Service.club.ClubService;
import Utils.DataSource;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HomeContentController {

    @FXML
    private Label lblClubCount;
    @FXML
    private Label lblMembreCount;
    @FXML
    private Label lblSeanceCount;
    @FXML
    private Label lblSatisfaction;
    @FXML
    private FlowPane clubCardsContainer;
    @FXML
    private FlowPane testimonialsContainer;

    private final ClubService clubService = new ClubService();

    @FXML
    public void initialize() {
        loadKPIs();
        loadTopClubs();
        loadTestimonials();
    }

    // ── KPIs ─────────────────────────────────────────────

    private void loadKPIs() {
        int clubs = clubService.countClubs();
        int membres = clubService.countMembres();
        int seances = clubService.countSeances();
        double satisfaction = clubService.avgSatisfaction();

        lblClubCount.setText(clubs + "+");
        lblMembreCount.setText(membres + "+");
        lblSeanceCount.setText(seances + "+");
        lblSatisfaction.setText(String.format("%.0f%%", satisfaction * 20));
    }

    // ── Top Clubs ────────────────────────────────────────

    private void loadTopClubs() {
        clubCardsContainer.getChildren().clear();
        List<Club> topClubs = clubService.getTopClubs(4);

        for (Club club : topClubs) {
            clubCardsContainer.getChildren().add(buildClubCard(club));
        }
    }

    private VBox buildClubCard(Club club) {
        VBox card = new VBox(0);
        card.getStyleClass().add("home-club-card");
        card.setPrefWidth(220);
        card.setMaxWidth(220);

        // Club image placeholder with background
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(130);
        imgPane.setStyle("-fx-background-color: #0a4a4b; -fx-background-radius: 12 12 0 0;");

        String photoPath = club.getPhotoClub();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                Image img = new Image(getClass().getResourceAsStream(photoPath), 220, 130, false, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(220);
                iv.setFitHeight(130);
                iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(220, 130);
                clip.setArcWidth(24);
                clip.setArcHeight(24);
                iv.setClip(clip);
                imgPane.getChildren().add(iv);
            } catch (Exception ignored) {
            }
        }
        // Club name overlay on image
        Label overlayName = new Label(club.getNom());
        overlayName.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        overlayName.setPadding(new Insets(0, 0, 8, 12));
        StackPane.setAlignment(overlayName, Pos.BOTTOM_LEFT);
        imgPane.getChildren().add(overlayName);

        // Card body
        VBox body = new VBox(6);
        body.setPadding(new Insets(10, 12, 12, 12));

        String desc = club.getDescription() != null ? club.getDescription() : "";
        if (desc.length() > 50)
            desc = desc.substring(0, 50) + "…";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        descLabel.setWrapText(true);

        Button btnDetail = new Button("À propos du club");
        btnDetail.getStyleClass().add("home-club-btn");

        body.getChildren().addAll(descLabel, btnDetail);
        card.getChildren().addAll(imgPane, body);

        return card;
    }

    // ── Testimonials from feedback table ──────────────────

    private void loadTestimonials() {
        testimonialsContainer.getChildren().clear();
        List<FeedbackItem> feedbacks = getRecentFeedbacks(3);
        for (FeedbackItem fb : feedbacks) {
            testimonialsContainer.getChildren().add(buildTestimonialCard(fb));
        }
    }

    private VBox buildTestimonialCard(FeedbackItem fb) {
        VBox card = new VBox(8);
        card.getStyleClass().add("home-testimonial-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(new Insets(18));

        // Stars
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fb.note; i++)
            stars.append("★");
        for (int i = fb.note; i < 5; i++)
            stars.append("☆");
        Label starsLabel = new Label(stars.toString());
        starsLabel.setStyle("-fx-text-fill: #FFBB02; -fx-font-size: 14px;");

        // Comment
        Label commentLabel = new Label("\"" + fb.commentaire + "\"");
        commentLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px; -fx-font-style: italic;");
        commentLabel.setWrapText(true);

        // Author
        Label authorLabel = new Label(fb.userName);
        authorLabel.setStyle("-fx-text-fill: #053536; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Category badge
        Label catLabel = new Label(fb.categorie);
        catLabel.setStyle(
                "-fx-text-fill: #0a4a4b; -fx-font-size: 10px; -fx-background-color: #e2f5f5; -fx-padding: 2 8; -fx-background-radius: 8;");

        card.getChildren().addAll(starsLabel, commentLabel, authorLabel, catLabel);
        return card;
    }

    private List<FeedbackItem> getRecentFeedbacks(int limit) {
        List<FeedbackItem> list = new ArrayList<>();
        String sql = "SELECT f.commentaire, f.note, f.categorie, CONCAT(u.prenom, ' ', u.nom) AS user_name " +
                "FROM feedback f JOIN user u ON f.idUser = u.id_user ORDER BY f.dateFeedback DESC LIMIT ?";
        try (Connection conn = DataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FeedbackItem fb = new FeedbackItem();
                fb.commentaire = rs.getString("commentaire");
                fb.note = rs.getInt("note");
                fb.categorie = rs.getString("categorie");
                fb.userName = rs.getString("user_name");
                list.add(fb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Simple DTO for feedback
    private static class FeedbackItem {
        String commentaire;
        int note;
        String categorie;
        String userName;
    }

    // ── Discover button ──────────────────────────────────

    @FXML
    private void onDiscoverClubs(ActionEvent event) {
        try {
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene scene = source.getScene();
            BorderPane root = (BorderPane) scene.getRoot();
            javafx.scene.Node sidebar = root.getLeft();
            if (sidebar != null) {
                javafx.scene.control.Button btnClubs = (javafx.scene.control.Button) sidebar.lookup("#btnClubs");
                if (btnClubs != null) {
                    btnClubs.fire();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Alert(Alert.AlertType.INFORMATION, "Navigation vers les clubs.", ButtonType.OK).showAndWait();
    }
}
