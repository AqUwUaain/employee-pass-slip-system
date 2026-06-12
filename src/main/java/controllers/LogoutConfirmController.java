package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LogoutConfirmController {

    @FXML
    private Button cancelBtn;

    @FXML
    private Button logoutBtn;

    private Stage dialogStage;
    private boolean confirmed = false;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void initialize() {
        cancelBtn.setOnAction(e -> {
            confirmed = false;
            dialogStage.close();
        });

        logoutBtn.setOnAction(e -> {
            confirmed = true;
            dialogStage.close();
        });
    }
}
