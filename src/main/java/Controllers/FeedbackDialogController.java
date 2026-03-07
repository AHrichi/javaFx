package Controllers;

import Entite.Feedback;
import Service.ServiceFeedback;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

public class FeedbackDialogController {

    @FXML
    private HBox starContainer;
    @FXML
    private ComboBox<String> cbCategorie;
    @FXML
    private TextArea taCommentaire;

    private int selectedNote = 5;
    private int idEntite;
    private String typeEntite;
    private final ServiceFeedback serviceFeedback = new ServiceFeedback();

    @FXML
    public void initialize() {
        cbCategorie.setItems(FXCollections.observableArrayList(
                "Commentaire", "Amélioration", "Critique", "Non Satisfaction", "Satisfaction"));
        cbCategorie.getSelectionModel().selectFirst();
        updateStars();
    }

    public void setData(int id, String type) {
        this.idEntite = id;
        this.typeEntite = type;
        updateStars();
    }

    @FXML
    private void handleStarClick(MouseEvent event) {
        Label Star = (Label) event.getSource();
        selectedNote = Integer.parseInt(Star.getUserData().toString());
        updateStars();
    }

    private void updateStars() {
        for (int i = 0; i < starContainer.getChildren().size(); i++) {
            Label star = (Label) starContainer.getChildren().get(i);
            star.setText("★");
            if (i < selectedNote) {
                star.setStyle("-fx-text-fill: #eab308; -fx-cursor: hand;");
            } else {
                star.setStyle("-fx-text-fill: #cbd5e1; -fx-cursor: hand;");
            }
        }
    }

    @FXML
    private void handleEnvoyer() {
        if (SessionManager.getCurrentUser() == null) {
            showAlert("Erreur", "Vous devez être connecté pour laisser un avis.");
            return;
        }

        String categorie = cbCategorie.getValue() != null ? cbCategorie.getValue() : "Commentaire";

        Feedback f = new Feedback(
                SessionManager.getCurrentUser().getIdUser(),
                idEntite,
                typeEntite,
                taCommentaire.getText(),
                categorie,
                selectedNote);

        if (serviceFeedback.ajouter(f)) {
            showAlert("Succès", "Merci pour votre avis !");
            close();
        } else {
            showAlert("Erreur", "Impossible d'enregistrer votre avis.");
        }
    }

    @FXML
    private void handleAnnuler() {
        close();
    }

    private void close() {
        ((Stage) taCommentaire.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
