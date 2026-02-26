package Controllers.seance;

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

    private final ServiceCoach serviceCoach = new ServiceCoach();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    // --- STATE MANAGEMENT ---
    private enum ViewType {
        DAY, WEEK, MONTH
    }

    private ViewType currentView = ViewType.WEEK;
    private LocalDate currentDate; // Points to the reference date (e.g., start of week, or specific day)

    // --- CONFIGURATION ---
    private final ServiceSeance serviceSeance = new ServiceSeance();
    private final int START_HOUR = 8; // 8 AM
    private final int END_HOUR = 21; // 9 PM

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

            // Display Coach Names instead of Object IDs
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

        // 3. Add Listeners to auto-refresh when filters change
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> rafraichirPlanning());
        filterCoach.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirPlanning());
        filterNiveau.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirPlanning());

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
        // Reset styles
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
        datePicker.show(); // Programmatically open the date picker
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

        // 1. Filter by Title (Search)
        if (txtSearch.getText() != null && !txtSearch.getText().isEmpty()) {
            stream = stream.filter(s -> s.getTitre().toLowerCase().contains(txtSearch.getText().toLowerCase()));
        }

        // 2. Filter by Coach
        if (filterCoach.getValue() != null) {
            int selectedCoachId = filterCoach.getValue().getIdUser();
            stream = stream.filter(s -> s.getIdCoach() == selectedCoachId);
        }

        // 3. Filter by Level
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
            buildTimeGrid(); // Shared logic for Day & Week
            loadTimeData();
        }
    }

    private void updateHeaderLabel() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

        if (currentView == ViewType.DAY) {
            lblDateRange.setText(currentDate.format(fmt));
        } else if (currentView == ViewType.WEEK) {
            LocalDate startOfWeek = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            lblDateRange
                    .setText(startOfWeek.format(DateTimeFormatter.ofPattern("dd MMM")) + " - " + endOfWeek.format(fmt));
        } else {
            lblDateRange.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)));
        }
    }

    // ---------------------------------------------------------
    // STRATEGY 1: TIME GRID (DAY & WEEK VIEWS)
    // ---------------------------------------------------------
    private void buildTimeGrid() {
        int colCount = (currentView == ViewType.DAY) ? 1 : 7;

        // 1. Column Constraints
        // First Col: Time Labels (Fixed Width)
        ColumnConstraints timeCol = new ColumnConstraints(60);
        timeCol.setHalignment(HPos.RIGHT);
        gridPlanning.getColumnConstraints().add(timeCol);

        // Content Cols
        for (int i = 0; i < colCount; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS); // Grow to fill space
            col.setMinWidth(100);
            gridPlanning.getColumnConstraints().add(col);
        }

        // 2. Row Constraints
        // Row 0: Header
        RowConstraints headerRow = new RowConstraints(40);
        gridPlanning.getRowConstraints().add(headerRow);

        // Time Slots
        for (int h = START_HOUR; h < END_HOUR; h++) {
            RowConstraints row = new RowConstraints(60); // 60px height per hour
            gridPlanning.getRowConstraints().add(row);
        }

        // 3. Add Headers
        gridPlanning.add(new Label("Time"), 0, 0); // Corner

        LocalDate startOfWeek = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);

        for (int i = 0; i < colCount; i++) {
            LocalDate date = (currentView == ViewType.DAY) ? currentDate : startOfWeek.plusDays(i);
            Label lbl = new Label(date.format(DateTimeFormatter.ofPattern("EEE dd")));
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            GridPane.setHalignment(lbl, HPos.CENTER);
            gridPlanning.add(lbl, i + 1, 0);
        }

        // 4. Add Time Labels & Background grid
        for (int h = START_HOUR; h < END_HOUR; h++) {
            int rowIndex = h - START_HOUR + 1;
            Label timeLbl = new Label(h + ":00");
            timeLbl.setPadding(new Insets(0, 10, 0, 0));
            timeLbl.setStyle("-fx-text-fill: #999;");
            gridPlanning.add(timeLbl, 0, rowIndex);

            // Add border lines for cells
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

            // Filter relevant sessions
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

        // Boundary Check
        if (startH < START_HOUR || startH >= END_HOUR)
            return;

        // Calculate Coords
        int duration = endH - startH;
        if (duration < 1)
            duration = 1;

        int rowIndex = startH - START_HOUR + 1;

        int colIndex;
        if (currentView == ViewType.DAY) {
            colIndex = 1; // Always col 1 in day view
        } else {
            // Monday = 1, Sunday = 7
            colIndex = s.getDateDebut().getDayOfWeek().getValue();
        }

        VBox card = createSessionCard(s);
        GridPane.setRowSpan(card, duration);

        // Add margins to prevent overlapping borders
        GridPane.setMargin(card, new Insets(2, 2, 2, 2));
        gridPlanning.add(card, colIndex, rowIndex);
    }

    // ---------------------------------------------------------
    // STRATEGY 2: MONTH GRID
    // ---------------------------------------------------------
    private void buildMonthGrid() {
        // 7 Columns (Mon-Sun)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7.0); // Equal width
            gridPlanning.getColumnConstraints().add(col);
        }

        // Header Row
        gridPlanning.getRowConstraints().add(new RowConstraints(30));

        // Day Headers
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(days[i]);
            lbl.setStyle("-fx-font-weight:bold; -fx-text-fill: #555;");
            GridPane.setHalignment(lbl, HPos.CENTER);
            gridPlanning.add(lbl, i, 0);
        }

        // 5 Rows for weeks (approx)
        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setMinHeight(100); // Taller cells for month view
            gridPlanning.getRowConstraints().add(row);
        }
    }

    private void loadMonthData() {
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeekOffset = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 for Monday

        try {
            List<Seance> allSeances = serviceSeance.readAll();
            List<Seance> filteredAll = filterSessions(allSeances);
            int daysInMonth = yearMonth.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);

                // Determine Grid Position
                int totalDayIndex = day + dayOfWeekOffset - 1;
                int col = totalDayIndex % 7;
                int row = (totalDayIndex / 7) + 1; // +1 to skip header

                // Create Cell Container
                VBox cell = new VBox(2);
                cell.setPadding(new Insets(5));
                cell.setStyle("-fx-border-color: #eee; -fx-border-width: 0.5; -fx-background-color: white;");

                // Day Number Label
                Label dayNum = new Label(String.valueOf(day));
                dayNum.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                cell.getChildren().add(dayNum);

                // Find sessions for this day
                List<Seance> dailySessions = filteredAll.stream()
                        .filter(s -> s.getDateDebut().toLocalDate().equals(date))
                        .collect(Collectors.toList());

                // Add mini-dots or small labels for sessions
                for (Seance s : dailySessions) {
                    Label sLbl = new Label(
                            s.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm")) + " " + s.getTitre());
                    sLbl.setStyle(
                            "-fx-background-color: #e0f7fa; -fx-text-fill: #006064; -fx-font-size: 9px; -fx-padding: 2; -fx-background-radius: 3;");
                    sLbl.setMaxWidth(Double.MAX_VALUE);

                    // Click to modify
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

    // --- SHARED COMPONENT: SESSION CARD ---
    private VBox createSessionCard(Seance s) {
        // 1. Get Capacity Info
        int max = s.getCapaciteMax();
        // Use the service to get the real count
        int current = serviceParticipation.countParticipants(s.getIdSeance());
        int available = max - current;
        boolean isFull = available <= 0;

        // 2. Title
        Label titre = new Label(s.getTitre());
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        titre.setWrapText(true);

        // 3. Info (Time & Level)
        Label info = new Label(
                s.getNiveau() + "\n" + s.getDateDebut().toLocalTime() + " - " + s.getDateFin().toLocalTime());
        info.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");

        // 4. Capacity Label
        Label lblPlaces = new Label();
        if (isFull) {
            lblPlaces.setText("COMPLET");
            lblPlaces.setStyle(
                    "-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-color: rgba(0,0,0,0.3); -fx-padding: 2 5; -fx-background-radius: 3;");
        } else {
            lblPlaces.setText(available + " places left");
            lblPlaces.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold; -fx-font-size: 10px;");
        }

        // 5. "Ajout Participant" Button
        Button btnAdd = new Button(isFull ? "Complet" : "Ajout Participant"); // CHANGED TEXT
        btnAdd.setStyle(isFull
                ? "-fx-background-color: #555; -fx-text-fill: #aaa; -fx-font-size: 10px; -fx-background-radius: 15;"
                : "-fx-background-color: #ffb800; -fx-text-fill: #00302e; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-radius: 15;");

        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setDisable(isFull);

        // ACTION: Open the Selection Popup
        btnAdd.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/seance/AddParticipant.fxml"));
                Parent root = loader.load();

                // Pass the current session to the controller
                AddParticipantController controller = loader.getController();
                controller.setSeance(s);

                Stage stage = new Stage();
                stage.setTitle("Ajouter un participant");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Block other windows
                stage.showAndWait();

                // Refresh grid after popup closes to show updated count
                rafraichirPlanning();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // 6. Layout Container
        VBox box = new VBox(4, titre, info, lblPlaces, btnAdd);
        box.setPadding(new Insets(8));

        // Dynamic Background Color
        String bgColor = "#00302e";
        if (isFull)
            bgColor = "#2c3e50"; // Dark Grey for full sessions

        box.setStyle("-fx-background-color: " + bgColor
                + "; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");

        // 7. Context Menu (Right Click)
        ContextMenu menu = new ContextMenu();

        // --- NEW: VIEW LIST BUTTON ---
        MenuItem listItem = new MenuItem("Voir Participants");
        listItem.setStyle("-fx-font-weight: bold;");
        listItem.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/seance/ListeParticipants.fxml"));
                Parent root = loader.load();

                // Pass data to controller
                ListeParticipantsController controller = loader.getController();
                controller.setSeance(s);

                Stage stage = new Stage();
                stage.setTitle("Liste des Participants");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        MenuItem modifierItem = new MenuItem("Modifier");
        modifierItem.setOnAction(e -> modifierSeance(s));

        MenuItem deleteItem = new MenuItem("Supprimer");
        deleteItem.setOnAction(e -> supprimerSeance(s));

        // Add all items to menu
        menu.getItems().addAll(listItem, new SeparatorMenuItem(), modifierItem, deleteItem);

        box.setOnContextMenuRequested(e -> menu.show(box, e.getScreenX(), e.getScreenY()));

        return box;
    }

    @FXML
    private void clearFilters() {
        txtSearch.clear();
        filterCoach.setValue(null);
        filterNiveau.setValue(null);
        rafraichirPlanning();
    }

    // --- CRUD HELPER METHODS ---
    @FXML
    private void ajouterSeance() {
        openModal(null);
    }

    private void modifierSeance(Seance s) {
        openModal(s);
    }

    private void supprimerSeance(Seance s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete Session");
        alert.setContentText("Are you sure you want to delete: " + s.getTitre() + "?");

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

            // Assuming your AjouterSeanceController has a setSeanceData method
            AjouterSeanceController controller = loader.getController();
            if (s != null)
                controller.setSeanceData(s);

            Stage stage = new Stage();
            stage.setTitle(s == null ? "Add Session" : "Edit Session");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh after modal closes
            rafraichirPlanning();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}