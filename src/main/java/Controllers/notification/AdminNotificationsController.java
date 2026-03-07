package Controllers.notification;

import Controllers.home.SidebarController;
import Entite.Notification;
import Service.notification.NotificationService;
import Utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AdminNotificationsController {

    @FXML
    private TabPane tabPane;

    private final NotificationService service = new NotificationService();

    @FXML
    public void initialize() {
        // Tab 1: Admin's own notifications (card view)
        Tab myNotifsTab = new Tab("Mes notifications");
        myNotifsTab.setClosable(false);
        myNotifsTab.setContent(buildMyNotificationsView());

        // Tab 2: CRUD management
        Tab managementTab = new Tab("Gestion des notifications");
        managementTab.setClosable(false);
        try {
            Parent crudView = FXMLLoader.load(getClass().getResource("/notifications/NotificationsList.fxml"));
            managementTab.setContent(crudView);
        } catch (IOException e) {
            managementTab.setContent(new Label("Erreur chargement: " + e.getMessage()));
        }

        tabPane.getTabs().addAll(myNotifsTab, managementTab);
    }

    private VBox buildMyNotificationsView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("notif-user-page");
        root.setPadding(new Insets(40, 50, 40, 50));

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Mes notifications");
        titleLabel.getStyleClass().add("notif-page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeLabel = new Label();

        header.getChildren().addAll(titleLabel, spacer, badgeLabel);

        // Cards container
        VBox cardsContainer = new VBox(12);
        root.getChildren().addAll(header, cardsContainer);

        loadAdminNotifications(cardsContainer, badgeLabel);

        return root;
    }

    private void loadAdminNotifications(VBox container, Label badgeLabel) {
        container.getChildren().clear();

        Notification.RecipientRole role = Notification.RecipientRole.ADMIN;
        Integer userId = SessionManager.getUserId();

        List<Notification> notifs = service.findByRoleOrUser(role, userId);
        int unread = 0;
        for (Notification n : notifs) {
            if (!n.isReadStatus())
                unread++;
        }

        badgeLabel.setText(unread > 0 ? unread + " non lue" + (unread > 1 ? "s" : "") : "Tout est lu ✓");
        badgeLabel.getStyleClass().removeAll("notif-unread-badge", "notif-all-read");
        badgeLabel.getStyleClass().add(unread > 0 ? "notif-unread-badge" : "notif-all-read");

        if (notifs.isEmpty()) {
            Label empty = new Label("Aucune notification pour le moment.");
            empty.getStyleClass().add("notif-empty");
            container.getChildren().add(empty);
            return;
        }

        for (Notification n : notifs) {
            container.getChildren().add(buildCard(n, container, badgeLabel));
        }
    }

    private VBox buildCard(Notification n, VBox container, Label badgeLabel) {
        VBox card = new VBox(6);
        card.getStyleClass().add("notif-card");
        if (!n.isReadStatus()) {
            card.getStyleClass().add("notif-card-unread");
        }
        card.setPadding(new Insets(16, 20, 16, 20));

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

        Label message = new Label(n.getMessage());
        message.getStyleClass().add("notif-card-message");
        message.setWrapText(true);

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
                loadAdminNotifications(container, badgeLabel);
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
