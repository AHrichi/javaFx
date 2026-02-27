package Controllers.seance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import Entite.Membre;
import Entite.Seance;
import Service.seance.ServiceParticipation;
import Utils.SessionManager;

import java.util.List;

public class ListeParticipantsController {

    @FXML private Label lblTitre;
    @FXML private Label lblCapacite;
    @FXML private TableView<Membre> tableParticipants;
    @FXML private TableColumn<Membre, String> colNom;
    @FXML private TableColumn<Membre, String> colPrenom;
    @FXML private TableColumn<Membre, String> colEmail;

    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    private Runnable onParticipantsChanged;

    public void setOnParticipantsChanged(Runnable callback) {
        this.onParticipantsChanged = callback;
    }

    public void setSeance(Seance s) {
        lblTitre.setText(s.getTitre());

        List<Membre> participants = serviceParticipation.getParticipants(s.getIdSeance());
        int currentCount = participants.size();
        int maxCapacity = s.getCapaciteMax();

        lblCapacite.setText("Participants : " + currentCount + " / " + maxCapacity);
        if (currentCount >= maxCapacity) {
            lblCapacite.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lblCapacite.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        Entite.User currentUser = SessionManager.getCurrentUser();
        boolean canKick = false;

        if (currentUser != null) {
            if ("Coach".equals(currentUser.getTypeUser())) {
                if (s.getIdCoach() == currentUser.getIdUser()) canKick = true;
            } else if (!"Membre".equals(currentUser.getTypeUser())) {
                canKick = true;
            }
        }

        if (canKick) {
            boolean colExists = tableParticipants.getColumns().stream().anyMatch(c -> "Action".equals(c.getText()));
            if (!colExists) {
                TableColumn<Membre, Void> colAction = new TableColumn<>("Action");
                colAction.setPrefWidth(100);
                colAction.setCellFactory(param -> new TableCell<>() {
                    private final Button btn = new Button("Retirer");
                    {
                        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-radius: 4;");
                        btn.setOnAction(event -> {
                            Membre m = getTableView().getItems().get(getIndex());
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous expulser " + m.getNom() + " de la séance ?", ButtonType.YES, ButtonType.NO);
                            confirm.showAndWait().ifPresent(res -> {
                                if (res == ButtonType.YES) {
                                    serviceParticipation.annulerParticipation(s.getIdSeance(), m.getIdUser());

                                    new Thread(() -> {
                                        String sujet = "Annulation de votre séance : " + s.getTitre();
                                        String contenu = "Bonjour " + m.getPrenom() + ",\n\n"
                                                + "Nous vous informons que votre participation à la séance '" + s.getTitre() + "' a été annulée par le coach.\n"
                                                + "Si vous avez des questions, n'hésitez pas à nous contacter.\n\n"
                                                + "L'équipe SportLink";

                                        Utils.EmailService.envoyerEmail(m.getEmail(), sujet, contenu);
                                    }).start();

                                    setSeance(s);
                                    if (onParticipantsChanged != null) onParticipantsChanged.run();
                                }
                            });
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else setGraphic(btn);
                    }
                });
                tableParticipants.getColumns().add(colAction);
            }
        }

        ObservableList<Membre> data = FXCollections.observableArrayList(participants);
        tableParticipants.setItems(data);
    }
}