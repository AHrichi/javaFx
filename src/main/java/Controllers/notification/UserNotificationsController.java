package Controllers.notification;

import Controllers.home.SidebarController;
import Entite.Notification;
import Service.notification.NotificationService;
import Utils.SessionManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserNotificationsController {

    @FXML
    private VBox notifCardsContainer;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblUnreadCount;

    private final NotificationService service = new NotificationService();

    @FXML
    public void initialize() {
        loadNotifications();
    }

    private void loadNotifications() {
        notifCardsContainer.getChildren().clear();

        Notification.RecipientRole role = SessionManager.getRecipientRole();
        Integer userId = SessionManager.getUserId();
        if (role == null)
            return;

        List<Notification> notifs = service.findByRoleOrUser(role, userId);
        int unread = 0;
        for (Notification n : notifs) {
            if (!n.isReadStatus())
                unread++;
        }

        lblUnreadCount.setText(unread > 0 ? unread + " non lue" + (unread > 1 ? "s" : "") : "Tout est lu ✓");
        lblUnreadCount.getStyleClass().removeAll("notif-unread-badge", "notif-all-read");
        lblUnreadCount.getStyleClass().add(unread > 0 ? "notif-unread-badge" : "notif-all-read");

        if (notifs.isEmpty()) {
            Label empty = new Label("Aucune notification pour le moment.");
            empty.getStyleClass().add("notif-empty");
            notifCardsContainer.getChildren().add(empty);
            return;
        }

        for (Notification n : notifs) {
            notifCardsContainer.getChildren().add(buildCard(n));
        }
    }

    private VBox buildCard(Notification n) {
        VBox card = new VBox(6);
        card.getStyleClass().add("notif-card");
        if (!n.isReadStatus()) {
            card.getStyleClass().add("notif-card-unread");
        }
        card.setPadding(new Insets(16, 20, 16, 20));

        // Top row: icon + title + time
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getTypeIcon(n.getType()));
        icon.getStyleClass().addAll("notif-card-icon", "notif-type-" + n.getType().name().toLowerCase());

        Label title = new Label(n.getTitle());
        title.getStyleClass().add("notif-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(formatRelativeTime(n.getCreatedAt()));
        time.getStyleClass().add("notif-card-time");

        topRow.getChildren().addAll(icon, title, spacer, time);

        // Message
        Label message = new Label(n.getMessage());
        message.getStyleClass().add("notif-card-message");
        message.setWrapText(true);

        // Bottom row: type badge + mark as read
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(n.getType().name());
        typeBadge.getStyleClass().addAll("notif-type-badge", "notif-type-" + n.getType().name().toLowerCase());

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        if (!n.isReadStatus()) {
            Label markRead = new Label("Marquer comme lu");
            markRead.getStyleClass().add("notif-mark-read");
            markRead.setOnMouseClicked(e -> {
                service.markAsRead(n.getId());
                loadNotifications();
                SidebarController.refreshNotifBadge();
            });
            bottomRow.getChildren().addAll(typeBadge, spacer2, markRead);
        } else {
            Label readLabel = new Label("✓ Lu");
            readLabel.getStyleClass().add("notif-read-label");
            bottomRow.getChildren().addAll(typeBadge, spacer2, readLabel);
        }

        card.getChildren().addAll(topRow, message, bottomRow);
        return card;
    }

    private String getTypeIcon(Notification.NotificationType type) {
        switch (type) {
            case ALERT:
                return "\uE002";
            case PAYMENT:
                return "\uE8A1";
            case SESSION:
                return "\uE878";
            case SYSTEM:
                return "\uE8B8";
            default:
                return "\uE88E";
        }
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1)
            return "À l'instant";
        if (minutes < 60)
            return "Il y a " + minutes + " min";
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24)
            return "Il y a " + hours + "h";
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7)
            return "Il y a " + days + "j";
        return dateTime.getDayOfMonth() + "/" + dateTime.getMonthValue() + "/" + dateTime.getYear();
    }
}
