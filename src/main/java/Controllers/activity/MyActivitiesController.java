package Controllers.activity;

import Entite.HomeActivity;
import Entite.MemberHomeActivity;
import Service.activity.ServiceMemberActivity;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyActivitiesController {

    @FXML
    private VBox activitiesContainer;
    @FXML
    private Label lblStats;
    @FXML
    private Button btnPlanifiees, btnCompletees;

    private final ServiceMemberActivity serviceMemberActivity = new ServiceMemberActivity();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private boolean showingPlanifiees = true;

    @FXML
    public void initialize() {
        int memberId = SessionManager.getCurrentUser().getIdUser();
        int weekCount = serviceMemberActivity.countCompletedThisWeek(memberId);
        lblStats.setText("Cette semaine : " + weekCount + " activité(s) complétée(s)");
        showPlanifiees();
    }

    @FXML
    public void showPlanifiees() {
        showingPlanifiees = true;
        btnPlanifiees.setStyle(
                "-fx-background-color: #ffb800; -fx-text-fill: #00302e; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnCompletees.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-background-radius: 20; -fx-cursor: hand;");

        int memberId = SessionManager.getCurrentUser().getIdUser();
        List<MemberHomeActivity> activities = serviceMemberActivity.getActivitesPlanifiees(memberId);
        displayActivities(activities);
    }

    @FXML
    public void showCompletees() {
        showingPlanifiees = false;
        btnCompletees.setStyle(
                "-fx-background-color: #ffb800; -fx-text-fill: #00302e; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnPlanifiees.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-background-radius: 20; -fx-cursor: hand;");

        int memberId = SessionManager.getCurrentUser().getIdUser();
        List<MemberHomeActivity> activities = serviceMemberActivity.getActivitesCompletees(memberId);
        displayActivities(activities);
    }

    private void displayActivities(List<MemberHomeActivity> activities) {
        activitiesContainer.getChildren().clear();

        if (activities.isEmpty()) {
            Label empty = new Label(
                    showingPlanifiees ? "Aucune activité planifiée.\nExplorez les exercices pour en ajouter !"
                            : "Aucune activité complétée pour le moment.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaa; -fx-padding: 40;");
            empty.setWrapText(true);
            activitiesContainer.getChildren().add(empty);
            return;
        }

        for (MemberHomeActivity ma : activities) {
            activitiesContainer.getChildren().add(createActivityRow(ma));
        }
    }

    private HBox createActivityRow(MemberHomeActivity ma) {
        HomeActivity a = ma.getActivite();

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        // Icon
        Label icon = new Label(showingPlanifiees ? "📅" : "✅");
        icon.setStyle("-fx-font-size: 24px;");

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(a != null ? a.getTitre() : "Activité");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #00302e;");

        String subText = "";
        if (a != null && a.getCategorie() != null)
            subText += a.getCategorie();
        if (a != null && a.getMuscles() != null && !a.getMuscles().isEmpty())
            subText += " • " + a.getMuscles();
        Label subtitle = new Label(subText);
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        String dateStr = "";
        if (showingPlanifiees && ma.getDatePlanifiee() != null) {
            dateStr = "Planifié : " + ma.getDatePlanifiee().format(dateFmt);
        } else if (!showingPlanifiees && ma.getDateCompletion() != null) {
            dateStr = "Complété : " + ma.getDateCompletion().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        }
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

        info.getChildren().addAll(title, subtitle, dateLabel);

        // Video button (if available)
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (a != null && a.getVideoUrl() != null && !a.getVideoUrl().isEmpty()) {
            Button btnVideo = new Button("▶ Vidéo");
            btnVideo.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 12;");
            btnVideo.setOnAction(e -> showVideo(a));
            actions.getChildren().add(btnVideo);
        }

        // Action buttons
        if (showingPlanifiees) {
            Button btnDone = new Button("✓ Complété");
            btnDone.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6 14;");
            btnDone.setOnAction(e -> {
                serviceMemberActivity.marquerComplete(ma.getId());
                refreshStats();
                showPlanifiees();
            });

            Button btnDelete = new Button("🗑");
            btnDelete.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 14px;");
            btnDelete.setOnAction(e -> {
                serviceMemberActivity.supprimer(ma.getId());
                showPlanifiees();
            });

            actions.getChildren().addAll(btnDone, btnDelete);
        }

        row.getChildren().addAll(icon, info, actions);
        return row;
    }

    private void showVideo(HomeActivity a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/ActivityDetail.fxml"));
            BorderPane content = loader.load();

            ActivityDetailController controller = loader.getController();
            controller.setExerciseData(a, "/activity/MyActivities.fxml");

            BorderPane rootPane = (BorderPane) activitiesContainer.getScene().lookup("#rootPane");
            if (rootPane != null) {
                rootPane.setCenter(content);
            } else {
                activitiesContainer.getScene().setRoot(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshStats() {
        int memberId = SessionManager.getCurrentUser().getIdUser();
        int weekCount = serviceMemberActivity.countCompletedThisWeek(memberId);
        lblStats.setText("Cette semaine : " + weekCount + " activité(s) complétée(s)");
    }

    @FXML
    public void showExplore() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/ExploreActivities.fxml"));
            BorderPane content = loader.load();

            // The outer layout from SidebarController has fx:id="rootPane"
            BorderPane rootPane = (BorderPane) activitiesContainer.getScene().lookup("#rootPane");
            if (rootPane != null) {
                rootPane.setCenter(content);
            } else {
                activitiesContainer.getScene().setRoot(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
