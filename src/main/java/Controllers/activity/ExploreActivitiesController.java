package Controllers.activity;

import Entite.HomeActivity;
import Service.activity.ServiceHomeActivity;
import Service.activity.ServiceMemberActivity;
import Service.activity.WgerApiService;
import Utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExploreActivitiesController {

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> filterCategorie;
    @FXML
    private ComboBox<String> filterNiveau;
    @FXML
    private FlowPane exercisesGrid;
    @FXML
    private VBox loadingBox;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label lblLoading;

    private final WgerApiService apiService = new WgerApiService();
    private final ServiceHomeActivity serviceHomeActivity = new ServiceHomeActivity();
    private final ServiceMemberActivity serviceMemberActivity = new ServiceMemberActivity();

    private List<String[]> categories = new ArrayList<>(); // [id, name]
    private Integer selectedCategoryId = null;
    private String selectedNiveau = null;
    private final String[] niveaux = { "Facile", "Moyen", "Difficile" };

    @FXML
    public void initialize() {
        // Initialize Niveau Combobox
        filterNiveau.getItems().addAll("Tous", "Facile", "Moyen", "Difficile");

        loadCategories();
        loadExercises();

        // Search listener with delay
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            loadExercises();
        });

        // Category filter listener
        filterCategorie.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                for (String[] cat : categories) {
                    if (cat[1].equals(newVal)) {
                        selectedCategoryId = Integer.parseInt(cat[0]);
                        break;
                    }
                }
            } else {
                selectedCategoryId = null;
            }
            loadExercises();
        });

        // Niveau filter listener
        filterNiveau.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals("Tous")) {
                selectedNiveau = null;
            } else {
                selectedNiveau = newVal;
            }
            loadExercises();
        });
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                categories = apiService.fetchCategories();
                Platform.runLater(() -> {
                    for (String[] cat : categories) {
                        filterCategorie.getItems().add(cat[1]);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading categories: " + e.getMessage());
            }
        }).start();
    }

    private void loadExercises() {
        showLoading(true);
        exercisesGrid.getChildren().clear();

        new Thread(() -> {
            try {
                // Fetch more exercises (limit=60) since we don't have pagination
                List<HomeActivity> exercises;
                if (selectedCategoryId != null) {
                    exercises = apiService.fetchExercisesByCategory(selectedCategoryId, 2, 60, 0);
                } else {
                    exercises = apiService.fetchExercises(2, 60, 0);
                }

                // Filter by search text locally
                String search = txtSearch.getText();
                if (search != null && !search.isEmpty()) {
                    String lowerSearch = search.toLowerCase();
                    exercises.removeIf(e -> e.getTitre() == null || !e.getTitre().toLowerCase().contains(lowerSearch));
                }

                // Assign random but consistent difficulty based on ID, and filter locally
                exercises.removeIf(e -> {
                    String assignedNiveau = niveaux[e.getApiExerciseId() % 3];
                    e.setDifficulte(assignedNiveau);
                    return selectedNiveau != null && !assignedNiveau.equals(selectedNiveau);
                });

                List<HomeActivity> finalExercises = exercises;
                Platform.runLater(() -> {
                    for (HomeActivity ex : finalExercises) {
                        exercisesGrid.getChildren().add(createExerciseCard(ex));
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblLoading.setText("Erreur de connexion à l'API. Vérifiez votre connexion internet.");
                    progressIndicator.setVisible(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createExerciseCard(HomeActivity ex) {
        VBox card = new VBox(8);
        card.setPrefWidth(200);
        card.setPrefHeight(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
                "-fx-padding: 0; -fx-cursor: hand;");

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 12 12 0 0;");

        if (ex.getImageUrl() != null && !ex.getImageUrl().isEmpty()) {
            try {
                Image img = new Image(ex.getImageUrl(), 200, 140, true, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(140);
                iv.setPreserveRatio(true);

                // Add fallback if image fails to load (common with Wger API missing images)
                img.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        Platform.runLater(() -> {
                            imageContainer.getChildren().remove(iv);
                            Label noImg = new Label("🏋️");
                            noImg.setStyle("-fx-font-size: 40px;");
                            imageContainer.getChildren().add(0, noImg);
                        });
                    }
                });
                imageContainer.getChildren().add(iv);
            } catch (Exception e) {
                Label noImg = new Label("🏋️");
                noImg.setStyle("-fx-font-size: 40px;");
                imageContainer.getChildren().add(noImg);
            }
        } else {
            Label noImg = new Label("🏋️");
            noImg.setStyle("-fx-font-size: 40px;");
            imageContainer.getChildren().add(noImg);
        }

        // Video badge
        if (ex.getVideoUrl() != null && !ex.getVideoUrl().isEmpty()) {
            Label videoBadge = new Label("▶ Vidéo");
            videoBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8; " +
                    "-fx-background-radius: 10; -fx-font-size: 10px;");
            StackPane.setAlignment(videoBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(videoBadge, new Insets(8, 8, 0, 0));
            imageContainer.getChildren().add(videoBadge);
        }

        card.getChildren().add(imageContainer);

        // Text content
        VBox textBox = new VBox(4);
        textBox.setPadding(new Insets(0, 12, 8, 12));

        Label title = new Label(ex.getTitre() != null ? ex.getTitre() : "Exercice");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #00302e;");
        title.setWrapText(true);
        title.setMaxHeight(36);

        Label muscles = new Label(ex.getMuscles() != null && !ex.getMuscles().isEmpty() ? "💪 " + ex.getMuscles() : "");
        muscles.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        muscles.setWrapText(true);
        muscles.setMaxHeight(30);

        Label category = new Label(ex.getCategorie() != null ? ex.getCategorie() : "");
        category.setStyle("-fx-font-size: 10px; -fx-text-fill: #ffb800; -fx-font-weight: bold;");

        textBox.getChildren().addAll(title, muscles, category);
        card.getChildren().add(textBox);

        // Bottom buttons
        HBox buttons = new HBox(5);
        buttons.setPadding(new Insets(0, 12, 10, 12));
        buttons.setAlignment(Pos.CENTER);

        Button btnSave = new Button("⭐ Sauvegarder");
        btnSave.setStyle("-fx-background-color: #00302e; -fx-text-fill: white; -fx-background-radius: 8; " +
                "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10;");
        btnSave.setOnAction(e -> saveAndPlan(ex));

        buttons.getChildren().add(btnSave);
        card.getChildren().add(buttons);

        // Click card for detail
        card.setOnMouseClicked(e -> showExerciseDetail(ex));

        return card;
    }

    private void showExerciseDetail(HomeActivity ex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/ActivityDetail.fxml"));
            BorderPane content = loader.load();

            ActivityDetailController controller = loader.getController();
            controller.setExerciseData(ex, "/activity/ExploreActivities.fxml");

            BorderPane rootPane = (BorderPane) exercisesGrid.getScene().lookup("#rootPane");
            if (rootPane != null) {
                rootPane.setCenter(content);
            } else {
                exercisesGrid.getScene().setRoot(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAndPlan(HomeActivity ex) {
        try {
            // Save to DB (if not already saved)
            serviceHomeActivity.ajouter(ex);

            // Find saved activity (by API ID) to get the DB id
            HomeActivity saved = serviceHomeActivity.findByApiId(ex.getApiExerciseId());
            if (saved != null) {
                int memberId = SessionManager.getCurrentUser().getIdUser();
                boolean ok = serviceMemberActivity.planifier(memberId, saved.getId(), LocalDate.now());
                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Activité planifiée !",
                            "'" + ex.getTitre() + "' a été ajoutée à vos activités pour aujourd'hui.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Déjà planifiée",
                            "Cette activité est déjà planifiée pour aujourd'hui.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder : " + e.getMessage());
        }
    }

    // ── Filters ──────────────────

    @FXML
    private void clearFilters() {
        txtSearch.clear();
        filterCategorie.setValue(null);
        filterCategorie.getSelectionModel().clearSelection();
        filterCategorie.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "Catégorie" : item);
            }
        });

        filterNiveau.setValue(null);
        filterNiveau.getSelectionModel().clearSelection();
        filterNiveau.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "Niveau" : item);
            }
        });

        selectedCategoryId = null;
        selectedNiveau = null;
        loadExercises();
    }

    // ── Navigation ──────────────────

    @FXML
    private void showMyActivities() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/MyActivities.fxml"));
            BorderPane content = loader.load();

            // The outer layout from SidebarController has fx:id="rootPane"
            BorderPane rootPane = (BorderPane) exercisesGrid.getScene().lookup("#rootPane");
            if (rootPane != null) {
                rootPane.setCenter(content);
            } else {
                exercisesGrid.getScene().setRoot(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──────────────────

    private void showLoading(boolean show) {
        loadingBox.setVisible(show);
        loadingBox.setManaged(show);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
