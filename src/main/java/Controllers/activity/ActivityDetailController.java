package Controllers.activity;

import Entite.HomeActivity;
import Service.activity.ServiceHomeActivity;
import Service.activity.ServiceMemberActivity;
import Utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.time.LocalDate;

public class ActivityDetailController {

    @FXML
    private Label lblTitle;
    @FXML
    private Button btnBack;
    @FXML
    private VBox videoContainer;
    @FXML
    private WebView videoPlayer;
    @FXML
    private VBox imageContainer;
    @FXML
    private ImageView imgFallback;
    @FXML
    private Label lblInfo;
    @FXML
    private WebView webDescription;

    private HomeActivity currentActivity;
    private String previousViewPath;

    private final ServiceHomeActivity serviceHomeActivity = new ServiceHomeActivity();
    private final ServiceMemberActivity serviceMemberActivity = new ServiceMemberActivity();

    public void setExerciseData(HomeActivity ex, String previousPath) {
        this.currentActivity = ex;
        this.previousViewPath = previousPath;

        lblTitle.setText(ex.getTitre() != null ? ex.getTitre() : "Détails de l'exercice");

        // Info labels
        String diff = ex.getDifficulte() != null ? " • Niveau " + ex.getDifficulte() : "";
        lblInfo.setText("Catégorie : " + (ex.getCategorie() != null ? ex.getCategorie() : "N/A") + "\n" +
                "Muscles : " + (ex.getMuscles() != null && !ex.getMuscles().isEmpty() ? ex.getMuscles() : "N/A") + "\n"
                +
                "Équipement : "
                + (ex.getEquipement() != null && !ex.getEquipement().isEmpty() ? ex.getEquipement() : "Aucun") +
                diff);

        // Description is often HTML in Wger API, so we use a WebView to render it
        if (ex.getDescription() != null && !ex.getDescription().isEmpty()) {
            webDescription.getEngine().loadContent("<html><body style='font-family:sans-serif; color:#444;'>"
                    + ex.getDescription() + "</body></html>");
        } else {
            webDescription.getEngine().loadContent(
                    "<html><body style='font-family:sans-serif; color:#444;'>Aucune description disponible.</body></html>");
        }

        // Handle Video or Image
        if (ex.getVideoUrl() != null && !ex.getVideoUrl().isEmpty()) {
            videoContainer.setVisible(true);
            videoContainer.setManaged(true);

            // Render video with native HTML5 player. Removing the type='video/mp4'
            // constraint
            // allows the WebKit engine to automatically handle .MOV or .mp4 files if codecs
            // are present.
            String videoHtml = "<html><body style='margin:0;background:#000;display:flex;align-items:center;justify-content:center;'>"
                    + "<video width='100%' controls autoplay>"
                    + "<source src='" + ex.getVideoUrl() + "'>"
                    + "Votre navigateur ne supporte pas la vidéo.</video></body></html>";

            Platform.runLater(() -> {
                videoPlayer.getEngine().loadContent(videoHtml);
            });

        } else if (ex.getImageUrl() != null && !ex.getImageUrl().isEmpty()) {
            imageContainer.setVisible(true);
            imageContainer.setManaged(true);
            try {
                Image img = new Image(ex.getImageUrl(), 500, 300, true, true, true);
                img.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        imageContainer.setVisible(false);
                        imageContainer.setManaged(false);
                    }
                });
                imgFallback.setImage(img);
            } catch (Exception e) {
                imageContainer.setVisible(false);
                imageContainer.setManaged(false);
            }
        }
    }

    @FXML
    private void goBack() {
        // Stop the video before navigating away
        if (videoPlayer != null && videoPlayer.getEngine() != null) {
            videoPlayer.getEngine().loadContent("");
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(previousViewPath));
            BorderPane content = loader.load();
            BorderPane rootPane = (BorderPane) btnBack.getScene().lookup("#rootPane");
            if (rootPane != null) {
                rootPane.setCenter(content);
            } else {
                btnBack.getScene().setRoot(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveAndPlan() {
        try {
            // Check if user is logged in
            if (SessionManager.getCurrentUser() == null) {
                return;
            }
            int memberId = SessionManager.getCurrentUser().getIdUser();

            // Save exercise locally if it doesn't exist
            serviceHomeActivity.ajouter(currentActivity);

            HomeActivity saved = serviceHomeActivity.findByApiId(currentActivity.getApiExerciseId());
            if (saved != null) {
                // Plan it for today
                boolean ok = serviceMemberActivity.planifier(memberId, saved.getId(), LocalDate.now());

                if (ok) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Activité planifiée");
                    alert.setHeaderText(null);
                    alert.setContentText("L'exercice a été ajouté à vos activités d'aujourd'hui !");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Déjà planifiée");
                    alert.setHeaderText(null);
                    alert.setContentText("Cette activité est déjà dans votre liste pour aujourd'hui.");
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
