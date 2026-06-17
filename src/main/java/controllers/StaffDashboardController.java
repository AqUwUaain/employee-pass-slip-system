package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import utils.NavigationHelper;
import utils.NotificationHelper;
import utils.SidebarHelper;
import utils.TimerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseConnection;

public class StaffDashboardController {

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
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnOpenPassSlip;

    @FXML
    private Button btnOpenReturn;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblPassSlipsToday;

    @FXML
    private Label lblReturnsToday;

    @FXML
    private Label lblCurrentlyOut;

    @FXML
    private Label lblLiveTimer;

    @FXML
    private StackPane staffDashboardRoot;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private Timeline autoRefreshTimeline;

    @FXML
    private void initialize() {

        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                null, null, null,
                null,
                btnSidebarLogReturn, null,
                btnSidebarSignatures, null,
                btnLogout, btnNotificationsAlert,
                btnSidebarDashboard
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        btnOpenPassSlip.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenPassSlip,
                        "/fxml/PassSlip.fxml"
                )
        );

        btnOpenReturn.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenReturn,
                        "/fxml/Return.fxml"
                )
        );

        loadTodayActivity();
        loadLiveTimer();
        startAutoRefresh();
        startLiveTimerRefresh();
    }

    private void loadTodayActivity() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try (Connection connection = DatabaseConnection.connect()) {
                    if (connection == null) {
                        return null;
                    }

                    PreparedStatement passSlipStmt = connection.prepareStatement(
                            "SELECT COUNT(*) FROM pass_slips WHERE time_out::date = CURRENT_DATE AND status != 'REJECTED'"
                    );
                    ResultSet passSlipResult = passSlipStmt.executeQuery();
                    int passSlipsToday = 0;
                    if (passSlipResult.next()) {
                        passSlipsToday = passSlipResult.getInt(1);
                    }

                    PreparedStatement returnsStmt = connection.prepareStatement(
                            "SELECT COUNT(*) FROM pass_slips WHERE time_in::date = CURRENT_DATE"
                    );
                    ResultSet returnsResult = returnsStmt.executeQuery();
                    int returnsToday = 0;
                    if (returnsResult.next()) {
                        returnsToday = returnsResult.getInt(1);
                    }

                    int currentlyOut = TimerService.getOutCount();

                    final int finalPassSlipsToday = passSlipsToday;
                    final int finalReturnsToday = returnsToday;
                    final int finalCurrentlyOut = currentlyOut;

                    Platform.runLater(() -> {
                        lblPassSlipsToday.setText(String.valueOf(finalPassSlipsToday));
                        lblReturnsToday.setText(String.valueOf(finalReturnsToday));
                        lblCurrentlyOut.setText(String.valueOf(finalCurrentlyOut));
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblPassSlipsToday.setText("0");
                        lblReturnsToday.setText("0");
                        lblCurrentlyOut.setText("0");
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

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            loadTodayActivity();
            loadLiveTimer();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void startLiveTimerRefresh() {
        TimerService.setOnUpdateCallback(() -> {
            loadLiveTimer();
            loadTodayActivity();
        });
        TimerService.startAutoRefresh();
    }

    public void shutdown() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
        TimerService.stopAutoRefresh();
    }
}
