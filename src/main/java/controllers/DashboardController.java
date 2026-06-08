package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import utils.NavigationHelper;
import utils.TimerService;

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
    private Button btnSidebarUsers;

    @FXML
    private Button btnLogout;

    @FXML
    private VBox cardCreatePassSlip;

    @FXML
    private Label lblDailyCounter;

    @FXML
    private Label lblMonthlyCounter;

    @FXML
    private Label lblLiveTimer;

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

        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarUsers,
                            "/fxml/User.fxml"
                    )
            );

        if (btnLogout != null)
            btnLogout.setOnAction(
                    event -> NavigationHelper.logout(btnLogout)
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

        loadSummaryCountsAsync();
        loadLiveTimer();
        startLiveTimerRefresh();

    }

    private void loadSummaryCountsAsync() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {

                    Connection connection =
                            DatabaseConnection.connect();

                    if (connection == null) {
                        return null;
                    }

                    PreparedStatement dailyStatement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM pass_slips WHERE DATE(time_out) = CURRENT_DATE"
                    );

                    ResultSet dailyResult = dailyStatement.executeQuery();

                    int dailyCount = 0;
                    if (dailyResult.next()) {
                        dailyCount = dailyResult.getInt(1);
                    }

                    int finalDailyCount = dailyCount;
                    Platform.runLater(() -> lblDailyCounter.setText(
                            String.valueOf(finalDailyCount)
                    ));

                    PreparedStatement monthlyStatement = connection.prepareStatement(
                            """
                            SELECT COUNT(*) FROM pass_slips
                            WHERE EXTRACT(YEAR FROM time_out) = EXTRACT(YEAR FROM CURRENT_DATE)
                            AND EXTRACT(MONTH FROM time_out) = EXTRACT(MONTH FROM CURRENT_DATE)
                            """
                    );

                    ResultSet monthlyResult = monthlyStatement.executeQuery();

                    int monthlyCount = 0;
                    if (monthlyResult.next()) {
                        monthlyCount = monthlyResult.getInt(1);
                    }

                    int finalMonthlyCount = monthlyCount;
                    Platform.runLater(() -> lblMonthlyCounter.setText(
                            String.valueOf(finalMonthlyCount)
                    ));

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblDailyCounter.setText("0");
                        lblMonthlyCounter.setText("0");
                    });
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    private void loadLiveTimer() {

        if (lblLiveTimer == null) return;

        int outCount = TimerService.getOutCount();

        if (outCount == 0) {
            lblLiveTimer.setText("No employees currently out");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(outCount).append(" employee(s) currently out:\n\n");

        for (TimerService.OutRecord record : TimerService.getOutRecords().values()) {
            sb.append(record.employeeName)
                    .append(" - ")
                    .append(record.getElapsedText())
                    .append("\n");
        }

        lblLiveTimer.setText(sb.toString());

    }

    private void startLiveTimerRefresh() {

        TimerService.setOnUpdateCallback(this::loadLiveTimer);
        TimerService.startAutoRefresh();

    }

}
