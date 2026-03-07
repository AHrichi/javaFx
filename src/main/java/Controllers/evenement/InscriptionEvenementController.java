package Controllers.evenement;

import Entite.Evenement;
import Entite.InscriptionEvenement;
import Service.evenement.ServiceEvenement;
import Service.evenement.ServiceInscription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class InscriptionEvenementController {

    @FXML
    private ComboBox<Evenement> cbEvenementInscription;
    @FXML
    private TextField tfNomParticipant;
    @FXML
    private TextField tfEmailParticipant;
    @FXML
    private Label lblStatutInscription;

    @FXML
    private TableView<InscriptionEvenement> tableInscriptions;
    @FXML
    private TableColumn<InscriptionEvenement, Integer> colInscId;
    @FXML
    private TableColumn<InscriptionEvenement, String> colInscNom;
    @FXML
    private TableColumn<InscriptionEvenement, String> colInscEmail;
    @FXML
    private TableColumn<InscriptionEvenement, Date> colInscDate;

    private ServiceInscription serviceInscription;
    private ServiceEvenement serviceEvenement;

    @FXML
    public void initialize() {
        serviceInscription = new ServiceInscription();
        serviceEvenement = new ServiceEvenement();

        colInscId.setCellValueFactory(new PropertyValueFactory<>("idInscription"));
        colInscNom.setCellValueFactory(new PropertyValueFactory<>("nomParticipant"));
        colInscEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colInscDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        chargerEvenements();

        // Quand on change d'événement sélectionné, rafraîchir la liste des inscriptions
        cbEvenementInscription.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null)
                chargerInscriptionsPourEvenement(n.getIdEvenement());
        });
    }

    private void chargerEvenements() {
        try {
            List<Evenement> evenements = serviceEvenement.readAll();
            cbEvenementInscription.setItems(FXCollections.observableArrayList(evenements));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void chargerInscriptionsPourEvenement(int idEvenement) {
        try {
            ObservableList<InscriptionEvenement> list = FXCollections
                    .observableArrayList(serviceInscription.afficherParEvenement(idEvenement));
            tableInscriptions.setItems(list);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les inscriptions: " + e.getMessage());
        }
    }

    @FXML
    public void sInscrire() {
        Evenement selectedEv = cbEvenementInscription.getSelectionModel().getSelectedItem();
        String nom = tfNomParticipant.getText().trim();
        String email = tfEmailParticipant.getText().trim();

        // Validation
        if (selectedEv == null) {
            afficherStatut("⚠️ Veuillez sélectionner un événement.", "#e65100", false);
            return;
        }
        if (nom.isEmpty()) {
            afficherStatut("⚠️ Le nom du participant est obligatoire.", "#e65100", false);
            tfNomParticipant.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            afficherStatut("⚠️ Email invalide (format: exemple@domaine.com).", "#e65100", false);
            tfEmailParticipant.requestFocus();
            return;
        }

        // Vérifier si événement complet
        if (selectedEv.isComplet()) {
            afficherStatut("🔴 COMPLET — Cet événement n'a plus de places disponibles.", "#b71c1c", false);
            return;
        }

        try {
            // Vérifier doublon email
            if (serviceInscription.emailDejaInscrit(email, selectedEv.getIdEvenement())) {
                afficherStatut("⚠️ Cet email est déjà inscrit à cet événement.", "#e65100", false);
                return;
            }

            InscriptionEvenement inscription = new InscriptionEvenement(
                    nom, email, Date.valueOf(LocalDate.now()), selectedEv.getIdEvenement());

            boolean ok = serviceInscription.inscrire(inscription);
            if (ok) {
                afficherStatut("✅ Inscription réussie pour " + nom + " !", "#1b5e20", true);
                // Rafraîchir les données
                chargerEvenements();
                // Resélectionner le même événement (rechargé) pour que nbInscriptions soit à
                // jour
                for (Evenement ev : cbEvenementInscription.getItems()) {
                    if (ev.getIdEvenement() == selectedEv.getIdEvenement()) {
                        cbEvenementInscription.getSelectionModel().select(ev);
                        break;
                    }
                }
                chargerInscriptionsPourEvenement(selectedEv.getIdEvenement());
                tfNomParticipant.clear();
                tfEmailParticipant.clear();
            } else {
                afficherStatut("🔴 COMPLET — L'événement est complet, inscription refusée.", "#b71c1c", false);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void viderChampsInsc() {
        tfNomParticipant.clear();
        tfEmailParticipant.clear();
        cbEvenementInscription.getSelectionModel().clearSelection();
        tableInscriptions.getItems().clear();
        lblStatutInscription.setText("");
    }

    private void afficherStatut(String message, String couleur, boolean success) {
        lblStatutInscription.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        lblStatutInscription.setText(message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
