package Controllers.home;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class HomeController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private SidebarController sidebarController;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setRootPane(rootPane);
        }
    }

    @FXML
    private void onClubCardClicked(MouseEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Détails du club à venir.", ButtonType.OK).showAndWait();
    }
}
