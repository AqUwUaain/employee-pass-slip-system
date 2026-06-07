package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import utils.NavigationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseConnection;

public class DashboardController {

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnHamburgerMenuToggle;

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private VBox cardCreatePassSlip;

    @FXML
    private Label lblDailyCounter;

    @FXML
    private Label lblMonthlyCounter;

    @FXML
    private void initialize() {

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarDashboard,
                        "/fxml/Dashboard.fxml"
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

        btnNotificationsAlert.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnNotificationsAlert,
                        "/fxml/ActivityLog.fxml"
                )
        );

        btnHamburgerMenuToggle.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnHamburgerMenuToggle,
                        "/fxml/User.fxml"
                )
        );

        cardCreatePassSlip.setOnMouseClicked(
                event -> NavigationHelper.navigateTo(
                        cardCreatePassSlip,
                        "/fxml/PassSlip.fxml"
                )
        );

        loadSummaryCounts();

    }

    private void loadSummaryCounts() {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            if (connection == null) {
                return;
            }

            PreparedStatement dailyStatement =
                    connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE DATE(time_out) = CURRENT_DATE
                            """
                    );

            ResultSet dailyResult =
                    dailyStatement.executeQuery();

            if (dailyResult.next()) {
                lblDailyCounter.setText(
                        String.valueOf(
                                dailyResult.getInt(1)
                        )
                );
            }

            PreparedStatement monthlyStatement =
                    connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE EXTRACT(YEAR FROM time_out) = EXTRACT(YEAR FROM CURRENT_DATE)
                            AND EXTRACT(MONTH FROM time_out) = EXTRACT(MONTH FROM CURRENT_DATE)
                            """
                    );

            ResultSet monthlyResult =
                    monthlyStatement.executeQuery();

            if (monthlyResult.next()) {
                lblMonthlyCounter.setText(
                        String.valueOf(
                                monthlyResult.getInt(1)
                        )
                );
            }

        }
        catch (Exception e) {

            lblDailyCounter.setText("0");
            lblMonthlyCounter.setText("0");

        }

    }
}
