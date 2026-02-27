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
import Service.user.ServiceCoach;
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

    // --- Error Labels ---
    @FXML private Label errTitre;
    @FXML private Label errDescription;
    @FXML private Label errDate;
    @FXML private Label errHeure;
    @FXML private Label errNiveau;
    @FXML private ComboBox<Coach> comboCoach;
    @FXML private Label errCoach;

    private ServiceSeance service = new ServiceSeance();
    private ServiceCoach serviceCoach = new ServiceCoach();
    private boolean isSaved = false;
    private Seance seanceAModifier = null;

    @FXML
    public void initialize() {
        comboNiveau.getItems().addAll("Débutant", "Intermédiaire", "Avancé");

        spinCapacite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 20));
        spinHeureDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 22, 10));
        spinHeureFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(9, 23, 11));

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

        Entite.User currentUser = SessionManager.getCurrentUser();
        try {
            List<Coach> allCoaches = serviceCoach.readAll();

            if (currentUser != null && "Coach".equals(currentUser.getTypeUser())) {
                for (Coach c : allCoaches) {
                    if (c.getIdUser() == currentUser.getIdUser()) {
                        comboCoach.getItems().add(c);
                        comboCoach.setValue(c);
                        break;
                    }
                }
                comboCoach.setDisable(true);
            } else {
                comboCoach.getItems().addAll(allCoaches);
            }
        } catch (Exception e) {
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
            s.setIdClub(1);
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