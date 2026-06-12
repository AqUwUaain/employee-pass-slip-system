package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label resetMessageLabel;

    @FXML
    private Button submitBtn;

    private Stage dialogStage;
    private String userEmail;
    private int requestId;
    private boolean resetSuccessful = false;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setRequestInfo(String email, int requestId) {
        this.userEmail = email;
        this.requestId = requestId;
    }

    public boolean isResetSuccessful() {
        return resetSuccessful;
    }

    @FXML
    private void initialize() {
        submitBtn.setOnAction(e -> handleSubmit());
    }

    private void handleSubmit() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty()) {
            resetMessageLabel.setText("Password is required.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (newPassword.length() < 4) {
            resetMessageLabel.setText("Password must be at least 4 characters.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            resetMessageLabel.setText("Passwords do not match.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {

            String hashedPassword = PasswordUtils.hashPassword(newPassword);

            try (PreparedStatement updatePassword = connection.prepareStatement(
                    "UPDATE users SET password = ? WHERE username = ?"
                 )) {
                updatePassword.setString(1, hashedPassword);
                updatePassword.setString(2, userEmail);
                int updated = updatePassword.executeUpdate();

                if (updated > 0) {
                    try (PreparedStatement markUsed = connection.prepareStatement(
                            "UPDATE password_reset_requests SET used = TRUE, status = 'COMPLETED' WHERE id = ?"
                         )) {
                        markUsed.setInt(1, requestId);
                        markUsed.executeUpdate();
                    }

                    ActivityLogController.logActivity("Password successfully changed for: " + userEmail, 0);

                    resetMessageLabel.setText("Password reset successful!");
                    resetMessageLabel.setStyle("-fx-text-fill: #34D399;");
                    resetSuccessful = true;

                    submitBtn.setDisable(true);

                    javafx.animation.Timeline timer = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                            javafx.util.Duration.seconds(1.5),
                            e -> dialogStage.close()
                        )
                    );
                    timer.play();
                } else {
                    resetMessageLabel.setText("Failed to update password.");
                    resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            resetMessageLabel.setText("Database error: " + e.getMessage());
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
        }
    }
}
