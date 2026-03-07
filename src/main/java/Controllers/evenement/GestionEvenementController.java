package Controllers.evenement;

import Entite.Club;
import Entite.Evenement;
import Entite.InscriptionEvenement;
import Entite.User;
import Service.club.ServiceClub;
import Service.evenement.ServiceEvenement;
import Service.evenement.ServiceInscription;
import Service.user.ServiceUser;
import Service.ServiceDemande;
import Utils.EmailService;
import Entite.DemandeAdhesion;
import Entite.Feedback;
import Service.ServiceFeedback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Service.ApiService;

public class GestionEvenementController {

    @FXML
    private TextField tfNomEv;
    @FXML
    private TextArea tfDescriptionEv;
    @FXML
    private DatePicker dpDateEv;
    @FXML
    private TextField tfCapaciteEv;
    @FXML
    private TextField tfPrixEv;
    @FXML
    private ComboBox<Club> cbClubEv;
    @FXML
    private ComboBox<User> cbUserPart;
    @FXML
    private Label lblStatutEv;
    @FXML
    private Label lblTotalEv;
    @FXML
    private Label lblAvgCapacityEv;
    @FXML
    private ImageView ivEvPreview;
    @FXML
    private Label lblImagePathEv;

    @FXML
    private TableView<Evenement> tableEvenement;
    @FXML
    private TableColumn<Evenement, Integer> colEvId;
    @FXML
    private TableColumn<Evenement, String> colEvNom;
    @FXML
    private TableColumn<Evenement, Date> colEvDate;
    @FXML
    private TableColumn<Evenement, Double> colEvPrix;
    @FXML
    private TableColumn<Evenement, Integer> colEvCapacite;
    @FXML
    private TableColumn<Evenement, Integer> colEvPlaces;

    // Approvals Tab
    @FXML
    private TableView<DemandeAdhesion> tableDemandes;
    @FXML
    private TableColumn<DemandeAdhesion, Integer> colDemandeId;
    @FXML
    private TableColumn<DemandeAdhesion, String> colDemandeUser;
    @FXML
    private TableColumn<DemandeAdhesion, String> colDemandeEmail;
    @FXML
    private TableColumn<DemandeAdhesion, String> colDemandeEvent;
    @FXML
    private TableColumn<DemandeAdhesion, String> colDemandeDate;

    // Feedbacks Tab
    @FXML
    private TableView<Feedback> tableFeedbacks;
    @FXML
    private TableColumn<Feedback, String> colFeedbackUser;
    @FXML
    private TableColumn<Feedback, Integer> colFeedbackNote;
    @FXML
    private TableColumn<Feedback, String> colFeedbackCategorie;
    @FXML
    private TableColumn<Feedback, String> colFeedbackComment;
    @FXML
    private TableColumn<Feedback, String> colFeedbackDate;
    @FXML
    private javafx.scene.chart.PieChart statRatingsChart;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> statCategoriesChart;

    private ServiceEvenement serviceEv;
    private ServiceClub serviceClub;
    private ServiceInscription serviceIns;
    private ServiceUser serviceUser;
    private ApiService apiService;
    private ServiceDemande serviceDemande;
    private EmailService emailService;
    private final ServiceFeedback serviceFeedback = new ServiceFeedback();

    private ObservableList<Evenement> evenementList;
    private ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();

    // Calendar State
    private YearMonth currentYearMonth;
    private final Map<Integer, Map<LocalDate, String>> holidaysCache = new HashMap<>();

    @FXML
    public void initialize() {
        serviceDemande = new ServiceDemande();
        emailService = new EmailService();
        serviceEv = new ServiceEvenement();
        serviceClub = new ServiceClub();
        serviceIns = new ServiceInscription();
        serviceUser = new ServiceUser();
        apiService = new ApiService();

        // Configure TableView columns
        colEvId.setCellValueFactory(new PropertyValueFactory<>("idEvenement"));
        colEvNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEvDate.setCellValueFactory(new PropertyValueFactory<>("dateEvenement"));
        colEvPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEvCapacite.setCellValueFactory(new PropertyValueFactory<>("capaciteMax"));
        colEvPlaces.setCellValueFactory(new PropertyValueFactory<>("placesRestantes"));

        // Color "COMPLET" rows in red
        tableEvenement.setRowFactory(tv -> new TableRow<Evenement>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isComplet()) {
                    setStyle("-fx-background-color: #ffcdd2;");
                } else {
                    setStyle("");
                }
            }
        });

        // Afficher "COMPLET" dans la colonne places restantes
        colEvPlaces.setCellFactory(col -> new TableCell<Evenement, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    if (ev.isComplet()) {
                        setText("🔴 COMPLET");
                        setStyle("-fx-text-fill: #b71c1c; -fx-font-weight: bold;");
                    } else {
                        setText(String.valueOf(item));
                        setStyle("-fx-text-fill: #1b5e20;");
                    }
                }
            }
        });

        chargerClubs();
        chargerEvenements();
        chargerUsers();
        setupDemandesTable();
        chargerDemandes();
        try {
            setupFeedbacksTable();
            chargerFeedbacks();
        } catch (Exception e) {
            System.err.println("Feedback init error: " + e.getMessage());
        }

        // Sélection dans table → remplir le formulaire
        tableEvenement.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null)
                remplirChamps(n);
        });
    }

    private void chargerClubs() {
        try {
            cbClubEv.setItems(FXCollections.observableArrayList(serviceClub.readAll()));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les clubs : " + e.getMessage());
        }
    }

    private void chargerUsers() {
        try {
            cbUserPart.setItems(FXCollections.observableArrayList(serviceUser.readAll()));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    private void chargerEvenements() {
        try {
            evenementList = FXCollections.observableArrayList(serviceEv.readAll());
            tableEvenement.setItems(evenementList);
            calculerKPIs();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void calculerKPIs() {
        if (evenementList == null || evenementList.isEmpty()) {
            lblTotalEv.setText("0");
            lblAvgCapacityEv.setText("0 places");
            return;
        }

        lblTotalEv.setText(String.valueOf(evenementList.size()));

        double sumCap = 0;
        for (Evenement e : evenementList) {
            sumCap += e.getCapaciteMax();
        }
        double avgCap = sumCap / evenementList.size();
        lblAvgCapacityEv.setText(String.format("%.0f places", avgCap));
    }

    @FXML
    public void importerImageEv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                File uploadDir = new File("src/main/resources/uploads");
                if (!uploadDir.exists())
                    uploadDir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File dest = new File(uploadDir, fileName);
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                lblImagePathEv.setText(fileName);
                ivEvPreview.setImage(new Image(dest.toURI().toString()));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Failed to upload image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void ajouterEvenement() {
        if (!validerChamps())
            return;
        try {
            int cap = Integer.parseInt(tfCapaciteEv.getText().trim());
            double prix = Double.parseDouble(tfPrixEv.getText().trim());
            String imagePath = lblImagePathEv.getText().equals("Aucune image") ? "" : lblImagePathEv.getText();

            Evenement e = new Evenement(tfNomEv.getText().trim(), tfDescriptionEv.getText(),
                    java.sql.Date.valueOf(dpDateEv.getValue()), cap, prix, cbClubEv.getValue().getIdClub(), imagePath);
            if (serviceEv.ajouter(e)) {
                lblStatutEv.setStyle("-fx-text-fill: #1b5e20;");
                lblStatutEv.setText("✅ Événement ajouté avec succès!");
                chargerEvenements();
                clearFields();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifierEvenement() {
        Evenement selected = tableEvenement.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un événement.");
            return;
        }
        if (!validerChamps())
            return;

        try {
            int cap = Integer.parseInt(tfCapaciteEv.getText().trim());
            double prix = Double.parseDouble(tfPrixEv.getText().trim());
            String imagePath = lblImagePathEv.getText().equals("Aucune image") ? "" : lblImagePathEv.getText();

            selected.setNom(tfNomEv.getText().trim());
            selected.setDescription(tfDescriptionEv.getText());
            selected.setDateEvenement(java.sql.Date.valueOf(dpDateEv.getValue()));
            selected.setCapaciteMax(cap);
            selected.setPrix(prix);
            selected.setIdClub(cbClubEv.getValue().getIdClub());
            selected.setImage(imagePath);

            if (serviceEv.modifier(selected)) {
                lblStatutEv.setStyle("-fx-text-fill: #1b5e20;");
                lblStatutEv.setText("✅ Événement modifié avec succès!");
                chargerEvenements();
                clearFields();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerEvenement() {
        Evenement selected = tableEvenement.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un événement.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'événement");
        confirmation.setContentText("Supprimer \"" + selected.getNom() + "\" et toutes ses inscriptions ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                if (serviceEv.supprimer(selected)) {
                    lblStatutEv.setStyle("-fx-text-fill: #b71c1c;");
                    lblStatutEv.setText("🗑️ Événement supprimé.");
                    chargerEvenements();
                    clearFields();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    public void actualiserEvenements() {
        chargerEvenements();
        lblStatutEv.setText("🔄 Liste actualisée.");
    }

    @FXML
    public void viderChampsEv() {
        clearFields();
    }

    @FXML
    public void ouvrirInscriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evenement/InscriptionEvenement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("📝 Inscriptions aux Événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les inscriptions: " + e.getMessage());
        }
    }

    @FXML
    public void afficherEvenements() {
        chargerEvenements();
        Map<LocalDate, String> eventsMap = new HashMap<>();
        for (Evenement ev : evenementList) {
            if (ev.getDateEvenement() != null) {
                eventsMap.put(ev.getDateEvenement().toLocalDate(), ev.getNom());
            }
        }
        currentYearMonth = YearMonth.now();
        Stage stage = new Stage();
        stage.setTitle("📅 Calendrier des Événements");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10));
        Button btnPrev = new Button("<");
        Button btnNext = new Button(">");
        Text title = new Text();
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);

        btnPrev.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar(title, calendarGrid, eventsMap);
        });
        btnNext.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar(title, calendarGrid, eventsMap);
        });

        headerBox.getChildren().addAll(btnPrev, title, btnNext);
        root.setTop(headerBox);
        root.setCenter(calendarGrid);
        updateCalendar(title, calendarGrid, eventsMap);

        Scene scene = new Scene(root, 650, 550);
        stage.setScene(scene);
        stage.show();
    }

    private void updateCalendar(Text title, GridPane grid, Map<LocalDate, String> eventsMap) {
        grid.getChildren().clear();
        title.setText(currentYearMonth.getMonth().name() + " " + currentYearMonth.getYear());
        int year = currentYearMonth.getYear();
        if (!holidaysCache.containsKey(year)) {
            holidaysCache.put(year, apiService.getHolidays(year, "TN"));
        }
        Map<LocalDate, String> holidays = holidaysCache.get(year);

        String[] daysOfWeek = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int i = 0; i < daysOfWeek.length; i++) {
            StackPane cell = new StackPane(new Text(daysOfWeek[i]));
            cell.setPrefSize(80, 30);
            cell.setStyle("-fx-background-color: #ddd; -fx-border-color: #bbb; -fx-font-weight: bold;");
            grid.add(cell, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1;
        int row = 1, col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox cell = new VBox(2);
            cell.setPrefSize(85, 75);
            cell.setPadding(new Insets(2));
            Text dayText = new Text(String.valueOf(day));
            dayText.setStyle("-fx-font-weight: bold;");
            cell.getChildren().add(new HBox(dayText));

            String eventName = eventsMap.get(date);
            String holiday = holidays.get(date);
            String style = "-fx-border-color: #bbb; -fx-background-color: white;";

            if (holiday != null) {
                style = "-fx-border-color: #e53935; -fx-background-color: #ffcdd2;";
                Label holidayLbl = new Label(holiday);
                holidayLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #b71c1c;");
                cell.getChildren().add(holidayLbl);
            }
            if (eventName != null) {
                style = holiday != null ? "-fx-border-color: #7b1fa2; -fx-background-color: #e1bee7;"
                        : "-fx-border-color: #1e88e5; -fx-background-color: #bbdefb;";
                Label eventLbl = new Label(eventName);
                eventLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #0d47a1;");
                cell.getChildren().add(eventLbl);
            }
            if (date.equals(LocalDate.now()))
                style += "-fx-border-width: 2; -fx-border-color: #4CAF50;";
            cell.setStyle(style);
            grid.add(cell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    public void afficherStatistiques() {
        try {
            Map<String, Integer> data = serviceEv.getNbInscriptionsParEvenement();
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barChart.getData().add(series);
            Stage stage = new Stage();
            stage.setScene(new Scene(new VBox(barChart), 750, 500));
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les statistiques.");
        }
    }

    private void remplirChamps(Evenement ev) {
        tfNomEv.setText(ev.getNom());
        tfDescriptionEv.setText(ev.getDescription());
        dpDateEv.setValue(ev.getDateEvenement().toLocalDate());
        tfCapaciteEv.setText(String.valueOf(ev.getCapaciteMax()));
        tfPrixEv.setText(String.valueOf(ev.getPrix()));
        lblImagePathEv.setText(ev.getImage() != null && !ev.getImage().isEmpty() ? ev.getImage() : "Aucune image");
        if (ev.getImage() != null && !ev.getImage().isEmpty()) {
            File f = new File("src/main/resources/uploads/" + ev.getImage());
            if (f.exists())
                ivEvPreview.setImage(new Image(f.toURI().toString()));
        }
        for (Club c : cbClubEv.getItems()) {
            if (c.getIdClub() == ev.getIdClub()) {
                cbClubEv.getSelectionModel().select(c);
                break;
            }
        }
    }

    private void clearFields() {
        tfNomEv.clear();
        tfDescriptionEv.clear();
        dpDateEv.setValue(null);
        tfCapaciteEv.clear();
        tfPrixEv.clear();
        cbClubEv.getSelectionModel().clearSelection();
        lblImagePathEv.setText("Aucune image");
        ivEvPreview.setImage(null);
    }

    private boolean validerChamps() {
        if (tfNomEv.getText().trim().isEmpty() || dpDateEv.getValue() == null || cbClubEv.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Champs obligatoires manquants.");
            return false;
        }
        return true;
    }

    private void setupDemandesTable() {
        serviceDemande = new ServiceDemande();
        colDemandeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDemandeUser.setCellValueFactory(new PropertyValueFactory<>("nomUser"));
        colDemandeEmail.setCellValueFactory(new PropertyValueFactory<>("emailUser"));
        colDemandeEvent.setCellValueFactory(new PropertyValueFactory<>("nomEntite"));
        colDemandeDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
    }

    private void chargerDemandes() {
        try {
            tableDemandes.setItems(
                    FXCollections.observableArrayList(serviceDemande.readAllByType("EVENEMENT", "EN_ATTENTE")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void actualiserDemandes() {
        chargerDemandes();
    }

    @FXML
    public void approuverUser() {
        DemandeAdhesion selected = tableDemandes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        try {
            InscriptionEvenement ins = new InscriptionEvenement(selected.getNomUser(), selected.getEmailUser(),
                    new Date(System.currentTimeMillis()), selected.getIdEntite());
            if (serviceIns.inscrire(ins)) {
                if (serviceDemande.approuverDemande(selected.getId())) {
                    emailService.sendApprovalEmail(selected.getEmailUser(), selected.getNomEntite());
                    // Notify specified admin email
                    emailService.sendEmail("nedddda213@gmail.com",
                            "Notification Admin: Participant Approuvé",
                            "Le participant " + selected.getNomUser() + " a été approuvé pour l'événement: "
                                    + selected.getNomEntite());

                    chargerDemandes();
                    chargerEvenements();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void refuserUser() {
        DemandeAdhesion selected = tableDemandes.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        try {
            if (serviceDemande.refuserDemande(selected.getId())) {
                emailService.sendRefusalEmail(selected.getEmailUser(), selected.getNomEntite());
                // Notify specified admin email
                emailService.sendEmail("nedddda213@gmail.com",
                        "Notification Admin: Participant Refusé",
                        "Le participant " + selected.getNomUser() + " a été refusé pour l'événement: "
                                + selected.getNomEntite());

                chargerDemandes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFeedbacksTable() {
        colFeedbackUser.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        colFeedbackNote.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNote()));
        if (colFeedbackCategorie != null) {
            colFeedbackCategorie.setCellValueFactory(
                    cellData -> new SimpleStringProperty(cellData.getValue().getCategorie()));
        }
        colFeedbackComment
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCommentaire()));
        colFeedbackDate.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDateFeedback().toString()));
    }

    private void chargerFeedbacks() {
        List<Feedback> feedbacks = serviceFeedback.readAllByType("EVENEMENT");
        feedbackList.setAll(feedbacks);
        tableFeedbacks.setItems(feedbackList);
        mettreAJourGraphiques(feedbacks);
    }

    private void mettreAJourGraphiques(List<Feedback> feedbacks) {
        if (feedbacks == null || feedbacks.isEmpty()) {
            if (statRatingsChart != null)
                statRatingsChart.setData(FXCollections.emptyObservableList());
            if (statCategoriesChart != null)
                statCategoriesChart.setData(FXCollections.emptyObservableList());
            return;
        }

        // 1. Répartition des Notes (PieChart)
        if (statRatingsChart != null) {
            Map<Integer, Long> distribution = feedbacks.stream()
                    .collect(Collectors.groupingBy(Feedback::getNote, Collectors.counting()));

            ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();
            for (int i = 5; i >= 1; i--) {
                long count = distribution.getOrDefault(i, 0L);
                if (count > 0) {
                    pieData.add(new javafx.scene.chart.PieChart.Data(i + " Étoiles (" + count + ")", count));
                }
            }
            statRatingsChart.setData(pieData);
        }

        // 2. Moyenne par Catégorie (BarChart)
        if (statCategoriesChart != null) {
            Map<String, Double> avgByCategory = feedbacks.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getCategorie() != null ? f.getCategorie() : "Général",
                            Collectors.averagingInt(Feedback::getNote)));

            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            avgByCategory.forEach((cat, avg) -> series.getData().add(new javafx.scene.chart.XYChart.Data<>(cat, avg)));

            statCategoriesChart.getData().setAll(series);
        }
    }

    @FXML
    public void actualiserFeedbacks() {
        chargerFeedbacks();
    }

    @FXML
    public void supprimerFeedback() {
        Feedback selected = tableFeedbacks.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (serviceFeedback.supprimer(selected.getIdFeedback())) {
            chargerFeedbacks();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Feedback supprimé.");
        }
    }

    @FXML
    public void assignerParticipant() {
        Evenement selectedEvent = tableEvenement.getSelectionModel().getSelectedItem();
        User selectedUser = cbUserPart.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un événement dans la liste.");
            return;
        }
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur.");
            return;
        }
        if (selectedEvent.isComplet()) {
            showAlert(Alert.AlertType.WARNING, "Complet",
                    "Cet événement est complet, impossible d'assigner un participant.");
            return;
        }

        try {
            InscriptionEvenement ins = new InscriptionEvenement(
                    selectedUser.getNom() + " " + selectedUser.getPrenom(),
                    selectedUser.getEmail(),
                    new java.sql.Date(System.currentTimeMillis()),
                    selectedEvent.getIdEvenement());
            if (serviceIns.inscrire(ins)) {
                lblStatutEv.setStyle("-fx-text-fill: #1b5e20;");
                lblStatutEv.setText("✅ Participant assigné avec succès !");
                chargerEvenements();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'assigner le participant (déjà inscrit ?).");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'assignation : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
