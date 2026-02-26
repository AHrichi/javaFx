package Controllers.auth;

import Entite.Admin;
import Service.auth.AdminService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AdminLoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final AdminService adminService = new AdminService();

    @FXML
    private void onLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        Admin admin = adminService.loginAdmin(email, password);
        if (admin != null) {
            navigateToAdminDashboard(admin);
        } else {
            showError(adminService.getLastError() != null ? adminService.getLastError()
                    : "Email ou mot de passe incorrect.");
        }
    }

    @FXML
    private void onBackToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            emailField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToAdminDashboard(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/AdminDashboard.fxml"));
            Parent content = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setCurrentAdmin(admin);

            emailField.getScene().setRoot(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("auth-success");
        if (!errorLabel.getStyleClass().contains("auth-error")) {
            errorLabel.getStyleClass().add("auth-error");
        }
    }
}
