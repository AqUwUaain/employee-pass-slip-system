package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utils.NavigationHelper;
import utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnSidebarUsers;

    @FXML
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnHamburgerMenuToggle;

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
    private Label lblUserMessage;

    @FXML
    private Button btnClearUserForm;

    @FXML
    private Button btnSaveUserRegistry;

    @FXML
    private Button btnRevokeAccess;

    @FXML
    private TableView<models.User> tblSystemUsersView;

    @FXML
    private TableColumn<models.User, Number> colUserRefId;

    @FXML
    private TableColumn<models.User, String> colUserFullName;

    @FXML
    private TableColumn<models.User, String> colUserRoleTier;

    @FXML
    private void initialize() {

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnSidebarDashboard)
        );

        btnSidebarMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(btnSidebarMonitoring, "/fxml/Monitoring.fxml")
        );

        btnSidebarEmployees.setOnAction(
                event -> NavigationHelper.navigateTo(btnSidebarEmployees, "/fxml/EmployeeController.fxml")
        );

        btnSidebarReports.setOnAction(
                event -> NavigationHelper.navigateTo(btnSidebarReports, "/fxml/Reports.fxml")
        );

        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarUsers, "/fxml/User.fxml"));
        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml"));
        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));

        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> NavigationHelper.navigateTo(btnNotificationsAlert, "/fxml/ActivityLog.fxml"));
        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(e -> NavigationHelper.navigateTo(btnHamburgerMenuToggle, "/fxml/User.fxml"));

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

        btnSaveUserRegistry.setOnAction(event -> {
            createUser(
                    txtUserId,
                    txtUserPassword,
                    cmbUserRole,
                    lblUserMessage
            );
            refreshUsers();
        });

        btnRevokeAccess.setOnAction(event -> revokeSelectedUser());

        refreshUsers();

    }

    public static void createUser(

            TextField usernameField,
            PasswordField passwordField,
            ComboBox<String> roleBox,
            Label messageLabel

    ) {

        String username =
                usernameField.getText().trim();

        String password =
                passwordField.getText().trim();

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

        if (password.length() < 4) {
            messageLabel.setText("PASSWORD TOO SHORT");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (role == null) {
            messageLabel.setText("SELECT ROLE");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String checkQuery =
                    "SELECT * FROM users WHERE username = ?";

            PreparedStatement checkStatement =
                    connection.prepareStatement(checkQuery);

            checkStatement.setString(1, username);

            ResultSet resultSet =
                    checkStatement.executeQuery();

            if (resultSet.next()) {
                messageLabel.setText("USERNAME ALREADY EXISTS");
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
                passwordField.clear();
                roleBox.setValue(null);

            } else {
                messageLabel.setText("FAILED TO CREATE USER");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }

            statement.close();
            resultSet.close();
            checkStatement.close();
            connection.close();

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
        txtUserPassword.clear();
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

}
