package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.ThemeManager;

public class LogoutConfirmController {

    @FXML
    private VBox logoutDialogRoot;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label dialogTitle;

    @FXML
    private Label dialogSubtitle;

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
        boolean isDark = ThemeManager.isDark();

        if (isDark) {
            logoutDialogRoot.setStyle("-fx-background-color: #2D2520; -fx-background-radius: 20px; -fx-padding: 40px; -fx-border-color: #3D3229; -fx-border-radius: 20px; -fx-border-width: 1px;");
        } else {
            logoutDialogRoot.setStyle("-fx-background-color: #FDF8EE; -fx-background-radius: 20px; -fx-padding: 40px; -fx-border-color: #C0B89E; -fx-border-radius: 20px; -fx-border-width: 1px;");
        }

        cancelBtn.setOnAction(e -> {
            confirmed = false;
            dialogStage.close();
        });

        logoutBtn.setOnAction(e -> {
            confirmed = true;
            dialogStage.close();
        });

        if (dialogTitle != null) {
            dialogTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + (isDark ? "#D4A853;" : "#1C1917;"));
        }
        if (dialogSubtitle != null) {
            dialogSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isDark ? "#A8A29E;" : "#6B6358;"));
        }
    }
}
