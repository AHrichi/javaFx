package Controllers.seance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import Entite.Membre;
import Entite.Seance;
import Service.user.ServiceMembre;
import Service.seance.ServiceParticipation;

import java.sql.SQLException;
import java.util.List;

public class AddParticipantController {

    @FXML private Label lblSeanceTitle;
    @FXML private ComboBox<Membre> comboMembre;
    @FXML private Label lblMessage;

    private Seance currentSeance;
    private final ServiceMembre serviceMembre = new ServiceMembre();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    // List to hold data for filtering
    private ObservableList<Membre> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadMembers();
    }

    public void setSeance(Seance s) {
        this.currentSeance = s;
        if (s != null) {
            lblSeanceTitle.setText("Ajout à : " + s.getTitre());
        }
    }

    private void loadMembers() {
        try {
            List<Membre> membres = serviceMembre.readAll();
            masterData.setAll(membres);

            // 1. Setup filtered list
            FilteredList<Membre> filteredItems = new FilteredList<>(masterData, p -> true);
            comboMembre.setItems(filteredItems);

            // 2. Define how Members look in the box (Name + Email)
            comboMembre.setConverter(new StringConverter<Membre>() {
                @Override
                public String toString(Membre m) {
                    return (m == null) ? "" : m.getNom() + " " + m.getPrenom() + " (" + m.getEmail() + ")";
                }

                @Override
                public Membre fromString(String string) {
                    return comboMembre.getItems().stream()
                            .filter(m -> toString(m).equals(string))
                            .findFirst().orElse(null);
                }
            });

            // 3. Add Typing Listener for Filter
            comboMembre.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                final Membre selected = comboMembre.getSelectionModel().getSelectedItem();

                // If user selected a real item, don't filter immediately
                if (selected != null && comboMembre.getConverter().toString(selected).equals(newText)) {
                    return;
                }

                // Run filter
                filteredItems.setPredicate(membre -> {
                    if (newText == null || newText.isEmpty()) return true;
                    String lowerText = newText.toLowerCase();

                    return membre.getNom().toLowerCase().contains(lowerText) ||
                            membre.getPrenom().toLowerCase().contains(lowerText) ||
                            membre.getEmail().toLowerCase().contains(lowerText);
                });

                // Show the dropdown if there are matches
                if (!filteredItems.isEmpty()) {
                    comboMembre.show();
                }
            });

        } catch (SQLException e) {
            showMessage("Erreur lors du chargement des membres : " + e.getMessage(), true);
        }
    }

    @FXML
    private void confirm() {
        if (currentSeance == null) return;

        // Get selected member
        Membre selected = comboMembre.getSelectionModel().getSelectedItem();

        // Handle case where user typed name but didn't click selection
        if (selected == null && !comboMembre.getEditor().getText().isEmpty()) {
            // Try to find exact match manually
            String currentText = comboMembre.getEditor().getText();
            selected = masterData.stream()
                    .filter(m -> (m.getNom() + " " + m.getPrenom() + " (" + m.getEmail() + ")").equals(currentText))
                    .findFirst()
                    .orElse(null);
        }

        if (selected == null) {
            showMessage("Veuillez sélectionner un membre valide dans la liste.", true);
            return;
        }

        // Add to Database
        String result = serviceParticipation.participer(currentSeance.getIdSeance(), selected.getIdUser());

        if (result.contains("réussie") || result.contains("success")) {
            closeWindow();
        } else {
            showMessage(result, true);
        }
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: green;");
        lblMessage.setVisible(true);
    }

    private void closeWindow() {
        if (lblSeanceTitle.getScene() != null) {
            ((Stage) lblSeanceTitle.getScene().getWindow()).close();
        }
    }
}