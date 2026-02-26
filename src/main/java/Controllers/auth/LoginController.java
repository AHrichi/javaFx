package Controllers.auth;

import Entite.User;
import Service.auth.AuthService;
import Utils.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            errorLabel.getStyleClass().removeAll("auth-success");
            errorLabel.getStyleClass().add("auth-error");
            return;
        }

        User user = authService.login(email, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            navigateToHome();
        } else {
            errorLabel.setText(authService.getLastError() != null ? authService.getLastError()
                    : "Email ou mot de passe incorrect.");
            errorLabel.getStyleClass().removeAll("auth-success");
            errorLabel.getStyleClass().add("auth-error");
        }
    }

    @FXML
    private void onGoToRegister(ActionEvent event) {
        switchScene("/auth/Register.fxml");
    }

    @FXML
    private void onGoToAdminLogin(ActionEvent event) {
        switchScene("/auth/AdminLogin.fxml");
    }

    private void navigateToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/home.fxml"));
            emailField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            emailField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
