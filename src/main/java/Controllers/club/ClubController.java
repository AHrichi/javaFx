package Controllers.club;

import Entite.Club;
import Entite.User;
import Entite.AdhesionClub;
import Service.club.ServiceClub;
import Service.user.ServiceUser;
import Service.club.ServiceAdhesion;
import Service.ServiceDemande;
import Utils.EmailService;
import Entite.DemandeAdhesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.io.File;
import Service.ServiceFeedback;
import Entite.Feedback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

public class ClubController {

    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfAdresse;
    // Removed tfVille
    @FXML
    private TextField tfTelephone;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextArea tfDescription;
    @FXML
    private TextField tfNomSalle;
    @FXML
    private TextField tfCapacite;
    @FXML
    private ImageView ivClubPreview;
    @FXML
    private Label lblImagePath;

    // KPIs
    @FXML
    private Label lblTotalClubs;
    @FXML
    private Label lblAvgCapacity;

    @FXML
    private TableView<Club> tableClub;
    @FXML
    private TableColumn<Club, Integer> colId;
    @FXML
    private TableColumn<Club, String> colNom;
    // Removed colVille
    @FXML
    private TableColumn<Club, String> colTelephone;
    @FXML
    private TableColumn<Club, String> colEmail;
    @FXML
    private TableColumn<Club, Integer> colCapacite;
    @FXML
    private TableColumn<Club, Integer> colPlaces;
    @FXML
    private TableColumn<Club, String> colDescription;

    @FXML
    private ComboBox<User> cbUserMembre;
    @FXML
    private Label lblStatutAdhesion;

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
    private TableColumn<DemandeAdhesion, String> colDemandeClub;
    @FXML
    private TableColumn<DemandeAdhesion, String> colDemandeDate;

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

    private final ServiceFeedback serviceFeedback = new ServiceFeedback();
    private ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();

    private ServiceDemande serviceDemande;
    private EmailService emailService;

    private ServiceClub service;
    private ServiceUser serviceUser;
    private ServiceAdhesion serviceAdhesion;
    private ObservableList<Club> clubList;

    private static final String STYLE_ERROR = "-fx-border-color: #e53935; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;";
    private static final String STYLE_OK = "-fx-border-color: #43a047; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;";
    private static final String STYLE_DEFAULT = "";

    @FXML
    public void initialize() {
        serviceDemande = new ServiceDemande();
        emailService = new EmailService();
        service = new ServiceClub();
        serviceUser = new ServiceUser();
        serviceAdhesion = new ServiceAdhesion();

        colId.setCellValueFactory(new PropertyValueFactory<>("idClub"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("placesRestantes"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        setupUserComboBox();
        setupTableStyling();
        chargerClubs();
        chargerUsers();
        setupDemandesTable();
        chargerDemandes();
        try {
            setupFeedbacksTable();
            chargerFeedbacks();
        } catch (Exception e) {
            System.err.println("Feedback init error (Club): " + e.getMessage());
        }

        tableClub.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                remplirChamps(newSel);
            }
        });

        // Validations
        tfNom.textProperty().addListener(
                (obs, oldVal, newVal) -> validateText(tfNom, newVal, "Le nom ne doit pas contenir de chiffres"));

        tfTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty())
                tfTelephone.setStyle(STYLE_DEFAULT);
            else if (!newVal.matches("\\d{8}")) {
                tfTelephone.setStyle(STYLE_ERROR);
                tfTelephone.setTooltip(new Tooltip("Le téléphone doit contenir exactement 8 chiffres"));
            } else {
                tfTelephone.setStyle(STYLE_OK);
                tfTelephone.setTooltip(null);
            }
        });

        tfEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty())
                tfEmail.setStyle(STYLE_DEFAULT);
            else if (!newVal.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                tfEmail.setStyle(STYLE_ERROR);
                tfEmail.setTooltip(new Tooltip("Format attendu: exemple@domaine.com"));
            } else {
                tfEmail.setStyle(STYLE_OK);
                tfEmail.setTooltip(null);
            }
        });

        tfCapacite.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tfCapacite.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void validateText(TextField field, String val, String errorMsg) {
        if (val.isEmpty())
            field.setStyle(STYLE_DEFAULT);
        else if (val.matches(".*\\d.*")) {
            field.setStyle(STYLE_ERROR);
            field.setTooltip(new Tooltip(errorMsg));
        } else {
            field.setStyle(STYLE_OK);
            field.setTooltip(null);
        }
    }

    private void chargerClubs() {
        try {
            clubList = FXCollections.observableArrayList(service.readAll());
            tableClub.setItems(clubList);
            calculerKPIs();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les clubs: " + e.getMessage());
        }
    }

    private void calculerKPIs() {
        if (clubList == null)
            return;
        int total = clubList.size();
        lblTotalClubs.setText(String.valueOf(total));

        if (total > 0) {
            double avg = clubList.stream().mapToInt(Club::getCapacite).average().orElse(0.0);
            lblAvgCapacity.setText(String.format("%.0f places", avg));
        } else {
            lblAvgCapacity.setText("0 places");
        }
    }

    private void setupUserComboBox() {
        cbUserMembre.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User u) {
                return (u == null) ? "" : u.getNom() + " " + u.getPrenom() + " (" + u.getEmail() + ")";
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });
    }

    private void setupTableStyling() {
        tableClub.setRowFactory(tv -> new TableRow<Club>() {
            @Override
            protected void updateItem(Club item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getPlacesRestantes() <= 0) {
                    setStyle("-fx-background-color: #ffebee;"); // Rouge très clair si complet
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void chargerUsers() {
        try {
            cbUserMembre.setItems(FXCollections.observableArrayList(serviceUser.readAll()));
        } catch (Exception e) {
            System.err.println("Erreur chargement users: " + e.getMessage());
        }
    }

    @FXML
    public void ajouterClub() {
        if (!validerChamps())
            return;
        int capacite = 0;
        try {
            if (!tfCapacite.getText().trim().isEmpty()) {
                capacite = Integer.parseInt(tfCapacite.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La capacité doit être un nombre valide.");
            return;
        }

        try {
            Club c = new Club(
                    tfNom.getText().trim(),
                    tfAdresse.getText().trim(),
                    "", // Ville supprimée
                    tfTelephone.getText().trim(),
                    tfEmail.getText().trim(),
                    java.sql.Date.valueOf(LocalDate.now()),
                    tfDescription.getText().trim(),
                    "", // Evenements vide
                    lblImagePath.getText().equals("Aucune image") ? "" : lblImagePath.getText(),
                    tfNomSalle.getText().trim(),
                    capacite,
                    1);

            if (service.clubExiste(c.getNom(), c.getDescription())) {
                showAlert(Alert.AlertType.WARNING, "Doublon", "Un club avec ce nom et cette description existe déjà !");
                return;
            }

            if (service.ajouter(c)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Club ajouté avec succès!");
                chargerClubs();
                clearFields();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void importerLogo() {
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
                lblImagePath.setText(fileName);
                ivClubPreview.setImage(new Image(dest.toURI().toString()));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Failed to upload logo: " + e.getMessage());
            }
        }
    }

    @FXML
    public void modifierClub() {
        Club selected = tableClub.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un club à modifier.");
            return;
        }
        if (!validerChamps())
            return;
        try {
            selected.setNom(tfNom.getText().trim());
            selected.setAdresse(tfAdresse.getText().trim());
            selected.setVille(""); // Ville supprimée
            selected.setTelephone(tfTelephone.getText().trim());
            selected.setEmail(tfEmail.getText().trim());
            selected.setDescription(tfDescription.getText().trim());
            selected.setPhotoClub(lblImagePath.getText().equals("Aucune image") ? "" : lblImagePath.getText());
            selected.setPhotoSalle(tfNomSalle.getText().trim());
            int newCapacite = selected.getCapacite();
            try {
                String txt = tfCapacite.getText().trim();
                if (!txt.isEmpty()) {
                    newCapacite = Integer.parseInt(txt);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La capacité doit être un nombre valide.");
                return;
            }

            // Ajuster placesRestantes si la capacité change
            int diff = newCapacite - selected.getCapacite();
            selected.setCapacite(newCapacite);
            selected.setPlacesRestantes(selected.getPlacesRestantes() + diff);

            if (service.modifier(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Club modifié avec succès!");
                chargerClubs();
                clearFields();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerClub() {
        Club selected = tableClub.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un club à supprimer.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer le club " + selected.getNom() + " ?", ButtonType.OK, ButtonType.CANCEL);
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                if (service.supprimer(selected)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Club supprimé avec succès!");
                    chargerClubs();
                    clearFields();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression: " + e.getMessage());
            }
        }
    }

    private void remplirChamps(Club c) {
        tfNom.setText(c.getNom() != null ? c.getNom() : "");
        tfAdresse.setText(c.getAdresse() != null ? c.getAdresse() : "");
        tfTelephone.setText(c.getTelephone() != null ? c.getTelephone() : "");
        tfEmail.setText(c.getEmail() != null ? c.getEmail() : "");
        tfDescription.setText(c.getDescription() != null ? c.getDescription() : "");
        tfNomSalle.setText(c.getPhotoSalle() != null ? c.getPhotoSalle() : "");
        tfCapacite.setText(String.valueOf(c.getCapacite()));
        lblImagePath
                .setText(c.getPhotoClub() != null && !c.getPhotoClub().isEmpty() ? c.getPhotoClub() : "Aucune image");
        if (c.getPhotoClub() != null && !c.getPhotoClub().isEmpty()) {
            File f = new File("src/main/resources/uploads/" + c.getPhotoClub());
            if (f.exists()) {
                ivClubPreview.setImage(new Image(f.toURI().toString()));
            } else {
                ivClubPreview.setImage(null);
            }
        } else {
            ivClubPreview.setImage(null);
        }
    }

    private void clearFields() {
        tfNom.clear();
        tfAdresse.clear();
        tfTelephone.clear();
        tfEmail.clear();
        tfDescription.clear();
        tfNomSalle.clear();
        tfCapacite.clear();
        lblImagePath.setText("Aucune image");
        ivClubPreview.setImage(null);
        tableClub.getSelectionModel().clearSelection();
        tfNom.setStyle(STYLE_DEFAULT);
        tfTelephone.setStyle(STYLE_DEFAULT);
        tfEmail.setStyle(STYLE_DEFAULT);
    }

    @FXML
    public void viderChamps() {
        clearFields();
    }

    @FXML
    public void assignerUtilisateur() {
        Club selectedClub = tableClub.getSelectionModel().getSelectedItem();
        User selectedUser = cbUserMembre.getValue();

        if (selectedClub == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un club dans la table.");
            return;
        }

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur à assigner.");
            return;
        }

        try {
            if (serviceAdhesion.emailDejaAdherent(selectedUser.getEmail(), selectedClub.getIdClub())) {
                showAlert(Alert.AlertType.WARNING, "Doublon", "Cet utilisateur est déjà membre de ce club.");
                return;
            }

            AdhesionClub adhesion = new AdhesionClub(
                    selectedUser.getNom() + " " + selectedUser.getPrenom(),
                    selectedUser.getEmail(),
                    selectedClub.getIdClub());

            if (serviceAdhesion.adherer(adhesion)) {
                lblStatutAdhesion.setStyle("-fx-text-fill: #1b5e20;");
                lblStatutAdhesion.setText("✅ Utilisateur assigné avec succès !");
                cbUserMembre.setValue(null);
                chargerClubs(); // Rafraîchir pour voir les places restantes
            } else {
                showAlert(Alert.AlertType.ERROR, "Complet", "Impossible d'assigner : le club est complet !");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'assignation a échoué : " + e.getMessage());
        }
    }

    private boolean validerChamps() {
        if (tfNom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Nom obligatoire.");
            return false;
        }
        if (!tfCapacite.getText().trim().isEmpty()) {
            try {
                int cap = Integer.parseInt(tfCapacite.getText().trim());
                if (cap < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La capacité doit être un nombre positif.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La capacité doit être un nombre entier valide.");
                return false;
            }
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void afficherStatistiques() {
        if (clubList == null || clubList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun club à analyser.");
            return;
        }

        // Grouper les clubs par Mois-Année de création
        java.util.Map<String, Integer> clubsParMois = new java.util.HashMap<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");

        for (Club c : clubList) {
            if (c.getDateCreation() != null) {
                java.time.LocalDate localDate = c.getDateCreation().toLocalDate();
                String moisAnnee = localDate.format(formatter);
                clubsParMois.put(moisAnnee, clubsParMois.getOrDefault(moisAnnee, 0) + 1);
            }
        }

        // Création du BarChart
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        xAxis.setLabel("Mois de Création");

        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Nombre de Clubs");

        javafx.scene.chart.BarChart<String, Number> barChart = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        barChart.setTitle("Évolution des Créations de Clubs");
        barChart.setLegendVisible(false);

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Clubs");

        // Trier par date pour un affichage chronologique
        clubsParMois.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(entry -> series.getData()
                        .add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue())));

        barChart.getData().add(series);

        javafx.scene.Scene scene = new javafx.scene.Scene(barChart, 800, 600);

        // Ajouter un peu de style au graphique
        scene.getStylesheets()
                .add(getClass().getResource("/css/style.css") != null
                        ? getClass().getResource("/css/style.css").toExternalForm()
                        : "");

        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Statistiques Temporelles des Clubs");
        stage.setScene(scene);
        stage.show();
    }

    private void setupDemandesTable() {
        serviceDemande = new ServiceDemande();
        colDemandeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDemandeUser.setCellValueFactory(new PropertyValueFactory<>("nomUser"));
        colDemandeEmail.setCellValueFactory(new PropertyValueFactory<>("emailUser"));
        colDemandeClub.setCellValueFactory(new PropertyValueFactory<>("nomEntite"));
        colDemandeDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
    }

    private void chargerDemandes() {
        try {
            tableDemandes
                    .setItems(FXCollections.observableArrayList(serviceDemande.readAllByType("CLUB", "EN_ATTENTE")));
        } catch (Exception e) {
            System.err.println("Erreur chargement demandes: " + e.getMessage());
        }
    }

    @FXML
    public void actualiserDemandes() {
        chargerDemandes();
    }

    @FXML
    public void approuverUser() {
        DemandeAdhesion selected = tableDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande à approuver.");
            return;
        }

        try {
            // 1. Ajouter l'utilisateur au club
            AdhesionClub adhesion = new AdhesionClub(selected.getNomUser(), selected.getEmailUser(),
                    selected.getIdEntite());
            if (serviceAdhesion.adherer(adhesion)) {
                // 2. Mettre à jour le statut de la demande
                if (serviceDemande.approuverDemande(selected.getId())) {
                    emailService.sendApprovalEmail(selected.getEmailUser(), selected.getNomEntite());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "L'utilisateur a été approuvé et ajouté au club.");
                    chargerDemandes();
                    chargerClubs(); // Rafraîchir les places
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Complet", "Le club est complet, impossible d'approuver.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'approbation a échoué : " + e.getMessage());
        }
    }

    @FXML
    public void refuserUser() {
        DemandeAdhesion selected = tableDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande à refuser.");
            return;
        }

        try {
            if (serviceDemande.refuserDemande(selected.getId())) {
                emailService.sendRefusalEmail(selected.getEmailUser(), selected.getNomEntite());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La demande a été refusée.");
                chargerDemandes();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le refus a échoué.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le refus a échoué : " + e.getMessage());
        }
    }

    private void setupFeedbacksTable() {
        colFeedbackUser.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        colFeedbackNote.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNote()));
        colFeedbackCategorie
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategorie()));
        colFeedbackComment
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCommentaire()));
        colFeedbackDate.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDateFeedback().toString()));
    }

    private void chargerFeedbacks() {
        List<Feedback> feedbacks = serviceFeedback.readAllByType("CLUB");
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
    private void actualiserFeedbacks() {
        chargerFeedbacks();
    }

    @FXML
    private void supprimerFeedback() {
        Feedback selected = tableFeedbacks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un feedback à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le feedback ?");
        confirmation.setContentText("Cette action est irréversible.");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            if (serviceFeedback.supprimer(selected.getIdFeedback())) {
                chargerFeedbacks();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Feedback supprimé.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le feedback.");
            }
        }
    }
}
