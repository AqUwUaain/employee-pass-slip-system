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
    private Button btnSidebarLogReturn;

    @FXML
    private Button btnSidebarSignatures;

    @FXML
    private Button btnOpenPassSlip;

    @FXML
    private Button btnOpenReturn;

    @FXML
    private Button btnOpenMonitoring;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnHamburgerMenuToggle;

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

        NavigationHelper.setActiveButton(btnSidebarDashboard);

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarDashboard,
                        "/fxml/StaffDashboard.fxml"
                )
        );

        btnSidebarMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarMonitoring,
                        "/fxml/Monitoring.fxml"
                )
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarLogReturn,
                            "/fxml/Return.fxml"
                    )
            );

        if (btnSidebarSignatures != null)
            btnSidebarSignatures.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarSignatures,
                            "/fxml/SignatureManager.fxml"
                    )
            );

        if (btnLogout != null)
            btnLogout.setOnAction(
                    event -> NavigationHelper.logout(btnLogout)
            );

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

        btnOpenMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenMonitoring,
                        "/fxml/Monitoring.fxml"
                )
        );

        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(
                    event -> NotificationHelper.toggle(btnNotificationsAlert)
            );

        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(
                    event -> showAccessRestrictedDialog()
            );

        loadTodayActivity();
        loadLiveTimer();
        startAutoRefresh();
        startLiveTimerRefresh();
    }

    private void showAccessRestrictedDialog() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

        VBox dialog = new VBox(16);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPrefWidth(420);
        dialog.setMaxWidth(420);
        dialog.setPrefHeight(250);
        dialog.setMaxHeight(250);
        dialog.setStyle(
                "-fx-background-color: #1F1B1B; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #FFD700; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px; " +
                "-fx-padding: 30px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0.25, 0, 6);"
        );

        Label icon = new Label("\u26D4");
        icon.setStyle("-fx-font-size: 40px;");

        Label title = new Label("Access Restricted");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F5F5F4;");

        Label message = new Label("Only Administrator accounts can access this feature.");
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #A8A29E; -fx-text-alignment: center; -fx-wrap-text: true;");
        message.setMaxWidth(320);
        message.setTextAlignment(TextAlignment.CENTER);

        Button okButton = new Button("OK");
        okButton.setStyle(
                "-fx-background-color: #D4A853; -fx-text-fill: #1C0A04; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 32px; -fx-cursor: hand;"
        );
        okButton.setOnAction(e -> staffDashboardRoot.getChildren().remove(overlay));

        dialog.getChildren().addAll(icon, title, message, okButton);
        StackPane.setAlignment(dialog, Pos.CENTER);
        overlay.getChildren().add(dialog);
        staffDashboardRoot.getChildren().add(overlay);
    }

    private void loadTodayActivity() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Connection connection = DatabaseConnection.connect();
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

                    connection.close();

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
