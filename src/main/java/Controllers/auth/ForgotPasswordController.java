package Controllers.auth;

import Service.auth.AuthService;
import Utils.SendEmailService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.security.SecureRandom;

public class ForgotPasswordController {

    // Step 1
    @FXML
    private VBox stepEmail;
    @FXML
    private TextField emailField;
    @FXML
    private Label emailErrorLabel;

    // Step 2
    @FXML
    private VBox stepCode;
    @FXML
    private TextField codeField;
    @FXML
    private Label codeErrorLabel;
    @FXML
    private Label codeSentLabel;

    // Step 3
    @FXML
    private VBox stepPassword;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label passwordErrorLabel;

    private final AuthService authService = new AuthService();
    private String generatedCode;
    private String userEmail;

    @FXML
    private void onSendCode(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError(emailErrorLabel, "Veuillez entrer votre email.");
            return;
        }

        if (!authService.userEmailExists(email)) {
            showError(emailErrorLabel, "Aucun compte trouvé avec cet email.");
            return;
        }

        userEmail = email;
        generatedCode = generateCode();

        // Send code via email in background
        new Thread(() -> {
            SendEmailService.envoyerEmail(email,
                    "SportLink - Code de réinitialisation",
                    "Bonjour,\n\n"
                            + "Votre code de vérification est : " + generatedCode + "\n\n"
                            + "Ce code est valable pour une seule utilisation.\n\n"
                            + "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n"
                            + "Cordialement,\nL'équipe SportLink");
        }).start();

        codeSentLabel.setText("Un code a été envoyé à " + email);
        showStep(stepCode);
    }

    @FXML
    private void onVerifyCode(ActionEvent event) {
        String enteredCode = codeField.getText().trim();
        if (enteredCode.isEmpty()) {
            showError(codeErrorLabel, "Veuillez entrer le code.");
            return;
        }

        if (!enteredCode.equals(generatedCode)) {
            showError(codeErrorLabel, "Code incorrect. Veuillez réessayer.");
            return;
        }

        showStep(stepPassword);
    }

    @FXML
    private void onResendCode(ActionEvent event) {
        generatedCode = generateCode();
        new Thread(() -> {
            SendEmailService.envoyerEmail(userEmail,
                    "SportLink - Nouveau code de réinitialisation",
                    "Bonjour,\n\n"
                            + "Votre nouveau code de vérification est : " + generatedCode + "\n\n"
                            + "Ce code est valable pour une seule utilisation.\n\n"
                            + "Cordialement,\nL'équipe SportLink");
        }).start();

        codeErrorLabel.setText("Un nouveau code a été envoyé.");
        codeErrorLabel.getStyleClass().removeAll("auth-error");
        if (!codeErrorLabel.getStyleClass().contains("auth-success")) {
            codeErrorLabel.getStyleClass().add("auth-success");
        }
    }

    @FXML
    private void onChangePassword(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(passwordErrorLabel, "Veuillez remplir tous les champs.");
            return;
        }
        if (newPassword.length() < 6) {
            showError(passwordErrorLabel, "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError(passwordErrorLabel, "Les mots de passe ne correspondent pas.");
            return;
        }

        if (authService.updatePassword(userEmail, newPassword)) {
            passwordErrorLabel.setText("Mot de passe modifié avec succès ! Redirection...");
            passwordErrorLabel.getStyleClass().removeAll("auth-error");
            if (!passwordErrorLabel.getStyleClass().contains("auth-success")) {
                passwordErrorLabel.getStyleClass().add("auth-success");
            }

            // Redirect to login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::navigateToLogin);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } else {
            showError(passwordErrorLabel, "Erreur lors de la modification du mot de passe.");
        }
    }

    @FXML
    private void onBackToLogin(ActionEvent event) {
        navigateToLogin();
    }

    private void showStep(VBox step) {
        stepEmail.setVisible(false);
        stepEmail.setManaged(false);
        stepCode.setVisible(false);
        stepCode.setManaged(false);
        stepPassword.setVisible(false);
        stepPassword.setManaged(false);

        step.setVisible(true);
        step.setManaged(true);
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            stepEmail.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("auth-success");
        if (!label.getStyleClass().contains("auth-error")) {
            label.getStyleClass().add("auth-error");
        }
    }
}
