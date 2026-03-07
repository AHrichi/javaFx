package Controllers.home;

import Entite.Notification;
import Service.notification.NotificationService;
import Utils.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SidebarController {

    private BorderPane rootPane;

    @FXML
    private VBox menuContainer;
    @FXML
    private VBox bottomContainer;

    private final List<Button> menuButtons = new ArrayList<>();
    private Button defaultButton;

    public void setRootPane(BorderPane rootPane) {
        this.rootPane = rootPane;
    }

    @FXML
    public void initialize() {
        String role = SessionManager.getRole();
        if (role == null)
            role = "membre";

        buildProfileHeader();
        buildMenuItems(role);
        buildBottomItems();

        // Auto-generate session reminders for upcoming sessions (within 24h)
        Service.notification.AutoNotificationService.notifySessionReminders();

        // Refresh the red dot badge AFTER auto-notifications are created
        refreshNotifBadge();

        // Auto-select first menu item
        if (defaultButton != null) {
            defaultButton.fire();
        }
    }

    // ── Profile Header ──────────────────────────────────

    private void buildProfileHeader() {
        VBox profileBox = new VBox(4);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.getStyleClass().add("sidebar-profile");
        profileBox.setPadding(new Insets(0, 0, 10, 0));

        // Profile photo as a filled circle
        double radius = 24;
        Circle photoCircle = new Circle(radius);
        photoCircle.setStroke(javafx.scene.paint.Color.web("#ffffff30"));
        photoCircle.setStrokeWidth(2);

        String photoPath = SessionManager.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                Image img = new Image(getClass().getResourceAsStream(photoPath),
                        0, 0, true, true);
                photoCircle.setFill(new javafx.scene.paint.ImagePattern(img));
            } catch (Exception ignored) {
                photoCircle.setFill(javafx.scene.paint.Color.web("#3a3f5c"));
            }
        } else {
            photoCircle.setFill(javafx.scene.paint.Color.web("#3a3f5c"));
        }

        // Display name
        Label nameLabel = new Label(SessionManager.getDisplayName());
        nameLabel.setStyle("-fx-text-fill: #ffffffcc; -fx-font-size: 12px;");

        profileBox.getChildren().addAll(photoCircle, nameLabel);
        menuContainer.getChildren().add(profileBox);
    }

    // ── Menu Builder ────────────────────────────────────

    private void buildMenuItems(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                buildAdminMenu();
                break;
            case "Coach":
                buildCoachMenu();
                break;
            case "membre":
            default:
                buildMembreMenu();
                break;
        }
    }

    private void buildAdminMenu() {
        // dashboard: &#xE871;
        defaultButton = addMenuItem("Dashboard", "\uE871", "/home/home-content.fxml");
        // people: &#xE7FB;
        addMenuItem("Utilisateurs", "\uE7FB", "/auth/AdminDashboard.fxml");
        // club: &#xE7EF;
        addMenuItem("Gestion Clubs", "\uE7EF", "/club/club.fxml");
        // event: &#xEA65;
        addMenuItem("Gestion Événements", "\uEA65", "/evenement/GestionEvenement.fxml");
    }

    private void buildCoachMenu() {
        // home: &#xE88A;
        defaultButton = addMenuItem("Accueil", "\uE88A", "/home/home-content.fxml");
        // dashboard: &#xE871;
        addMenuPlaceholder("Dashboard", "\uE871");
        // event: &#xE878;
        addMenuItem("Séances", "\uE878", "/seance/afficherSeances.fxml");
        // chat: &#xE0B7;
        addMenuItem("AI Assistant", "\uE0B7", "/chatbot/chatbot.fxml");
        // group: &#xE7EF;
        addMenuPlaceholder("Membres", "\uE7EF");
        // celebration: &#xEA65;
        addMenuPlaceholder("Événements", "\uEA65");
    }

    private void buildMembreMenu() {
        // home: &#xE88A;
        defaultButton = addMenuItem("Accueil", "\uE88A", "/home/home-content.fxml");
        // groups: &#xE7EF;
        addMenuItem("Clubs", "\uE7EF", "/club/club_user.fxml");
        // event: &#xE878;
        addMenuItem("Séances", "\uE878", "/seance/afficherSeances.fxml");
        // celebration: &#xEA65;
        addMenuItem("Événements", "\uEA65", "/evenement/evenement_user.fxml");
        // fitness: &#xE020;
        addMenuItem("Activités", "\uE020", "/activity/ExploreActivities.fxml");
        // flag: &#xE153;
        addMenuPlaceholder("Mes objectifs", "\uE153");
        // chat: &#xE0B7;
        addMenuItem("AI Assistant", "\uE0B7", "/chatbot/chatbot.fxml");
    }

    // ── Bottom Items ────────────────────────────────────

    private void buildBottomItems() {
        String role = SessionManager.getRole();
        if (role == null)
            role = "membre";

        // notifications: &#xE7F4;
        if (role.equalsIgnoreCase("admin")) {
            addBottomNotifMenuItem("Notifications", "\uE7F4", "/notifications/AdminNotifications.fxml");
        } else {
            addBottomNotifMenuItem("Notifications", "\uE7F4", "/notifications/UserNotifications.fxml");
        }

        // person: &#xE7FD;
        addBottomPlaceholder("Profil", "\uE7FD");

        // logout: &#xE9BA;
        Button logoutBtn = createMenuButton("Déconnexion", "\uE9BA");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(this::onLogout);
        bottomContainer.getChildren().add(logoutBtn);
    }

    private void addBottomNotifMenuItem(String text, String icon, String fxmlPath) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("material-icon");

        notifDot = new Circle(4);
        notifDot.setFill(javafx.scene.paint.Color.web("#e74c3c"));
        notifDot.setVisible(false);

        StackPane iconWithBadge = new StackPane(iconLabel, notifDot);
        StackPane.setAlignment(notifDot, Pos.TOP_RIGHT);
        iconWithBadge.setPrefWidth(24);
        iconWithBadge.setPrefHeight(24);

        Button btn = new Button(text);
        btn.setGraphic(iconWithBadge);
        btn.getStyleClass().add("menu-btn");
        btn.setAlignment(Pos.BASELINE_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            updateActiveState(btn);
            loadCenter(fxmlPath);
        });
        bottomContainer.getChildren().add(btn);
        menuButtons.add(btn);
    }

    // ── Button Factories ────────────────────────────────

    private Button addMenuItem(String text, String icon, String fxmlPath) {
        Button btn = createMenuButton(text, icon);
        btn.setOnAction(e -> {
            updateActiveState(btn);
            loadCenter(fxmlPath);
        });
        menuContainer.getChildren().add(btn);
        menuButtons.add(btn);
        return btn;
    }

    private Button addMenuPlaceholder(String text, String icon) {
        Button btn = createMenuButton(text, icon);
        btn.setOnAction(e -> {
            updateActiveState(btn);
            showComingSoon(text);
        });
        menuContainer.getChildren().add(btn);
        menuButtons.add(btn);
        return btn;
    }

    private void addBottomPlaceholder(String text, String icon) {
        Button btn = createMenuButton(text, icon);
        btn.setOnAction(e -> showComingSoon(text));
        bottomContainer.getChildren().add(btn);
    }

    private Button createMenuButton(String text, String icon) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("material-icon");

        Button btn = new Button(text);
        btn.setGraphic(iconLabel);
        btn.getStyleClass().add("menu-btn");
        btn.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private static Circle notifDot;

    /** Re-checks unread count and shows/hides the red dot. */
    public static void refreshNotifBadge() {
        if (notifDot == null)
            return;
        try {
            Notification.RecipientRole role = SessionManager.getRecipientRole();
            Integer userId = SessionManager.getUserId();
            if (role != null) {
                int unread = new NotificationService().countUnread(role, userId);
                notifDot.setVisible(unread > 0);
            }
        } catch (Exception ignored) {
        }
    }

    // ── Navigation ──────────────────────────────────────

    private void loadCenter(String fxmlPath) {
        if (rootPane == null)
            return;
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            menuContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveState(Button clickedButton) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("active");
        }
        if (clickedButton != null) {
            clickedButton.getStyleClass().add("active");
        }
    }

    private void showComingSoon(String feature) {
        new Alert(Alert.AlertType.INFORMATION,
                feature + " — à venir prochainement.",
                ButtonType.OK).showAndWait();
    }
}
