package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ForgotPasswordController {

    @FXML
    private TextField resetEmailField;

    @FXML
    private Label resetMessageLabel;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button submitBtn;

    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void initialize() {
        cancelBtn.setOnAction(e -> dialogStage.close());
        submitBtn.setOnAction(e -> handleSubmit());
    }

    private void handleSubmit() {
        String email = resetEmailField.getText().trim();

        if (email.isEmpty()) {
            resetMessageLabel.setText("Email is required.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (!email.contains("@")) {
            resetMessageLabel.setText("Enter a valid email address.");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        if (!email.toLowerCase().endsWith("@pup.edu.ph")) {
            resetMessageLabel.setText("Email must end with @pup.edu.ph");
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            return;
        }

        try {
            Connection connection = DatabaseConnection.connect();

            String checkQuery = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, email);
            java.sql.ResultSet resultSet = checkStatement.executeQuery();

            if (!resultSet.next()) {
                resetMessageLabel.setText("No account found with this email.");
                resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
                checkStatement.close();
                connection.close();
                return;
            }

            String insertQuery = "INSERT INTO password_reset_requests (email, requested_at, status) VALUES (?, CURRENT_TIMESTAMP, 'PENDING')";
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, email);
            int inserted = statement.executeUpdate();

            if (inserted > 0) {
                resetMessageLabel.setText("Request submitted successfully. Wait for admin approval.");
                resetMessageLabel.setStyle("-fx-text-fill: #34D399;");
                resetEmailField.clear();

                ActivityLogController.logActivity("Password reset request submitted for: " + email, 0);
            } else {
                resetMessageLabel.setText("Failed to submit request.");
                resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
            }

            statement.close();
            checkStatement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            resetMessageLabel.setText("Database error: " + e.getMessage());
            resetMessageLabel.setStyle("-fx-text-fill: #FCA5A5;");
        }
    }
}
