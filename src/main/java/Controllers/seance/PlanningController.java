package Controllers.seance;

import Entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Entite.Seance;
import Service.seance.ServiceSeance;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import Service.seance.ServiceParticipation;
import Entite.Coach;
import Service.user.ServiceCoach;
import javafx.util.StringConverter;
import java.util.stream.Stream;

public class PlanningController {

    // --- FXML UI ELEMENTS ---
    @FXML
    private GridPane gridPlanning;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label lblDateRange;
    @FXML
    private Button btnDay, btnWeek, btnMonth;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Coach> filterCoach;
    @FXML
    private ComboBox<String> filterNiveau;
    @FXML
    private Button btnAddSession;

    private final ServiceCoach serviceCoach = new ServiceCoach();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    // --- STATE MANAGEMENT ---
    private enum ViewType {
        DAY, WEEK, MONTH
    }

    private ViewType currentView = ViewType.WEEK;
    private LocalDate currentDate;

    // --- CONFIGURATION ---
    private final ServiceSeance serviceSeance = new ServiceSeance();
    private final int START_HOUR = 8;
    private final int END_HOUR = 21;

    // --- INITIALIZATION ---
    @FXML
    public void initialize() {
        currentDate = LocalDate.now();

        // 1. Initialize Level Filter
        filterNiveau.getItems().addAll("Débutant", "Intermédiaire", "Avancé");

        // 2. Initialize Coach Filter
        try {
            List<Coach> coaches = serviceCoach.readAll();
            filterCoach.getItems().addAll(coaches);

            filterCoach.setConverter(new StringConverter<Coach>() {
                @Override
                public String toString(Coach c) {
                    return c == null ? null : c.getNom();
                }

                @Override
                public Coach fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> rafraichirPlanning());
        filterCoach.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirPlanning());
        filterNiveau.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirPlanning());

        User currentUser = Utils.SessionManager.getCurrentUser();
        if (currentUser != null) {
            if ("Membre".equals(currentUser.getTypeUser())) {
                if (btnAddSession != null) {
                    btnAddSession.setVisible(false);
                    btnAddSession.setManaged(false);
                }
            } else if ("Coach".equals(currentUser.getTypeUser())) {
                if (filterCoach != null) {
                    filterCoach.setVisible(false);
                    filterCoach.setManaged(false);
                }
            }
        }

        setViewWeek();
    }

    // --- VIEW SWITCHING LOGIC ---
    @FXML
    public void setViewDay() {
        switchView(ViewType.DAY);
    }

    @FXML
    public void setViewWeek() {
        switchView(ViewType.WEEK);
    }

    @FXML
    public void setViewMonth() {
        switchView(ViewType.MONTH);
    }

    private void switchView(ViewType type) {
        this.currentView = type;
        updateViewButtons();
        rafraichirPlanning();
    }

    private void updateViewButtons() {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #666; -fx-background-radius: 20;";
        String activeStyle = "-fx-background-color: #00302e; -fx-text-fill: white; -fx-background-radius: 20;";

        btnDay.setStyle(currentView == ViewType.DAY ? activeStyle : inactiveStyle);
        btnWeek.setStyle(currentView == ViewType.WEEK ? activeStyle : inactiveStyle);
        btnMonth.setStyle(currentView == ViewType.MONTH ? activeStyle : inactiveStyle);
    }

    // --- NAVIGATION LOGIC ---
    @FXML
    private void previousPeriod() {
        if (currentView == ViewType.DAY)
            currentDate = currentDate.minusDays(1);
        else if (currentView == ViewType.WEEK)
            currentDate = currentDate.minusWeeks(1);
        else if (currentView == ViewType.MONTH)
            currentDate = currentDate.minusMonths(1);
        rafraichirPlanning();
    }

    @FXML
    private void nextPeriod() {
        if (currentView == ViewType.DAY)
            currentDate = currentDate.plusDays(1);
        else if (currentView == ViewType.WEEK)
            currentDate = currentDate.plusWeeks(1);
        else if (currentView == ViewType.MONTH)
            currentDate = currentDate.plusMonths(1);
        rafraichirPlanning();
    }

    @FXML
    private void openDatePicker() {
        datePicker.show();
    }

    @FXML
    private void updateDate() {
        if (datePicker.getValue() != null) {
            currentDate = datePicker.getValue();
            rafraichirPlanning();
        }
    }

    private List<Seance> filterSessions(List<Seance> allSeances) {
        Stream<Seance> stream = allSeances.stream();
        User currentUser = Utils.SessionManager.getCurrentUser();

        if (currentUser != null && "Coach".equals(currentUser.getTypeUser())) {
            stream = stream.filter(s -> s.getIdCoach() == currentUser.getIdUser());
        }

        if (txtSearch.getText() != null && !txtSearch.getText().isEmpty()) {
            stream = stream.filter(s -> s.getTitre().toLowerCase().contains(txtSearch.getText().toLowerCase()));
        }

        if (filterCoach.getValue() != null && (currentUser == null || !"Coach".equals(currentUser.getTypeUser()))) {
            int selectedCoachId = filterCoach.getValue().getIdUser();
            stream = stream.filter(s -> s.getIdCoach() == selectedCoachId);
        }

        if (filterNiveau.getValue() != null) {
            stream = stream.filter(s -> s.getNiveau().equals(filterNiveau.getValue()));
        }

        return stream.collect(Collectors.toList());
    }

    // --- CORE RENDERING LOGIC ---
    private void rafraichirPlanning() {
        gridPlanning.getChildren().clear();
        gridPlanning.getColumnConstraints().clear();
        gridPlanning.getRowConstraints().clear();

        updateHeaderLabel();

        if (currentView == ViewType.MONTH) {
            buildMonthGrid();
            loadMonthData();
        } else {
            buildTimeGrid();
            loadTimeData();
        }
    }

    private void updateHeaderLabel() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRANCE);

        if (currentView == ViewType.DAY) {
            lblDateRange.setText(currentDate.format(fmt));
        } else if (currentView == ViewType.WEEK) {
            LocalDate startOfWeek = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            lblDateRange.setText(startOfWeek.format(DateTimeFormatter.ofPattern("dd MMM", Locale.FRANCE)) + " - " + endOfWeek.format(fmt));
        } else {
            lblDateRange.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)));
        }
    }

    private void buildTimeGrid() {
        int colCount = (currentView == ViewType.DAY) ? 1 : 7;

        ColumnConstraints timeCol = new ColumnConstraints(60);
        timeCol.setHalignment(HPos.RIGHT);
        gridPlanning.getColumnConstraints().add(timeCol);

        for (int i = 0; i < colCount; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(100);
            gridPlanning.getColumnConstraints().add(col);
        }

        RowConstraints headerRow = new RowConstraints(40);
        gridPlanning.getRowConstraints().add(headerRow);

        for (int h = START_HOUR; h < END_HOUR; h++) {
            RowConstraints row = new RowConstraints(120);
            gridPlanning.getRowConstraints().add(row);
        }

        gridPlanning.add(new Label("Heure"), 0, 0);

        LocalDate startOfWeek = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);

        for (int i = 0; i < colCount; i++) {
            LocalDate date = (currentView == ViewType.DAY) ? currentDate : startOfWeek.plusDays(i);
            Label lbl = new Label(date.format(DateTimeFormatter.ofPattern("EEE dd", Locale.FRANCE)));
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            GridPane.setHalignment(lbl, HPos.CENTER);
            gridPlanning.add(lbl, i + 1, 0);
        }

        for (int h = START_HOUR; h < END_HOUR; h++) {
            int rowIndex = h - START_HOUR + 1;
            Label timeLbl = new Label(h + ":00");
            timeLbl.setPadding(new Insets(0, 10, 0, 0));
            timeLbl.setStyle("-fx-text-fill: #999;");
            gridPlanning.add(timeLbl, 0, rowIndex);

            for (int c = 1; c <= colCount; c++) {
                Pane cell = new Pane();
                cell.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0.5;");
                gridPlanning.add(cell, c, rowIndex);
            }
        }
    }

    private void loadTimeData() {
        try {
            LocalDate start = (currentView == ViewType.DAY) ? currentDate
                    : currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
            LocalDate end = (currentView == ViewType.DAY) ? currentDate : start.plusDays(6);

            List<Seance> allSeances = serviceSeance.readAll();

            List<Seance> filtered = filterSessions(allSeances).stream()
                    .filter(s -> !s.getDateDebut().toLocalDate().isBefore(start)
                            && !s.getDateDebut().toLocalDate().isAfter(end))
                    .collect(Collectors.toList());

            for (Seance s : filtered) {
                placeSessionOnGrid(s, start);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void placeSessionOnGrid(Seance s, LocalDate viewStartDate) {
        int startH = s.getDateDebut().getHour();
        int endH = s.getDateFin().getHour();

        if (startH < START_HOUR || startH >= END_HOUR) return;

        int duration = endH - startH;
        if (duration < 1) duration = 1;

        int rowIndex = startH - START_HOUR + 1;

        int colIndex;
        if (currentView == ViewType.DAY) {
            colIndex = 1;
        } else {
            colIndex = s.getDateDebut().getDayOfWeek().getValue();
        }

        VBox card = createSessionCard(s);
        GridPane.setRowSpan(card, duration);
        GridPane.setMargin(card, new Insets(2, 2, 2, 2));
        gridPlanning.add(card, colIndex, rowIndex);
    }

    private void buildMonthGrid() {
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7.0);
            gridPlanning.getColumnConstraints().add(col);
        }

        gridPlanning.getRowConstraints().add(new RowConstraints(30));

        String[] days = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(days[i]);
            lbl.setStyle("-fx-font-weight:bold; -fx-text-fill: #555;");
            GridPane.setHalignment(lbl, HPos.CENTER);
            gridPlanning.add(lbl, i, 0);
        }

        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setMinHeight(100);
            gridPlanning.getRowConstraints().add(row);
        }
    }

    private void loadMonthData() {
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeekOffset = firstOfMonth.getDayOfWeek().getValue() - 1;

        try {
            List<Seance> allSeances = serviceSeance.readAll();
            List<Seance> filteredAll = filterSessions(allSeances);
            int daysInMonth = yearMonth.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);

                int totalDayIndex = day + dayOfWeekOffset - 1;
                int col = totalDayIndex % 7;
                int row = (totalDayIndex / 7) + 1;

                VBox cell = new VBox(2);
                cell.setPadding(new Insets(5));
                cell.setStyle("-fx-border-color: #eee; -fx-border-width: 0.5; -fx-background-color: white;");

                Label dayNum = new Label(String.valueOf(day));
                dayNum.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                cell.getChildren().add(dayNum);

                List<Seance> dailySessions = filteredAll.stream()
                        .filter(s -> s.getDateDebut().toLocalDate().equals(date))
                        .collect(Collectors.toList());

                for (Seance s : dailySessions) {
                    Label sLbl = new Label(
                            s.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm")) + " " + s.getTitre());
                    sLbl.setStyle(
                            "-fx-background-color: #e0f7fa; -fx-text-fill: #006064; -fx-font-size: 9px; -fx-padding: 2; -fx-background-radius: 3;");
                    sLbl.setMaxWidth(Double.MAX_VALUE);

                    sLbl.setOnMouseClicked(e -> modifierSeance(s));
                    sLbl.setCursor(javafx.scene.Cursor.HAND);

                    cell.getChildren().add(sLbl);
                }

                gridPlanning.add(cell, col, row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createSessionCard(Seance s) {
        int max = s.getCapaciteMax();
        int current = serviceParticipation.countParticipants(s.getIdSeance());
        int available = max - current;
        boolean isFull = available <= 0;

        Label titre = new Label(s.getTitre());
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        titre.setWrapText(true);

        Label info = new Label(
                s.getNiveau() + "\n" + s.getDateDebut().toLocalTime() + " - " + s.getDateFin().toLocalTime());
        info.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");

        Label lblPlaces = new Label();
        if (isFull) {
            lblPlaces.setText("COMPLET");
            lblPlaces.setStyle(
                    "-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-color: rgba(0,0,0,0.3); -fx-padding: 2 5; -fx-background-radius: 3;");
        } else {
            lblPlaces.setText(available + " place(s) restante(s)");
            lblPlaces.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold; -fx-font-size: 10px;");
        }

        User currentUser = Utils.SessionManager.getCurrentUser();
        boolean isMember = currentUser != null && "Membre".equals(currentUser.getTypeUser());
        boolean isCoach = currentUser != null && "Coach".equals(currentUser.getTypeUser());
        boolean isOwnerCoach = isCoach && (currentUser.getIdUser() == s.getIdCoach());

        boolean checkRegistered = false;
        if (isMember) {
            List<Entite.Membre> participants = serviceParticipation.getParticipants(s.getIdSeance());
            for (Entite.Membre m : participants) {
                if (m.getIdUser() == currentUser.getIdUser()) {
                    checkRegistered = true;
                    break;
                }
            }
        }

        final boolean isAlreadyRegistered = checkRegistered;

        String btnText;
        if (isAlreadyRegistered) {
            btnText = "Se désinscrire";
        } else if (isFull) {
            btnText = "Complet";
        } else if (isMember) {
            btnText = "Rejoindre";
        } else if (isCoach && !isOwnerCoach) {
            btnText = "Restreint";
        } else {
            btnText = "Ajout Participant";
        }

        Button btnAdd = new Button(btnText);

        boolean disableButton = false;
        if (isMember && isFull && !isAlreadyRegistered) disableButton = true;
        if (isCoach && !isOwnerCoach) disableButton = true;
        if (!isMember && isFull) disableButton = true;

        btnAdd.setDisable(disableButton);

        if (disableButton) {
            btnAdd.setStyle("-fx-background-color: #555; -fx-text-fill: #aaa; -fx-font-size: 10px; -fx-background-radius: 15;");
        } else if (isAlreadyRegistered) {
            btnAdd.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-radius: 15;");
        } else {
            btnAdd.setStyle("-fx-background-color: #ffb800; -fx-text-fill: #00302e; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-radius: 15;");
        }
        btnAdd.setMaxWidth(Double.MAX_VALUE);

        btnAdd.setOnAction(e -> {
            if (isMember) {
                if (isAlreadyRegistered) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment vous désinscrire de cette séance ?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.YES) {
                            serviceParticipation.annulerParticipation(s.getIdSeance(), currentUser.getIdUser());
                            rafraichirPlanning();
                        }
                    });
                } else {
                    String resultMessage = serviceParticipation.participer(s.getIdSeance(), currentUser.getIdUser());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Inscription");
                    alert.setHeaderText(null);
                    alert.setContentText(resultMessage);
                    alert.showAndWait();
                    if (resultMessage.contains("réussie") || resultMessage.contains("success")) {
                        new Thread(() -> {
                            String sujet = "Confirmation d'inscription : " + s.getTitre();
                            String contenu = "Bonjour " + currentUser.getPrenom() + ",\n\n"
                                    + "Votre inscription à la séance '" + s.getTitre() + "' est confirmée !\n"
                                    + "Date : " + s.getDateDebut().toLocalDate() + "\n"
                                    + "Heure : " + s.getDateDebut().toLocalTime() + "\n\n"
                                    + "À très vite,\nL'équipe SportLink";

                            Utils.EmailService.envoyerEmail(currentUser.getEmail(), sujet, contenu);
                        }).start();
                    }

                    rafraichirPlanning();
                }
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/seance/AddParticipant.fxml"));
                    Parent root = loader.load();

                    AddParticipantController controller = loader.getController();
                    controller.setSeance(s);

                    Stage stage = new Stage();
                    stage.setTitle("Ajouter un participant");
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();

                    rafraichirPlanning();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox box = new VBox(4, titre, info, lblPlaces, btnAdd);
        box.setPadding(new Insets(8));

        String bgColor = "#00302e";
        if (isFull) bgColor = "#2c3e50";

        box.setStyle("-fx-background-color: " + bgColor
                + "; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");

        ContextMenu menu = new ContextMenu();

        if (!isMember) {
            MenuItem listItem = new MenuItem("Voir les participants");
            listItem.setStyle("-fx-font-weight: bold;");
            listItem.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/seance/ListeParticipants.fxml"));
                    Parent root = loader.load();
                    ListeParticipantsController controller = loader.getController();
                    controller.setSeance(s);
                    controller.setOnParticipantsChanged(this::rafraichirPlanning);

                    Stage stage = new Stage();
                    stage.setTitle("Liste des Participants");
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            menu.getItems().add(listItem);

            if (!isCoach || isOwnerCoach) {
                MenuItem modifierItem = new MenuItem("Modifier la séance");
                modifierItem.setOnAction(e -> modifierSeance(s));

                MenuItem deleteItem = new MenuItem("Supprimer la séance");
                deleteItem.setOnAction(e -> supprimerSeance(s));

                menu.getItems().addAll(new SeparatorMenuItem(), modifierItem, deleteItem);
            }

            box.setOnContextMenuRequested(e -> menu.show(box, e.getScreenX(), e.getScreenY()));
        }

        return box;
    }

    @FXML
    private void clearFilters() {
        txtSearch.clear();
        filterCoach.setValue(null);
        filterNiveau.setValue(null);
        rafraichirPlanning();
    }

    @FXML
    private void ajouterSeance() {
        User currentUser = Utils.SessionManager.getCurrentUser();
        if (currentUser != null && "Membre".equals(currentUser.getTypeUser())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accès Refusé");
            alert.setHeaderText(null);
            alert.setContentText("Les membres ne peuvent pas ajouter de séances.");
            alert.showAndWait();
            return;
        }
        openModal(null);
    }

    private void modifierSeance(Seance s) {
        openModal(s);
    }

    private void supprimerSeance(Seance s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la séance");
        alert.setContentText("Voulez-vous vraiment supprimer la séance : " + s.getTitre() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceSeance.supprimer(s);
                    rafraichirPlanning();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openModal(Seance s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seance/AjouterSeance.fxml"));
            Parent root = loader.load();

            AjouterSeanceController controller = loader.getController();
            if (s != null)
                controller.setSeanceData(s);

            Stage stage = new Stage();
            stage.setTitle(s == null ? "Nouvelle Séance" : "Modifier la Séance");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            rafraichirPlanning();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}