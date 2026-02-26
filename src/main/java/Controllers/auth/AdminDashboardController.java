package Controllers.auth;

import Entite.Admin;
import Entite.User;
import Service.auth.AdminService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label adminInfoLabel;
    @FXML
    private TabPane tabPane;

    // Users table
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> userIdCol;
    @FXML
    private TableColumn<User, String> userNomCol;
    @FXML
    private TableColumn<User, String> userPrenomCol;
    @FXML
    private TableColumn<User, String> userEmailCol;
    @FXML
    private TableColumn<User, String> userTypeCol;
    @FXML
    private TableColumn<User, String> userStatutCol;

    // Admins table
    @FXML
    private TableView<Admin> adminsTable;
    @FXML
    private TableColumn<Admin, Integer> adminIdCol;
    @FXML
    private TableColumn<Admin, String> adminNomCol;
    @FXML
    private TableColumn<Admin, String> adminPrenomCol;
    @FXML
    private TableColumn<Admin, String> adminEmailCol;
    @FXML
    private TableColumn<Admin, String> adminTelCol;
    @FXML
    private TableColumn<Admin, String> adminStatutCol;

    private final AdminService adminService = new AdminService();
    private Admin currentAdmin;

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        if (adminInfoLabel != null) {
            adminInfoLabel.setText("Bienvenue, " + admin.getNomComplet());
        }
        refreshUsers();
        refreshAdmins();
    }

    @FXML
    public void initialize() {
        // Users table columns
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        userNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        userPrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeCol.setCellValueFactory(new PropertyValueFactory<>("typeUser"));
        userStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Color-code user statut
        userStatutCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold;");
                    switch (item) {
                        case "actif":
                            setTextFill(Color.web("#27ae60"));
                            break;
                        case "en attente":
                            setTextFill(Color.web("#f39c12"));
                            break;
                        case "inactif":
                            setTextFill(Color.web("#c0392b"));
                            break;
                        default:
                            setTextFill(Color.BLACK);
                    }
                }
            }
        });

        // Admins table columns
        adminIdCol.setCellValueFactory(new PropertyValueFactory<>("idAdmin"));
        adminNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adminPrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        adminEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        adminTelCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        adminStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    // ===================== Users Actions =====================

    @FXML
    private void onRefreshUsers(ActionEvent event) {
        refreshUsers();
    }

    @FXML
    private void onApproveUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un utilisateur.", Alert.AlertType.WARNING);
            return;
        }
        if ("actif".equals(selected.getStatut())) {
            showAlert("Info", "Cet utilisateur est déjà actif.", Alert.AlertType.INFORMATION);
            return;
        }
        if (adminService.approveUser(selected.getIdUser())) {
            showAlert("Succès", "Utilisateur " + selected.getNom() + " " + selected.getPrenom() + " approuvé !",
                    Alert.AlertType.INFORMATION);
            refreshUsers();
        } else {
            showAlert("Erreur", "Impossible d'approuver l'utilisateur.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onDeactivateUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un utilisateur.", Alert.AlertType.WARNING);
            return;
        }
        if ("inactif".equals(selected.getStatut())) {
            showAlert("Info", "Cet utilisateur est déjà inactif.", Alert.AlertType.INFORMATION);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Désactiver " + selected.getNom() + " " + selected.getPrenom() + " ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (adminService.deactivateUser(selected.getIdUser())) {
                    showAlert("Succès", "Utilisateur désactivé.", Alert.AlertType.INFORMATION);
                    refreshUsers();
                }
            }
        });
    }

    @FXML
    private void onDeleteUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un utilisateur.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer " + selected.getNom() + " " + selected.getPrenom()
                        + " ?\nCette action est irréversible !");
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (adminService.deleteUser(selected.getIdUser())) {
                    showAlert("Succès", "Utilisateur supprimé.", Alert.AlertType.INFORMATION);
                    refreshUsers();
                } else {
                    showAlert("Erreur", "Impossible de supprimer l'utilisateur.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ===================== Admins Actions =====================

    @FXML
    private void onRefreshAdmins(ActionEvent event) {
        refreshAdmins();
    }

    @FXML
    private void onAddAdmin(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un administrateur");
        dialog.setHeaderText("Créer un nouveau compte admin");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        nomField.setPrefWidth(250);

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone (optionnel)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirmer mot de passe");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Téléphone:"), 0, 3);
        grid.add(telephoneField, 1, 3);
        grid.add(new Label("Mot de passe:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Confirmer:"), 0, 5);
        grid.add(confirmField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String password = passwordField.getText();
                String confirm = confirmField.getText();

                if (!password.equals(confirm)) {
                    showAlert("Erreur", "Les mots de passe ne correspondent pas.", Alert.AlertType.ERROR);
                    return;
                }

                String error = adminService.addAdmin(
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        emailField.getText().trim(),
                        password,
                        telephoneField.getText().trim());

                if (error == null) {
                    showAlert("Succès", "Admin créé avec succès !", Alert.AlertType.INFORMATION);
                    refreshAdmins();
                } else {
                    showAlert("Erreur", error, Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void onDeleteAdmin(ActionEvent event) {
        Admin selected = adminsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un admin.", Alert.AlertType.WARNING);
            return;
        }
        if (currentAdmin != null && selected.getIdAdmin() == currentAdmin.getIdAdmin()) {
            showAlert("Erreur", "Vous ne pouvez pas vous supprimer vous-même.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer l'admin " + selected.getNomComplet() + " ?");
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (adminService.deleteAdmin(selected.getIdAdmin())) {
                    showAlert("Succès", "Admin supprimé.", Alert.AlertType.INFORMATION);
                    refreshAdmins();
                } else {
                    showAlert("Erreur", "Impossible de supprimer l'admin.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ===================== Logout =====================

    @FXML
    private void onLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            tabPane.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== Helpers =====================

    private void refreshUsers() {
        usersTable.setItems(FXCollections.observableArrayList(adminService.getAllUsers()));
    }

    private void refreshAdmins() {
        adminsTable.setItems(FXCollections.observableArrayList(adminService.getAllAdmins()));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
