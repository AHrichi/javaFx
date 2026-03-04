package Controllers.seance;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Entite.Seance;
import Service.seance.ServiceSeance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import Entite.Coach;
import Entite.Club;
import Service.user.ServiceCoach;
import Service.club.ServiceClub;
import javafx.util.StringConverter;
import Utils.SessionManager;
import java.util.List;

public class AjouterSeanceController {

    // --- Inputs ---
    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboNiveau;
    @FXML private Spinner<Integer> spinCapacite;
    @FXML private Spinner<Integer> spinHeureDebut;
    @FXML private Spinner<Integer> spinHeureFin;
    @FXML private Button btnAction;
    @FXML private ComboBox<Club> comboClub;
    @FXML private ComboBox<Coach> comboCoach;

    // --- Error Labels ---
    @FXML private Label errTitre;
    @FXML private Label errDescription;
    @FXML private Label errDate;
    @FXML private Label errHeure;
    @FXML private Label errNiveau;
    @FXML private Label errClub;
    @FXML private Label errCoach;

    private ServiceSeance service = new ServiceSeance();
    private ServiceCoach serviceCoach = new ServiceCoach();
    private ServiceClub serviceClub = new ServiceClub(); // Service pour récupérer les clubs
    private boolean isSaved = false;
    private Seance seanceAModifier = null;

    @FXML
    public void initialize() {
        comboNiveau.getItems().addAll("Débutant", "Intermédiaire", "Avancé");

        spinCapacite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 20));
        spinHeureDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 22, 10));
        spinHeureFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(9, 23, 11));

        // --- Configurer le ComboBox Club ---
        comboClub.setConverter(new StringConverter<Club>() {
            @Override
            public String toString(Club club) {
                return (club != null) ? club.getNom() : "";
            }

            @Override
            public Club fromString(String string) {
                return null;
            }
        });

        // --- Configurer le ComboBox Coach ---
        comboCoach.setConverter(new StringConverter<Coach>() {
            @Override
            public String toString(Coach coach) {
                return (coach != null) ? coach.getNom() + " " + coach.getPrenom() : "";
            }

            @Override
            public Coach fromString(String string) {
                return null;
            }
        });

        // --- 1. Charger la liste des clubs (Isolé) ---
        try {
            List<Club> allClubs = serviceClub.readAll();
            comboClub.getItems().addAll(allClubs);
        } catch (Exception e) {
            System.out.println("Erreur SQL lors du chargement des clubs : " + e.getMessage());
            e.printStackTrace();
        }

        // --- 2. Charger la liste des coachs (Isolé) ---
        try {
            List<Coach> allCoaches = serviceCoach.readAll();
            Entite.User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null && "Coach".equals(currentUser.getTypeUser())) {
                for (Coach c : allCoaches) {
                    if (c.getIdUser() == currentUser.getIdUser()) {
                        comboCoach.getItems().add(c);
                        comboCoach.setValue(c);
                        break;
                    }
                }
                comboCoach.setDisable(true); // Verrouiller sur le coach connecté
            } else {
                comboCoach.getItems().addAll(allCoaches);
            }
        } catch (Exception e) {
            System.out.println("Erreur SQL lors du chargement des coachs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSeanceData(Seance s) {
        this.seanceAModifier = s;
        txtTitre.setText(s.getTitre());
        txtDescription.setText(s.getDescription());
        datePicker.setValue(s.getDateDebut().toLocalDate());

        spinHeureDebut.getValueFactory().setValue(s.getDateDebut().getHour());
        spinHeureFin.getValueFactory().setValue(s.getDateFin().getHour());

        comboNiveau.setValue(s.getNiveau());
        spinCapacite.getValueFactory().setValue(s.getCapaciteMax());

        if (btnAction != null) btnAction.setText("Modifier");

        // Pré-sélectionner le club
        for (Club club : comboClub.getItems()) {
            if (club.getIdClub() == s.getIdClub()) {
                comboClub.setValue(club);
                break;
            }
        }

        // Pré-sélectionner le coach
        for (Coach c : comboCoach.getItems()) {
            if (c.getIdUser() == s.getIdCoach()) {
                comboCoach.setValue(c);
                break;
            }
        }

        Entite.User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && "Coach".equals(currentUser.getTypeUser())) {
            comboCoach.setDisable(true);
        }
    }

    @FXML
    private void sauvegarder() {
        if (!validateInputs()) {
            return;
        }

        try {
            Seance s = (seanceAModifier == null) ? new Seance() : seanceAModifier;

            s.setTitre(txtTitre.getText().trim());
            s.setDescription(txtDescription.getText().trim());

            int startHour = spinHeureDebut.getValue();
            int endHour = spinHeureFin.getValue();
            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(), LocalTime.of(startHour, 0));
            LocalDateTime fin = LocalDateTime.of(datePicker.getValue(), LocalTime.of(endHour, 0));

            s.setDateDebut(debut);
            s.setDateFin(fin);
            s.setNiveau(comboNiveau.getValue());
            s.setCapaciteMax(spinCapacite.getValue());

            // Assigner l'ID du club sélectionné
            s.setIdClub(comboClub.getValue().getIdClub());

            s.setStatut("planifiée");
            s.setPhotoSeance("default.png");

            boolean success;
            Entite.User currentUser = SessionManager.getCurrentUser();

            if (seanceAModifier == null) {
                if (currentUser != null && "Coach".equals(currentUser.getTypeUser())) {
                    s.setIdCoach(currentUser.getIdUser());
                } else {
                    if (comboCoach.getValue() == null) {
                        showError(errCoach, comboCoach, "Veuillez sélectionner un coach");
                        return;
                    }
                    s.setIdCoach(comboCoach.getValue().getIdUser());
                }
                success = service.ajouter(s);
            } else {
                if (currentUser != null && "Coach".equals(currentUser.getTypeUser())) {
                    s.setIdCoach(currentUser.getIdUser());
                } else if (comboCoach.getValue() != null) {
                    s.setIdCoach(comboCoach.getValue().getIdUser());
                }
                success = service.modifier(s);
            }

            if (success) {
                isSaved = true;
                fermer();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde: " + e.getMessage());
            alert.show();
        }
    }

    // --- VALIDATION LOGIC ---
    private boolean validateInputs() {
        boolean isValid = true;

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            showError(errTitre, txtTitre, "Le titre est obligatoire");
            isValid = false;
        } else {
            clearError(errTitre, txtTitre);
        }

        if (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty()) {
            showError(errDescription, txtDescription, "La description est obligatoire");
            isValid = false;
        } else {
            clearError(errDescription, txtDescription);
        }

        if (datePicker.getValue() == null) {
            showError(errDate, datePicker, "Veuillez choisir une date");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            showError(errDate, datePicker, "La date ne peut pas être passée");
            isValid = false;
        } else {
            clearError(errDate, datePicker);
        }

        int start = spinHeureDebut.getValue();
        int end = spinHeureFin.getValue();
        if (end <= start) {
            errHeure.setText("L'heure de fin doit être après le début");
            errHeure.setVisible(true);
            errHeure.setManaged(true);
            isValid = false;
        } else {
            errHeure.setVisible(false);
            errHeure.setManaged(false);
        }

        if (comboNiveau.getValue() == null) {
            showError(errNiveau, comboNiveau, "Veuillez sélectionner un niveau");
            isValid = false;
        } else {
            clearError(errNiveau, comboNiveau);
        }

        // Validation du Club
        if (comboClub.getValue() == null) {
            showError(errClub, comboClub, "Veuillez sélectionner un club");
            isValid = false;
        } else {
            clearError(errClub, comboClub);
        }

        return isValid;
    }

    private void showError(Label errorLabel, Control inputField, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        inputField.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
    }

    private void clearError(Label errorLabel, Control inputField) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        inputField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");
    }

    @FXML private void fermer() {
        if (txtTitre.getScene() != null) ((Stage) txtTitre.getScene().getWindow()).close();
    }

    public boolean isSaved() { return isSaved; }
}