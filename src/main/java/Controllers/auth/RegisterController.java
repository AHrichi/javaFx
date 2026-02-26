package Controllers.auth;

import Service.auth.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private RadioButton coachRadio;
    @FXML
    private RadioButton membreRadio;
    @FXML
    private ToggleGroup typeGroup;
    @FXML
    private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onRegister(ActionEvent event) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String typeUser = coachRadio.isSelected() ? "Coach" : "Membre";

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        String error = authService.register(nom, prenom, email, password, typeUser);
        if (error == null) {
            showSuccess("Inscription réussie ! Votre compte est en attente de validation.");
            // After 2 seconds, navigate to login
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::navigateToLogin);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } else {
            showError(error);
        }
    }

    @FXML
    private void onGoToLogin(ActionEvent event) {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            emailField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("auth-success");
        if (!messageLabel.getStyleClass().contains("auth-error")) {
            messageLabel.getStyleClass().add("auth-error");
        }
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("auth-error");
        if (!messageLabel.getStyleClass().contains("auth-success")) {
            messageLabel.getStyleClass().add("auth-success");
        }
    }
}
