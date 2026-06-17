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
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
import java.time.Instant;
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

    @FXML
    private StackPane leftHeroPanel;

    @FXML
    private ImageView campusImageView;

    private TextField visiblePasswordField;
    private boolean passwordVisible = false;

    private static final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    private void initialize() {
        // Scale campus image to fill left panel while preserving aspect ratio
        campusImageView.fitWidthProperty().bind(leftHeroPanel.widthProperty());
        campusImageView.fitHeightProperty().bind(leftHeroPanel.heightProperty());

        // Load saved credentials
        String savedEmail = prefs.get("saved_email", "");
        String savedPassword = prefs.get("saved_password", "");
        boolean rememberMe = prefs.getBoolean("remember_me", false);

        if (rememberMe && !savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
            passwordField.setText(savedPassword);
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

        forgotPasswordLink.setOnAction(e -> showForgotPasswordDialog());
        privacyPolicyLink.setOnAction(e -> showModal("/fxml/PrivacyPolicy.fxml"));
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
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.initOwner(emailField.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            modalStage.setScene(scene);
            modalStage.setResizable(false);

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

    private void showForgotPasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForgotPassword.fxml"));
            Parent root = loader.load();

            ForgotPasswordController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(emailField.getScene().getWindow());
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasPendingPasswordReset(String email) {
        try (Connection connection = DatabaseConnection.connect()) {
            String query = "SELECT id FROM password_reset_requests WHERE email = ? AND status = 'APPROVED' AND (used = FALSE OR used IS NULL) LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            boolean hasPending = resultSet.next();
            resultSet.close();
            statement.close();
            return hasPending;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getPendingResetRequestId(String email) {
        try (Connection connection = DatabaseConnection.connect()) {
            String query = "SELECT id FROM password_reset_requests WHERE email = ? AND status = 'APPROVED' AND (used = FALSE OR used IS NULL) LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            int id = -1;
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            resultSet.close();
            statement.close();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void showResetPasswordDialog(String email) {
        try {
            int requestId = getPendingResetRequestId(email);
            if (requestId == -1) {
                navigateToDashboard();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ResetPassword.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(emailField.getScene().getWindow());
            controller.setDialogStage(dialogStage);
            controller.setRequestInfo(email, requestId);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (controller.isResetSuccessful()) {
                navigateToDashboard();
            } else {
                messageLabel.setText("Password reset was cancelled or failed.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        String role = Session.currentRole;
        if (role.equalsIgnoreCase("ADMIN")) {
            NavigationHelper.navigateTo(emailField, "/fxml/Dashboard.fxml");
        } else if (role.equalsIgnoreCase("STAFF")) {
            NavigationHelper.navigateTo(emailField, "/fxml/StaffDashboard.fxml");
        }
    }

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000;

    private int getFailAttempts(String email) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT fail_attempts FROM login_lockouts WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("fail_attempts") : 0;
        } catch (Exception e) { return 0; }
    }

    private long getLockoutTime(String email) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT lockout_time FROM login_lockouts WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getLong("lockout_time") : 0;
        } catch (Exception e) { return 0; }
    }

    private void updateFailAttempts(String email, int attempts) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO login_lockouts (email, fail_attempts) VALUES (?, ?) " +
                     "ON CONFLICT (email) DO UPDATE SET fail_attempts = ?")) {
            ps.setString(1, email);
            ps.setInt(2, attempts);
            ps.setInt(3, attempts);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void updateLockoutTime(String email, long time) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE login_lockouts SET lockout_time = ? WHERE email = ?")) {
            ps.setLong(1, time);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void resetLockout(String email) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE login_lockouts SET fail_attempts = 0, lockout_time = 0 WHERE email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (Exception ignored) {}
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

        if (!email.toLowerCase().endsWith("@pup.edu.ph")) {
            messageLabel.setText("Email must end with @pup.edu.ph");
            return;
        }

        int attempts = getFailAttempts(email);
        long lockoutTime = getLockoutTime(email);

        if (attempts >= MAX_ATTEMPTS) {
            long elapsed = System.currentTimeMillis() - lockoutTime;
            if (elapsed < LOCKOUT_DURATION_MS) {
                long remaining = (LOCKOUT_DURATION_MS - elapsed) / 60000;
                long seconds = ((LOCKOUT_DURATION_MS - elapsed) % 60000) / 1000;
                messageLabel.setText("Too many failed attempts. Try again in " + remaining + "m " + seconds + "s.");
                return;
            } else {
                resetLockout(email);
                attempts = 0;
            }
        }

        try (Connection connection = DatabaseConnection.connect()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                Session.currentUserId = resultSet.getInt("id");
                String role = resultSet.getString("role");
                Session.currentRole = role;
                Session.currentUsername = email;

                if (hasPendingPasswordReset(email)) {
                    resetLockout(email);
                    ActivityLogController.logActivity("User Logged In (Password Reset Pending)", 0);
                    showResetPasswordDialog(email);
                    return;
                }

                boolean passwordMatch = PasswordUtils.verifyPassword(password, storedPassword);

                if (!passwordMatch) {
                    attempts++;
                    updateFailAttempts(email, attempts);
                    if (attempts >= MAX_ATTEMPTS) {
                        updateLockoutTime(email, System.currentTimeMillis());
                        messageLabel.setText("Too many failed attempts. Account locked for 15 minutes.");
                    } else {
                        messageLabel.setText("Invalid username or password. (" + attempts + "/" + MAX_ATTEMPTS + " attempts)");
                    }
                    return;
                }

                resetLockout(email);

                if (!storedPassword.equals(PasswordUtils.hashPassword(password))) {
                    try (Connection conn2 = DatabaseConnection.connect()) {
                        PreparedStatement upgrade = conn2.prepareStatement(
                                "UPDATE users SET password = ? WHERE id = ?"
                        );
                        upgrade.setString(1, PasswordUtils.hashPassword(password));
                        upgrade.setInt(2, resultSet.getInt("id"));
                        upgrade.executeUpdate();
                    } catch (Exception ignored) {}
                }

                // --- Remember Me ---
                if (rememberMeCheckbox.isSelected()) {
                    prefs.put("saved_email", email);
                    prefs.put("saved_password", password);
                    prefs.putBoolean("remember_me", true);
                } else {
                    prefs.remove("saved_email");
                    prefs.remove("saved_password");
                    prefs.putBoolean("remember_me", false);
                }

                ActivityLogController.logActivity("User Logged In", 0);

                if (role.equalsIgnoreCase("ADMIN")) {
                    NavigationHelper.navigateTo(emailField, "/fxml/Dashboard.fxml");
                } else if (role.equalsIgnoreCase("STAFF")) {
                    NavigationHelper.navigateTo(emailField, "/fxml/StaffDashboard.fxml");
                } else {
                    messageLabel.setText("Unknown user role.");
                }
            } else {
                attempts++;
                updateFailAttempts(email, attempts);
                if (attempts >= MAX_ATTEMPTS) {
                    updateLockoutTime(email, System.currentTimeMillis());
                    messageLabel.setText("Too many failed attempts. Account locked for 15 minutes.");
                } else {
                    messageLabel.setText("Invalid username or password. (" + attempts + "/" + MAX_ATTEMPTS + " attempts)");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error.");
        }
    }
}
