package Controllers.notification;

import Entite.Notification;
import Service.notification.INotificationService;
import Service.notification.NotificationService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class NotificationListController {

    @FXML
    private TableView<Notification> tableView;
    @FXML
    private TableColumn<Notification, Number> colId;
    @FXML
    private TableColumn<Notification, String> colTitle;
    @FXML
    private TableColumn<Notification, String> colType;
    @FXML
    private TableColumn<Notification, String> colRole;
    @FXML
    private TableColumn<Notification, Boolean> colRead;
    @FXML
    private TableColumn<Notification, String> colDate;
    @FXML
    private TableColumn<Notification, Void> colActions;

    private final INotificationService service = new NotificationService();
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType() != null ? c.getValue().getType().name() : ""));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecipientRole() != null ? c.getValue().getRecipientRole().name() : ""));
        colRead.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isReadStatus()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(FORMAT) : ""));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");

            {
                editBtn.getStyleClass().add("notif-btn");
                editBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 12;");
                deleteBtn.getStyleClass().add("notif-btn");
                deleteBtn.getStyleClass().add("notif-btn-danger");
                deleteBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 12;");

                editBtn.setOnAction(e -> {
                    Notification n = getTableRow().getItem();
                    if (n != null) openForm(n);
                });
                deleteBtn.setOnAction(e -> {
                    Notification n = getTableRow().getItem();
                    if (n != null) confirmDelete(n);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(10, editBtn, deleteBtn));
                }
            }
        });

        refresh();
    }

    @FXML
    private void onCreate() {
        openForm(null);
    }

    private void openForm(Notification n) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/notifications/NotificationForm.fxml"));
            Parent root = loader.load();
            NotificationFormController ctrl = loader.getController();
            ctrl.setNotification(n);
            ctrl.setOnSaved(this::refresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(n == null ? "Nouvelle notification" : "Modifier la notification");
            Scene scene = new Scene(root, 500, 420);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void confirmDelete(Notification n) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la notification ?");
        alert.setContentText("« " + n.getTitle() + " » sera définitivement supprimée.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.delete(n.getId());
                refresh();
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
            }
        }
    }

    private void refresh() {
        tableView.getItems().clear();
        try {
            tableView.getItems().addAll(service.findAll());
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les notifications: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
