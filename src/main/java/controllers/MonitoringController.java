package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import utils.NavigationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.time.Duration;
import java.time.LocalDateTime;

public class MonitoringController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnRefreshMonitoringFeed;

    @FXML
    private Label lblScannerStatus;

    @FXML
    private Label lblTotalInCampusCount;

    @FXML
    private Label lblActiveAlertsCount;

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

        btnRefreshMonitoringFeed.setOnAction(
                event -> loadSummary()
        );

        loadSummary();

    }

    public static void updateExpiredPassSlips() {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT id, time_out
                    FROM pass_slips
                    WHERE status = 'OUT'
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                int passSlipId =
                        resultSet.getInt("id");

                Timestamp timeOutTimestamp =
                        resultSet.getTimestamp("time_out");

                LocalDateTime timeOut =
                        timeOutTimestamp.toLocalDateTime();

                LocalDateTime now =
                        LocalDateTime.now();

                long hours =
                        Duration.between(timeOut, now).toHours();

                if(hours >= 1) {

                    String updateQuery = """
                            UPDATE pass_slips
                            SET status = 'EXPIRED'
                            WHERE id = ?
                            """;

                    PreparedStatement updateStatement =
                            connection.prepareStatement(updateQuery);

                    updateStatement.setInt(1, passSlipId);

                    updateStatement.executeUpdate();

                }

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static String getMonitoringData() {

        updateExpiredPassSlips();

        StringBuilder data = new StringBuilder();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT
                   pass_slips.id,
                   employees.first_name,
                   employees.last_name,
                   pass_slips.reason,
                   pass_slips.time_out,
                   pass_slips.time_in,
                   pass_slips.duration,
                   pass_slips.status
                   FROM pass_slips
                   JOIN employees
                   ON pass_slips.employee_id = employees.id
                   ORDER BY pass_slips.id DESC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                data.append("PASS SLIP ID: ")
                        .append(resultSet.getInt("id"))
                        .append("\n");

                data.append("EMPLOYEE: ")
                        .append(resultSet.getString("first_name"))
                        .append(" ")
                        .append(resultSet.getString("last_name"))
                        .append("\n");

                data.append("REASON: ")
                        .append(resultSet.getString("reason"))
                        .append("\n");

                data.append("TIME OUT: ")
                        .append(resultSet.getString("time_out"))
                        .append("\n");

                data.append("TIME IN: ")
                        .append(resultSet.getString("time_in"))
                        .append("\n");

                data.append("DURATION: ")
                        .append(resultSet.getString("duration"))
                        .append("\n");

                data.append("STATUS: ")
                        .append(resultSet.getString("status"))
                        .append("\n");

                data.append("\n-----------------------------------\n\n");

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return data.toString();

    }

    private void loadSummary() {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            if (connection == null) {
                lblScannerStatus.setText("OFFLINE");
                lblTotalInCampusCount.setText("0");
                lblActiveAlertsCount.setText("0");
                return;
            }

            lblScannerStatus.setText("ONLINE");

            PreparedStatement outStatement =
                    connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE status = 'OUT'
                            """
                    );

            ResultSet outResult =
                    outStatement.executeQuery();

            if (outResult.next()) {
                lblTotalInCampusCount.setText(
                        String.valueOf(
                                outResult.getInt(1)
                        )
                );
            }

            PreparedStatement expiredStatement =
                    connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE status = 'EXPIRED'
                            """
                    );

            ResultSet expiredResult =
                    expiredStatement.executeQuery();

            if (expiredResult.next()) {
                lblActiveAlertsCount.setText(
                        String.valueOf(
                                expiredResult.getInt(1)
                        )
                );
            }

        }
        catch (Exception e) {

            lblScannerStatus.setText("OFFLINE");
            lblTotalInCampusCount.setText("0");
            lblActiveAlertsCount.setText("0");

        }

    }

}
