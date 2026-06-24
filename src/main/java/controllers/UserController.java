package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import utils.NavigationHelper;
import utils.PasswordUtils;
import utils.ConfirmDialog;
import utils.SidebarHelper;

import utils.ThemeManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployeeDirectory;

    @FXML
    private Button btnSidebarAddEmployee;

    @FXML
    private Button btnSidebarImportEmployee;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnSidebarLogReturn;

    @FXML
    private Button btnSidebarUsers;

    @FXML
    private Button btnSidebarSignatures;

    @FXML
    private Button btnSidebarRequests;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnThemeToggle;

    @FXML
    private Label lblTotalUsers;

    @FXML
    private Label lblActiveSessions;

    @FXML
    private TextField txtUserId;

    @FXML
    private TextField txtUserName;

    @FXML
    private ComboBox<String> cmbUserRole;

    @FXML
    private PasswordField txtUserPassword;

    @FXML
    private Label btnTogglePassword;

    @FXML
    private Label lblUserMessage;

    @FXML
    private Button btnClearUserForm;

    @FXML
    private Button btnSaveUserRegistry;

    @FXML
    private Button btnRevokeAccess;

    @FXML
    private Button btnChangePassword;

    @FXML
    private Button btnEditUser;

    @FXML
    private TableView<models.User> tblSystemUsersView;

    @FXML
    private TableColumn<models.User, Number> colUserRefId;

    @FXML
    private TableColumn<models.User, String> colUserFullName;

    @FXML
    private TableColumn<models.User, String> colUserRoleTier;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private javafx.scene.Node savedCenterContent;
    private boolean dialogOpen = false;

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, btnNotificationsAlert,
                btnSidebarUsers, btnThemeToggle,
                btnManageEmployees, manageEmployeesSubMenu
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        if (manageEmployeesSubMenu != null) {
            manageEmployeesSubMenu.setVisible(true);
            manageEmployeesSubMenu.setManaged(true);
        }

        cmbUserRole.setItems(
                FXCollections.observableArrayList("ADMIN", "STAFF")
        );

        colUserRefId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(
                        cellData.getValue().getId()
                )
        );

        colUserFullName.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getUsername()
                )
        );

        colUserRoleTier.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRole()
                )
        );

        btnClearUserForm.setOnAction(event -> clearForm());

        final double[] createLockedHeight = {-1};
        btnTogglePassword.setOnMouseClicked(event -> {
            if (createLockedHeight[0] < 0) {
                PasswordField current = (PasswordField) btnTogglePassword.getParent().getChildrenUnmodifiable().stream()
                        .filter(c -> c instanceof PasswordField).findFirst().orElse(null);
                createLockedHeight[0] = current != null ? current.getHeight() : 36;
            }
            double lockedHeight = createLockedHeight[0];
            if (btnTogglePassword.getText().equals("Show")) {
                PasswordField current = (PasswordField) btnTogglePassword.getParent().getChildrenUnmodifiable().stream()
                        .filter(c -> c instanceof PasswordField).findFirst().orElse(null);
                if (current == null) return;

                TextField visible = new TextField(current.getText());
                visible.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 8px 4px; -fx-font-size: 13px;");
                visible.setPromptText("Enter password...");
                visible.setMaxWidth(Double.MAX_VALUE);
                visible.setMinHeight(lockedHeight);
                visible.setPrefHeight(lockedHeight);
                visible.setMaxHeight(lockedHeight);
                HBox parent = (HBox) current.getParent();
                HBox.setHgrow(visible, javafx.scene.layout.Priority.ALWAYS);
                int idx = parent.getChildren().indexOf(current);
                parent.getChildren().set(idx, visible);
                txtUserPassword = null;
                btnTogglePassword.setText("Hide");
                visible.requestFocus();
                visible.positionCaret(visible.getText().length());
            } else {
                TextField visible = (TextField) ((HBox) btnTogglePassword.getParent()).getChildren().stream()
                        .filter(c -> c instanceof TextField).findFirst().orElse(null);
                if (visible != null) {
                    PasswordField hidden = new PasswordField();
                    hidden.setText(visible.getText());
                    hidden.setPromptText("Enter password...");
                    hidden.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 8px 4px; -fx-font-size: 13px;");
                    hidden.setMaxWidth(Double.MAX_VALUE);
                    hidden.setMinHeight(lockedHeight);
                    hidden.setPrefHeight(lockedHeight);
                    hidden.setMaxHeight(lockedHeight);
                    HBox parent = (HBox) visible.getParent();
                    HBox.setHgrow(hidden, javafx.scene.layout.Priority.ALWAYS);
                    int idx = parent.getChildren().indexOf(visible);
                    parent.getChildren().set(idx, hidden);
                    txtUserPassword = hidden;
                    btnTogglePassword.setText("Show");
                    hidden.requestFocus();
                }
            }
        });

        btnSaveUserRegistry.setOnAction(event -> {
            String passwordText = "";
            if (txtUserPassword != null) {
                passwordText = txtUserPassword.getText().trim();
            } else {
                TextField vis = (TextField) btnTogglePassword.getParent().getChildrenUnmodifiable().stream()
                        .filter(c -> c instanceof TextField).findFirst().orElse(null);
                if (vis != null) passwordText = vis.getText().trim();
            }
            createUser(
                    txtUserId,
                    passwordText,
                    cmbUserRole,
                    lblUserMessage
            );
            refreshUsers();
        });

        btnRevokeAccess.setOnAction(event -> revokeSelectedUser());

        btnChangePassword.setOnAction(event -> showChangePasswordDialog());

        if (btnEditUser != null)
            btnEditUser.setOnAction(event -> showEditUserDialog());

        refreshUsers();

    }

    public static void createUser(

            TextField usernameField,
            String password,
            ComboBox<String> roleBox,
            Label messageLabel

    ) {

        String username =
                usernameField.getText().trim();

        String role =
                roleBox.getValue();

        if (username.isEmpty()) {
            messageLabel.setText("USERNAME REQUIRED");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (!username.contains("@")) {
            messageLabel.setText("USERNAME MUST CONTAIN '@'");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (!username.toLowerCase().endsWith("@pup.edu.ph")) {
            messageLabel.setText("EMAIL MUST END WITH @PUP.EDU.PH");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (password.isEmpty()) {
            messageLabel.setText("PASSWORD REQUIRED");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (password.length() < 5) {
            messageLabel.setText("PASSWORD MUST BE AT LEAST 5 CHARACTERS");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            messageLabel.setText("PASSWORD MUST CONTAIN AT LEAST 1 UPPERCASE LETTER");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (!password.matches(".*[0-9].*")) {
            messageLabel.setText("PASSWORD MUST CONTAIN AT LEAST 1 NUMBER");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (!password.matches(".*[!@#%*].*")) {
            messageLabel.setText("PASSWORD MUST CONTAIN AT LEAST 1 SPECIAL CHARACTER (!@#%*)");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (role == null) {
            messageLabel.setText("SELECT ROLE");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {

            String checkQuery =
                    "SELECT * FROM users WHERE username = ?";

            PreparedStatement checkStatement =
                    connection.prepareStatement(checkQuery);

            checkStatement.setString(1, username);

            ResultSet resultSet =
                    checkStatement.executeQuery();

            if (resultSet.next()) {
                messageLabel.setText("USERNAME ALREADY EXISTS");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword(password);

            String insertQuery =
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

            PreparedStatement statement =
                    connection.prepareStatement(insertQuery);

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, role);

            int inserted = statement.executeUpdate();

            if (inserted > 0) {

                ActivityLogController.logActivity(
                        "Created User: " + username,
                        0
                );

                messageLabel.setText("USER CREATED SUCCESSFULLY");
                messageLabel.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");

                usernameField.clear();
                roleBox.setValue(null);

            } else {
                messageLabel.setText("FAILED TO CREATE USER");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }

            statement.close();
            resultSet.close();
            checkStatement.close();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("DATABASE ERROR: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }

    }

    private void refreshUsers() {

        tblSystemUsersView.setItems(
                UserListController.getUsers()
        );

        lblTotalUsers.setText(
                String.valueOf(
                        tblSystemUsersView.getItems().size()
                )
        );

        lblActiveSessions.setText("1 Active Session");

    }

    private void clearForm() {
        txtUserId.clear();
        txtUserName.clear();
        if (txtUserPassword != null) {
            txtUserPassword.clear();
        } else {
            TextField vis = (TextField) btnTogglePassword.getParent().getChildrenUnmodifiable().stream()
                    .filter(c -> c instanceof TextField).findFirst().orElse(null);
            if (vis != null) vis.clear();
        }
        cmbUserRole.setValue(null);
        lblUserMessage.setText("");
        lblUserMessage.setStyle("");
    }

    private void revokeSelectedUser() {

        models.User selectedUser =
                tblSystemUsersView.getSelectionModel()
                        .getSelectedItem();

        if (selectedUser == null) {
            lblUserMessage.setText("Select a user to revoke.");
            return;
        }

        boolean confirmed = ConfirmDialog.show(
                tblSystemUsersView,
                "Delete User",
                "Are you sure you want to delete user \"" + selectedUser.getUsername() + "\"?\nThis action cannot be undone."
        );

        if (!confirmed) return;

        boolean deleted =
                DeleteUserController.deleteUser(
                        selectedUser.getId()
                );

        if (deleted) {
            lblUserMessage.setText("User removed.");
            refreshUsers();
        } else {
            lblUserMessage.setText("Failed to remove user.");
        }

    }

    private void showChangePasswordDialog() {
        if (dialogOpen) return;

        models.User selectedUser =
                tblSystemUsersView.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            lblUserMessage.setText("Select a user to change password.");
            lblUserMessage.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        dialogOpen = true;

        boolean dark = ThemeManager.isDark();
        String bgColor = dark ? "#1F1B1B" : "#FDF8EE";
        String textColor = dark ? "#F5F5F4" : "#1C1917";
        String mutedText = dark ? "#A8A29E" : "#6B6358";
        String borderColor = dark ? "#D4A853" : "#800517";
        String inputBg = dark ? "#2D2520" : "#FDF8EE";
        String inputBorder = dark ? "#3D3229" : "#C0B89E";
        String cancelBg = dark ? "#3D3229" : "#E8DFC8";
        String confirmBg = dark ? "#D4A853" : "#800517";
        String confirmText = dark ? "#1C0A04" : "#FFFFFF";

        javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) tblSystemUsersView.getScene().getRoot();
        if (savedCenterContent == null) {
            savedCenterContent = root.getCenter();
        }

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setAlignment(Pos.CENTER);

        VBox dialog = new VBox(12);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPrefWidth(420);
        dialog.setMaxWidth(420);
        dialog.setPrefHeight(450);
        dialog.setMaxHeight(450);
        dialog.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px; " +
                "-fx-padding: 28px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0.25, 0, 6);"
        );

        Label icon = new Label("\u1F512");
        icon.setStyle("-fx-font-size: 32px;");

        Label title = new Label("Change Password");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Label subtitle = new Label("for " + selectedUser.getUsername());
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + mutedText + ";");

        Label oldPwLabel = new Label("CURRENT PASSWORD");
        oldPwLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + "; -fx-font-size: 11px;");

        HBox oldPwBox = createPasswordFieldWithEye("Enter current password...");

        Label newPwLabel = new Label("NEW PASSWORD");
        newPwLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + "; -fx-font-size: 11px;");

        HBox newPwBox = createPasswordFieldWithEye("Enter new password...");

        Label confirmPwLabel = new Label("CONFIRM NEW PASSWORD");
        confirmPwLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + "; -fx-font-size: 11px;");

        HBox confirmPwBox = createPasswordFieldWithEye("Re-enter new password...");

        Label rulesLabel = new Label("Min 5 chars, 1 uppercase, 1 number, 1 special (!@#%*)");
        rulesLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + mutedText + "; -fx-wrap-text: true;");
        rulesLabel.setMaxWidth(360);

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FCA5A5; -fx-text-alignment: center; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(360);
        msgLabel.setTextAlignment(TextAlignment.CENTER);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle(
                "-fx-background-color: " + cancelBg + "; -fx-text-fill: " + mutedText + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 24px; -fx-cursor: hand;"
        );

        Button btnConfirm = new Button("Update Password");
        btnConfirm.setStyle(
                "-fx-background-color: " + confirmBg + "; -fx-text-fill: " + confirmText + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 24px; -fx-cursor: hand;"
        );

        buttons.getChildren().addAll(btnCancel, btnConfirm);

        dialog.getChildren().addAll(icon, title, subtitle, oldPwLabel, oldPwBox, newPwLabel, newPwBox, confirmPwLabel, confirmPwBox, rulesLabel, msgLabel, buttons);
        overlay.getChildren().add(dialog);
        root.setCenter(overlay);

        btnCancel.setOnAction(e -> {
            dialogOpen = false;
            root.setCenter(savedCenterContent);
        });

        btnConfirm.setOnAction(e -> {
            String oldPw = getPasswordFromBox(oldPwBox).trim();
            String newPw = getPasswordFromBox(newPwBox).trim();
            String confirmPw = getPasswordFromBox(confirmPwBox).trim();

            if (oldPw.isEmpty()) {
                msgLabel.setText("Current password is required.");
                return;
            }

            if (newPw.isEmpty()) {
                msgLabel.setText("New password is required.");
                return;
            }

            if (newPw.length() < 5) {
                msgLabel.setText("Password must be at least 5 characters.");
                return;
            }

            if (!newPw.matches(".*[A-Z].*")) {
                msgLabel.setText("Password must contain at least 1 uppercase letter.");
                return;
            }

            if (!newPw.matches(".*[0-9].*")) {
                msgLabel.setText("Password must contain at least 1 number.");
                return;
            }

            if (!newPw.matches(".*[!@#%*].*")) {
                msgLabel.setText("Password must contain at least 1 special character (!@#%*).");
                return;
            }

            if (!newPw.equals(confirmPw)) {
                msgLabel.setText("New passwords do not match.");
                return;
            }

            try (Connection connection = DatabaseConnection.connect()) {

                PreparedStatement verifyStmt = connection.prepareStatement(
                        "SELECT password FROM users WHERE id = ?"
                );
                verifyStmt.setInt(1, selectedUser.getId());
                ResultSet rs = verifyStmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (!PasswordUtils.verifyPassword(oldPw, storedHash)) {
                        msgLabel.setText("Current password is incorrect.");
                        rs.close();
                        verifyStmt.close();
                        return;
                    }
                }
                rs.close();
                verifyStmt.close();

                String hashed = PasswordUtils.hashPassword(newPw);
                PreparedStatement updateStmt = connection.prepareStatement(
                        "UPDATE users SET password = ? WHERE id = ?"
                );
                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, selectedUser.getId());
                int updated = updateStmt.executeUpdate();
                updateStmt.close();

                if (updated > 0) {
                    dialogOpen = false;
                    root.setCenter(savedCenterContent);
                    lblUserMessage.setText("Password updated for " + selectedUser.getUsername());
                    lblUserMessage.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                    ActivityLogController.logActivity(
                            "Changed password for user: " + selectedUser.getUsername(), 0);
                } else {
                    msgLabel.setText("Failed to update password.");
                }
            } catch (Exception ex) {
                msgLabel.setText("Database error.");
                ex.printStackTrace();
            }
        });

        Platform.runLater(() -> getPasswordFieldFromBox(oldPwBox).requestFocus());
    }

    private void showEditUserDialog() {
        models.User selectedUser =
                tblSystemUsersView.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            lblUserMessage.setText("Select a user to edit.");
            lblUserMessage.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        boolean dark = ThemeManager.isDark();
        String bgColor = dark ? "#1F1B1B" : "#FDF8EE";
        String textColor = dark ? "#F5F5F4" : "#1C1917";
        String mutedText = dark ? "#A8A29E" : "#6B6358";
        String borderColor = dark ? "#D4A853" : "#800517";
        String inputBg = dark ? "#2D2520" : "#FDF8EE";
        String inputBorder = dark ? "#3D3229" : "#C0B89E";
        String cancelBg = dark ? "#3D3229" : "#E8DFC8";
        String confirmBg = dark ? "#D4A853" : "#800517";
        String confirmText = dark ? "#1C0A04" : "#FFFFFF";

        javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) tblSystemUsersView.getScene().getRoot();
        if (savedCenterContent == null) {
            savedCenterContent = root.getCenter();
        }

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setAlignment(Pos.CENTER);

        VBox dialog = new VBox(12);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPrefWidth(420);
        dialog.setMaxWidth(420);
        dialog.setPrefHeight(380);
        dialog.setMaxHeight(380);
        dialog.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px; " +
                "-fx-padding: 28px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0.25, 0, 6);"
        );

        Label icon = new Label("\u270F");
        icon.setStyle("-fx-font-size: 32px;");

        Label title = new Label("Edit User");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Label subtitle = new Label("Editing: " + selectedUser.getUsername());
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + mutedText + ";");

        Label emailLabel = new Label("USERNAME (EMAIL)");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + "; -fx-font-size: 11px;");

        TextField emailField = new TextField(selectedUser.getUsername());
        emailField.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 13px;");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label roleLabel = new Label("ROLE");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + "; -fx-font-size: 11px;");

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "STAFF"));
        roleBox.setValue(selectedUser.getRole());
        roleBox.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 13px;");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FCA5A5; -fx-text-alignment: center; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(360);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle(
                "-fx-background-color: " + cancelBg + "; -fx-text-fill: " + mutedText + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 24px; -fx-cursor: hand;"
        );

        Button btnConfirm = new Button("Save Changes");
        btnConfirm.setStyle(
                "-fx-background-color: " + confirmBg + "; -fx-text-fill: " + confirmText + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 24px; -fx-cursor: hand;"
        );

        buttons.getChildren().addAll(btnCancel, btnConfirm);

        dialog.getChildren().addAll(icon, title, subtitle, emailLabel, emailField, roleLabel, roleBox, msgLabel, buttons);
        overlay.getChildren().add(dialog);
        root.setCenter(overlay);

        btnCancel.setOnAction(e -> root.setCenter(savedCenterContent));

        btnConfirm.setOnAction(e -> {
            String newUsername = emailField.getText().trim();
            String newRole = roleBox.getValue();

            if (newUsername.isEmpty()) {
                msgLabel.setText("Username is required.");
                return;
            }

            if (!newUsername.contains("@") || !newUsername.toLowerCase().endsWith("@pup.edu.ph")) {
                msgLabel.setText("Email must end with @pup.edu.ph");
                return;
            }

            if (newRole == null) {
                msgLabel.setText("Select a role.");
                return;
            }

            try (Connection connection = DatabaseConnection.connect()) {

                if (!newUsername.equals(selectedUser.getUsername())) {
                    PreparedStatement checkStmt = connection.prepareStatement(
                            "SELECT id FROM users WHERE username = ? AND id != ?"
                    );
                    checkStmt.setString(1, newUsername);
                    checkStmt.setInt(2, selectedUser.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        msgLabel.setText("Username already exists.");
                        rs.close();
                        checkStmt.close();
                        return;
                    }
                    rs.close();
                    checkStmt.close();
                }

                PreparedStatement updateStmt = connection.prepareStatement(
                        "UPDATE users SET username = ?, role = ? WHERE id = ?"
                );
                updateStmt.setString(1, newUsername);
                updateStmt.setString(2, newRole);
                updateStmt.setInt(3, selectedUser.getId());
                int updated = updateStmt.executeUpdate();
                updateStmt.close();

                if (updated > 0) {
                    root.setCenter(savedCenterContent);
                    lblUserMessage.setText("User updated successfully.");
                    lblUserMessage.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                    refreshUsers();
                    ActivityLogController.logActivity(
                            "Updated user: " + selectedUser.getUsername() + " → " + newUsername + " (" + newRole + ")", 0);
                } else {
                    msgLabel.setText("Failed to update user.");
                }
            } catch (Exception ex) {
                msgLabel.setText("Database error.");
                ex.printStackTrace();
            }
        });

        Platform.runLater(emailField::requestFocus);
    }

    private HBox createPasswordFieldWithEye(String promptText) {
        String pwStyle = "-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 8px 4px; -fx-font-size: 13px;";

        PasswordField pwField = new PasswordField();
        pwField.setPromptText(promptText);
        pwField.setStyle(pwStyle);
        pwField.setMaxWidth(Double.MAX_VALUE);

        Label eye = new Label("Show");
        eye.getStyleClass().add("pw-eye-label");

        HBox box = new HBox();
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.getStyleClass().add("pw-field-wrapper");
        box.getChildren().addAll(pwField, eye);
        HBox.setHgrow(pwField, javafx.scene.layout.Priority.ALWAYS);

        final double[] lockedHeight = {-1};

        eye.setOnMouseClicked(e -> {
            if (lockedHeight[0] < 0) {
                lockedHeight[0] = box.getHeight();
                if (lockedHeight[0] <= 0) lockedHeight[0] = 36;
            }
            if (eye.getText().equals("Show")) {
                PasswordField current = (PasswordField) box.getChildren().stream()
                        .filter(c -> c instanceof PasswordField).findFirst().orElse(null);
                if (current == null) return;
                TextField visible = new TextField(current.getText());
                visible.setStyle(pwStyle);
                visible.setPromptText(promptText);
                visible.setMaxWidth(Double.MAX_VALUE);
                visible.setMinHeight(lockedHeight[0]);
                visible.setPrefHeight(lockedHeight[0]);
                visible.setMaxHeight(lockedHeight[0]);
                HBox.setHgrow(visible, javafx.scene.layout.Priority.ALWAYS);
                int idx = box.getChildren().indexOf(current);
                box.getChildren().set(idx, visible);
                eye.setText("Hide");
                visible.requestFocus();
            } else {
                TextField visible = (TextField) box.getChildren().stream()
                        .filter(c -> c instanceof TextField).findFirst().orElse(null);
                if (visible != null) {
                    PasswordField hidden = new PasswordField();
                    hidden.setText(visible.getText());
                    hidden.setPromptText(promptText);
                    hidden.setStyle(pwStyle);
                    hidden.setMaxWidth(Double.MAX_VALUE);
                    hidden.setMinHeight(lockedHeight[0]);
                    hidden.setPrefHeight(lockedHeight[0]);
                    hidden.setMaxHeight(lockedHeight[0]);
                    HBox.setHgrow(hidden, javafx.scene.layout.Priority.ALWAYS);
                    int idx = box.getChildren().indexOf(visible);
                    box.getChildren().set(idx, hidden);
                    eye.setText("Show");
                    hidden.requestFocus();
                }
            }
        });

        return box;
    }

    private String getPasswordFromBox(HBox box) {
        for (javafx.scene.Node child : box.getChildren()) {
            if (child instanceof PasswordField pf) return pf.getText();
            if (child instanceof TextField tf) return tf.getText();
        }
        return "";
    }

    private PasswordField getPasswordFieldFromBox(HBox box) {
        for (javafx.scene.Node child : box.getChildren()) {
            if (child instanceof PasswordField pf) return pf;
        }
        return null;
    }

}
