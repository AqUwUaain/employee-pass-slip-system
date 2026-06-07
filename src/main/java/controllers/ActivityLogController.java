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
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
    private Button btnClearLogsFilter;

    @FXML
    private TextField txtSearchLogs;

    @FXML
    private VBox logsFeedContentContainer;

    @FXML
    private void initialize() {

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(
                        btnSidebarDashboard
                )
        );

        btnSidebarMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarMonitoring,
                        "/fxml/Monitoring.fxml"
                )
        );

        btnSidebarEmployees.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarEmployees,
                        "/fxml/EmployeeController.fxml"
                )
        );

        btnSidebarReports.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarReports,
                        "/fxml/Reports.fxml"
                )
        );

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

    public static void logActivity(String action) {

        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query =
                    """
                    INSERT INTO activity_logs
                    (
                        username,
                        action
                    )
                    VALUES (?, ?)
                    """;



            PreparedStatement statement =
                    connection.prepareStatement(query);



            statement.setString(
                    1,
                    Session.currentUsername
            );



            statement.setString(
                    2,
                    action
            );



            statement.executeUpdate();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void renderLogs(List<models.ActivityLog> logs) {

        logsFeedContentContainer.getChildren().clear();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm"
                );

        for (models.ActivityLog log : logs) {

            HBox row =
                    new HBox();

            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-padding: 15px 20px; -fx-border-color: transparent transparent #edf2f7 transparent; -fx-border-width: 0 0 1px 0;"
            );

            VBox accent =
                    new VBox();

            accent.setStyle(
                    "-fx-background-color: #ffe600; -fx-pref-width: 4px; -fx-pref-height: 22px;"
            );

            Label message =
                    new Label(
                            log.getUsername()
                                    + " - "
                                    + log.getAction()
                    );

            message.setStyle(
                    "-fx-font-size: 14px; -fx-text-fill: #2d3748; -fx-padding: 0 0 0 12px;"
            );

            Region spacer =
                    new Region();

            HBox.setHgrow(
                    spacer,
                    Priority.ALWAYS
            );

            Label timestamp =
                    new Label(
                            log.getCreatedAt().format(formatter)
                    );

            timestamp.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #a0aec0;"
            );

            row.getChildren().addAll(
                    accent,
                    message,
                    spacer,
                    timestamp
            );

            logsFeedContentContainer.getChildren().add(row);

        }

    }

}
