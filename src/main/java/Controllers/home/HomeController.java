package Controllers.home;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class HomeController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private SidebarController sidebarController;

    @FXML
    public void initialize() {
        if (sidebarController != null && rootPane != null) {
            sidebarController.setRootPane(rootPane);
        }
    }
}
