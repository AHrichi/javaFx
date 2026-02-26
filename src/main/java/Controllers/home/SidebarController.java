package Controllers.home;

import Utils.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class SidebarController {

    private BorderPane rootPane;

    public void setRootPane(BorderPane rootPane) {
        this.rootPane = rootPane;
    }

    @FXML
    private void onHome(ActionEvent event) {
        loadCenter("/home/home-content.fxml");
    }

    @FXML
    private void onClubs(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Module Clubs pas encore disponible.", ButtonType.OK).showAndWait();
    }

    @FXML
    private void onSeances(ActionEvent event) {
        loadCenter("/seance/afficherSeances.fxml");
    }

    @FXML
    private void onNotifications(ActionEvent event) {
        loadCenter("/notifications/NotificationsList.fxml");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SessionManager.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            rootPane.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClubCardClicked(MouseEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Détails du club à venir.", ButtonType.OK).showAndWait();
    }

    private void loadCenter(String fxmlPath) {
        if (rootPane == null)
            return;
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
