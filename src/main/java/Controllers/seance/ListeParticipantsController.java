package Controllers.seance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import Entite.Membre;
import Entite.Seance;
import Service.seance.ServiceParticipation;

import java.util.List;

public class ListeParticipantsController {

    @FXML private Label lblTitre;
    @FXML private Label lblCapacite;
    @FXML private TableView<Membre> tableParticipants;
    @FXML private TableColumn<Membre, String> colNom;
    @FXML private TableColumn<Membre, String> colPrenom;
    @FXML private TableColumn<Membre, String> colEmail;

    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    public void setSeance(Seance s) {
        // 1. Set Labels
        lblTitre.setText(s.getTitre());

        // 2. Fetch Data
        List<Membre> participants = serviceParticipation.getParticipants(s.getIdSeance());
        int currentCount = participants.size();
        int maxCapacity = s.getCapaciteMax();

        // 3. Update Capacity Label
        lblCapacite.setText("Participants: " + currentCount + " / " + maxCapacity);
        if (currentCount >= maxCapacity) {
            lblCapacite.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red if full
        } else {
            lblCapacite.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green if space
        }

        // 4. Setup Table Columns (Mapping to Membre fields)
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 5. Load Data
        ObservableList<Membre> data = FXCollections.observableArrayList(participants);
        tableParticipants.setItems(data);
    }
}