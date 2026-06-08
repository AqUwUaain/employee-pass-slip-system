package controllers;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.NavigationHelper;
import utils.PasswordUtils;
import utils.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.prefs.Preferences;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private StackPane passwordStackPane;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink privacyPolicyLink;

    @FXML
    private Hyperlink termsOfServiceLink;

    private TextField visiblePasswordField;
    private boolean passwordVisible = false;

    private static final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    private void initialize() {

        // Load saved credentials
        String savedEmail = prefs.get("saved_email", "");
        boolean rememberMe = prefs.getBoolean("remember_me", false);

        if (rememberMe && !savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
            rememberMeCheckbox.setSelected(true);
        }

        // Show/hide password toggle
        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter your password");
        visiblePasswordField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06); " +
            "-fx-border-color: rgba(255,255,255,0.08); " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-padding: 14px 16px; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: rgba(255,255,255,0.25); " +
            "-fx-pref-height: 46px;"
        );

        togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());

        // Forgot Password
        forgotPasswordLink.setOnAction(e -> messageLabel.setText("Contact admin to reset password."));

        // Privacy Policy
        privacyPolicyLink.setOnAction(e -> showModal("/fxml/PrivacyPolicy.fxml"));

        // Terms and Conditions
        termsOfServiceLink.setOnAction(e -> showModal("/fxml/TermsConditions.fxml"));
    }

    private void togglePasswordVisibility() {
        String currentPassword = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (passwordVisible) {
            passwordStackPane.getChildren().remove(visiblePasswordField);
            passwordStackPane.getChildren().add(0, passwordField);
            passwordField.setText(currentPassword);
            togglePasswordBtn.setText("\uD83D\uDC41");
        } else {
            passwordStackPane.getChildren().remove(passwordField);
            passwordStackPane.getChildren().add(0, visiblePasswordField);
            visiblePasswordField.setText(currentPassword);
            togglePasswordBtn.setText("\uD83D\uDC41\u200D\uD83D\uDCBB");
        }

        passwordVisible = !passwordVisible;
    }

    private void showModal(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UNDECORATED);
            modalStage.initOwner(emailField.getScene().getWindow());

            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            // Wire close button
            Object controller = loader.getController();
            if (controller instanceof PrivacyPolicyController pc) {
                pc.setCloseAction(e -> modalStage.close());
            } else if (controller instanceof TermsConditionsController tc) {
                tc.setCloseAction(e -> modalStage.close());
            }

            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText().trim() : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter email and password.");
            return;
        }

        if (!email.contains("@")) {
            messageLabel.setText("Enter a valid email address.");
            return;
        }

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query =
                    "SELECT * FROM users " +
                            "WHERE username = ?";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(1, email);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                String storedPassword =
                        resultSet.getString("password");

                boolean passwordMatch = storedPassword.equals(password);

                if (!passwordMatch && storedPassword.length() == 64) {
                    passwordMatch = PasswordUtils.verifyPassword(password, storedPassword);
                }

                if (!passwordMatch) {
                    messageLabel.setText("Invalid username or password.");
                    return;
                }

                if (!storedPassword.equals(PasswordUtils.hashPassword(password))) {
                    try {
                        Connection conn2 = DatabaseConnection.connect();
                        PreparedStatement upgrade = conn2.prepareStatement(
                                "UPDATE users SET password = ? WHERE id = ?"
                        );
                        upgrade.setString(1, PasswordUtils.hashPassword(password));
                        upgrade.setInt(2, resultSet.getInt("id"));
                        upgrade.executeUpdate();
                    } catch (Exception ignored) {}
                }

                // Remember Me
                if (rememberMeCheckbox.isSelected()) {
                    prefs.put("saved_email", email);
                    prefs.putBoolean("remember_me", true);
                } else {
                    prefs.remove("saved_email");
                    prefs.putBoolean("remember_me", false);
                }

                Session.currentUserId =
                        resultSet.getInt("id");

                String role =
                        resultSet.getString("role");

                Session.currentRole = role;

                Session.currentUsername = email;

                ActivityLogController.logActivity(
                        "User Logged In",
                        0
                );

                if (role.equalsIgnoreCase("ADMIN")) {

                    NavigationHelper.navigateTo(
                            emailField,
                            "/fxml/Dashboard.fxml"
                    );

                }
                else if (role.equalsIgnoreCase("STAFF")) {

                    NavigationHelper.navigateTo(
                            emailField,
                            "/fxml/StaffDashboard.fxml"
                    );

                }
                else {

                    messageLabel.setText(
                            "Unknown user role."
                    );

                }

            }
            else {

                messageLabel.setText(
                        "Invalid username or password."
                );

            }

        }
        catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "Database error."
            );

        }

    }
}
