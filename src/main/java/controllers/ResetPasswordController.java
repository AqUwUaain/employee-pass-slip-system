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
import java.sql.ResultSet;

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

        if (newPassword.length() < 5) {
            resetMessageLabel.setText("Password must be at least 5 characters.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            resetMessageLabel.setText("Password must contain at least 1 uppercase letter.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (!newPassword.matches(".*[0-9].*")) {
            resetMessageLabel.setText("Password must contain at least 1 number.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (!newPassword.matches(".*[!@#%*].*")) {
            resetMessageLabel.setText("Password must contain at least 1 special character (!@#%*).");
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

            try (PreparedStatement checkOld = connection.prepareStatement(
                    "SELECT password FROM users WHERE username = ?"
                 )) {
                checkOld.setString(1, userEmail);
                ResultSet rs = checkOld.executeQuery();
                if (rs.next()) {
                    String oldHashed = rs.getString("password");
                    if (PasswordUtils.verifyPassword(newPassword, oldHashed)) {
                        resetMessageLabel.setText("New password cannot be the same as your old password.");
                        resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
                        return;
                    }
                }
            }

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
