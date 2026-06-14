package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivityLogController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnSidebarLogReturn;

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
    private Button btnClearLogsFilter;

    @FXML
    private TextField txtSearchLogs;

    @FXML
    private VBox logsFeedContentContainer;

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

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarLogReturn, "/fxml/Return.fxml"));
        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml"));
        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));

        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> utils.NotificationHelper.toggle(btnNotificationsAlert));
        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(e -> NavigationHelper.navigateTo(btnHamburgerMenuToggle, "/fxml/User.fxml"));

        btnClearLogsFilter.setOnAction(
                event -> {
                    txtSearchLogs.clear();
                    renderLogs(
                            ReportsController.getLogs()
                    );
                }
        );

        txtSearchLogs.textProperty().addListener(
                (observable, oldValue, newValue) -> renderLogs(
                        ReportsController.getLogs()
                                .stream()
                                .filter(log ->
                                        newValue == null
                                                || newValue.isBlank()
                                                || log.getUsername().toLowerCase().contains(newValue.toLowerCase())
                                                || log.getAction().toLowerCase().contains(newValue.toLowerCase())
                                )
                                .toList()
                )
        );

        renderLogs(
                ReportsController.getLogs()
        );

    }

    public static void logActivity(String action, int employeeId) {
        logActivity(action, "", employeeId);
    }

    public static void logActivity(String action, String description, int employeeId) {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    INSERT INTO activity_logs (username, action, description, employee_id, user_id, timestamp)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(1, Session.currentUsername != null ? Session.currentUsername : "System");
            statement.setString(2, action);
            statement.setString(3, description);
            statement.setInt(4, employeeId);
            statement.setInt(5, Session.currentUserId);
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));

            statement.executeUpdate();

        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void renderLogs(List<models.ActivityLog> logs) {

        logsFeedContentContainer.getChildren().clear();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (models.ActivityLog log : logs) {

            HBox row = new HBox();

            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-padding: 12px 16px; -fx-background-radius: 8px;"
            );
            row.setOnMouseEntered(ev -> row.setStyle("-fx-padding: 12px 16px; -fx-background-radius: 8px; -fx-background-color: #3D3229;"));
            row.setOnMouseExited(ev -> row.setStyle("-fx-padding: 12px 16px; -fx-background-radius: 8px;"));

            VBox accent = new VBox();
            accent.setStyle(
                    "-fx-background-color: #D4A853; -fx-pref-width: 4px; -fx-pref-height: 22px; -fx-background-radius: 2px;"
            );

            Label message = new Label(
                    log.getUsername() + " - " + log.getAction()
            );
            message.setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #E7E5E4; -fx-padding: 0 0 0 12px;"
            );

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label timestamp = new Label(
                    log.getTimestamp().format(formatter)
            );
            timestamp.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #78716C;"
            );

            row.getChildren().addAll(accent, message, spacer, timestamp);

            logsFeedContentContainer.getChildren().add(row);

        }

    }

}
