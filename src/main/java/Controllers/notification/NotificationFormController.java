package Controllers.notification;

import Entite.Notification;
import Service.notification.INotificationService;
import Service.notification.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NotificationFormController {

    @FXML
    private TextField fieldTitle;
    @FXML
    private TextArea fieldMessage;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private ComboBox<String> comboRole;

    private Notification notification;
    private Runnable onSaved;
    private final INotificationService service = new NotificationService();

    @FXML
    public void initialize() {
        comboType.getItems().addAll("INFO", "ALERT", "PAYMENT", "SESSION", "SYSTEM");
        comboType.setValue("INFO");
        comboRole.getItems().addAll("ADMIN", "COACH", "MEMBER");
        comboRole.setValue("ADMIN");
    }

    public void setNotification(Notification n) {
        this.notification = n;
        if (n != null) {
            fieldTitle.setText(n.getTitle());
            fieldMessage.setText(n.getMessage());
            comboType.setValue(n.getType() != null ? n.getType().name() : "INFO");
            comboRole.setValue(n.getRecipientRole() != null ? n.getRecipientRole().name() : "ADMIN");
        }
    }

    public void setOnSaved(Runnable r) {
        this.onSaved = r;
    }

    @FXML
    private void onSave() {
        String title = fieldTitle.getText();
        String message = fieldMessage.getText();
        if (title == null || title.trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Le titre est requis.").showAndWait();
            return;
        }
        if (message == null || message.trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Le message est requis.").showAndWait();
            return;
        }

        try {
            if (notification == null) {
                Notification n = new Notification(
                    title.trim(),
                    message.trim(),
                    Notification.RecipientRole.valueOf(comboRole.getValue()),
                    Notification.NotificationType.valueOf(comboType.getValue())
                );
                service.create(n);
            } else {
                notification.setTitle(title.trim());
                notification.setMessage(message.trim());
                notification.setRecipientRole(Notification.RecipientRole.valueOf(comboRole.getValue()));
                notification.setType(Notification.NotificationType.valueOf(comboType.getValue()));
                service.update(notification);
            }
            if (onSaved != null) onSaved.run();
            closeWindow();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) fieldTitle.getScene().getWindow()).close();
    }
}
