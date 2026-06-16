package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.ActivityLog;
import utils.NavigationHelper;
import utils.NotificationHelper;
import utils.PhilTime;
import utils.TimerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;

public class DashboardController {

    @FXML
    private Button btnNotificationsAlert;

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
    private Button btnSidebarSignatures;

    @FXML
    private Button btnSidebarPasswordReset;

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
    private VBox vboxActivityTracker;

    @FXML
    private Button btnViewAllActivity;

    @FXML
    private Label lblCalendarTitle;

    @FXML
    private Label btnPrevMonth;

    @FXML
    private Label btnNextMonth;

    @FXML
    private GridPane gridCalendar;

    @FXML
    private StackPane dashboardRoot;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private Timeline autoRefreshTimeline;
    private String currentFilter = "All";

    @FXML
    private void initialize() {
        NavigationHelper.setActiveButton(btnSidebarDashboard);
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();

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

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

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

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarLogReturn,
                            "/fxml/Return.fxml"
                    )
            );

        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarUsers,
                            "/fxml/User.fxml"
                    )
            );

        if (btnSidebarSignatures != null) btnSidebarSignatures.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarSignatures, "/fxml/SignatureManager.fxml"));

        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarPasswordReset,
                            "/fxml/PasswordResetRequests.fxml"
                    )
            );

        NavigationHelper.hideAdminSidebarItems(
            btnSidebarEmployees,
            btnSidebarReports,
            btnSidebarUsers,
            btnSidebarPasswordReset
        );

        if (btnLogout != null)
            btnLogout.setOnAction(
                    event -> NavigationHelper.logout(btnLogout)
            );

        btnNotificationsAlert.setOnAction(
                event -> NotificationHelper.toggle(btnNotificationsAlert)
        );

        if (btnViewAllActivity != null) {
            btnViewAllActivity.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnViewAllActivity,
                            "/fxml/ActivityLog.fxml"
                    )
            );
        }

        cardCreatePassSlip.setOnMouseClicked(
                event -> NavigationHelper.navigateTo(
                        cardCreatePassSlip,
                        "/fxml/PassSlip.fxml"
                )
        );

        btnPrevMonth.setOnMouseClicked(event -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            loadCalendar();
        });

        btnNextMonth.setOnMouseClicked(event -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            loadCalendar();
        });

        gridCalendar.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                currentYearMonth = currentYearMonth.minusMonths(1);
            } else if (event.getDeltaY() < 0) {
                currentYearMonth = currentYearMonth.plusMonths(1);
            }
            loadCalendar();
            event.consume();
        });

        loadDashboardData();
        startAutoRefresh();
        startLiveTimerRefresh();
    }

    private void loadDashboardData() {
        loadSummaryCountsAsync();
        loadLiveTimer();
        if (selectedDate != null && currentFilter.equals("DateSelection")) {
             loadCalendarEvents(selectedDate);
        } else {
            loadActivities(currentFilter);
        }
        loadCalendar();
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            loadSummaryCountsAsync();
            loadLiveTimer();
            if (selectedDate != null && currentFilter.equals("DateSelection")) {
                loadCalendarEvents(selectedDate);
            } else {
                loadActivities(currentFilter);
            }
            // Update calendar labels without rebuilding the whole grid to preserve state
            gridCalendar.getChildren().forEach(node -> {
                if (node instanceof Label && GridPane.getRowIndex(node) > 0) {
                    Label label = (Label) node;
                    try {
                        int day = Integer.parseInt(label.getText());
                        updateDayLabelStyle(label, currentYearMonth.atDay(day));
                    } catch (Exception ignored) {}
                }
            });
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void loadActivities() {
        loadActivities("All");
    }

    private void loadActivities(String filter) {
        currentFilter = filter;
        if (!filter.equals("DateSelection")) {
            selectedDate = null;
        }
        
        // Use a limit when getting all logs to avoid loading thousands
        List<ActivityLog> allLogs = ReportsController.getLogs(filter.equals("All") || filter.equals("DateSelection") ? 100 : 0);
        List<ActivityLog> filteredLogs = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now(PhilTime.ZONE);
        for (ActivityLog log : allLogs) {
            boolean matches = false;
            if ("All".equalsIgnoreCase(filter)) {
                matches = true;
            } else if ("Today".equalsIgnoreCase(filter)) {
                matches = log.getTimestamp().toLocalDate().equals(now.toLocalDate());
            } else if ("This Week".equalsIgnoreCase(filter)) {
                matches = log.getTimestamp().isAfter(now.minusWeeks(1));
            } else if ("This Month".equalsIgnoreCase(filter)) {
                matches = log.getTimestamp().isAfter(now.minusMonths(1));
            } else if ("DateSelection".equalsIgnoreCase(filter)) {
                matches = selectedDate != null && log.getTimestamp().toLocalDate().equals(selectedDate);
            } else {
                // Filter by action type
                matches = log.getAction().toLowerCase().contains(filter.toLowerCase());
            }

            if (matches) {
                filteredLogs.add(log);
            }
        }

        Platform.runLater(() -> {
            vboxActivityTracker.getChildren().clear();
            
            // Add Filter UI
            HBox filterBox = new HBox(10);
            filterBox.setAlignment(Pos.CENTER_LEFT);
            filterBox.setStyle("-fx-padding: 0 0 10 14;");
            
            String[] filters = {"All", "Today", "This Week", "This Month"};
            for (String f : filters) {
                Label fl = new Label(f);
                boolean isActive = f.equals(currentFilter);
                fl.setStyle("-fx-text-fill: " + (isActive ? "#D4A853" : "#78716C") + "; -fx-cursor: hand; -fx-font-size: 11px;" + (isActive ? "-fx-font-weight: bold;" : ""));
                fl.setOnMouseClicked(e -> {
                    loadActivities(f);
                });
                filterBox.getChildren().add(fl);
            }
            vboxActivityTracker.getChildren().add(filterBox);

            if (filteredLogs.isEmpty()) {
                VBox emptyBox = new VBox();
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setStyle("-fx-padding: 40 0;");
                Label noAct = new Label("No activities found");
                noAct.setStyle("-fx-text-fill: #78716C; -fx-font-size: 13px;");
                emptyBox.getChildren().add(noAct);
                vboxActivityTracker.getChildren().add(emptyBox);
            } else {
                int count = 0;
                for (ActivityLog log : filteredLogs) {
                    if (count >= 20) break; 
                    
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-padding: 8px 14px; -fx-background-radius: 8px;");
                    row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 8px 14px; -fx-background-color: rgba(212,168,83,0.05); -fx-background-radius: 8px;"));
                    row.setOnMouseExited(e -> row.setStyle("-fx-padding: 8px 14px; -fx-background-radius: 8px;"));

                    Label dot = new Label("●");
                    dot.setStyle("-fx-text-fill: #D4A853; -fx-font-size: 10px;");

                    Label actionLabel = new Label(log.getAction());
                    actionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #D6CCC2; -fx-padding: 0 0 0 10px;");
                    
                    StackPane spacer = new StackPane();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label timeLabel = new Label(getRelativeTime(log.getTimestamp()));
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #A8A29E;");

                    row.getChildren().addAll(dot, actionLabel, spacer, timeLabel);
                    vboxActivityTracker.getChildren().add(row);
                    count++;
                }
            }
        });
    }

    private String getRelativeTime(LocalDateTime timestamp) {
        java.time.Duration duration = java.time.Duration.between(timestamp, LocalDateTime.now(PhilTime.ZONE));
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = duration.toMinutes();
        if (minutes < 60) return minutes + "m ago";
        long hours = duration.toHours();
        if (hours < 24) return hours + "hr ago";
        long days = duration.toDays();
        if (days == 1) return "Yesterday";
        return days + " days ago";
    }

    private void loadCalendar() {
        lblCalendarTitle.setText(currentYearMonth.getMonth().name() + " " + currentYearMonth.getYear());
        gridCalendar.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // SUN = 0
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            final int dayOfMonth = day;
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(32);
            dayLabel.setPrefHeight(28);
            
            updateDayLabelStyle(dayLabel, currentYearMonth.atDay(dayOfMonth));
            
            dayLabel.setOnMouseClicked(event -> {
                selectedDate = currentYearMonth.atDay(dayOfMonth);
                loadCalendar(); // Refresh styles to show selection
                loadCalendarEvents(selectedDate);
            });

            gridCalendar.add(dayLabel, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private void updateDayLabelStyle(Label label, LocalDate date) {
        String style = "-fx-font-size: 13px; -fx-text-fill: #FFFFFF; -fx-cursor: hand; -fx-background-radius: 15px;";
        
        if (date.getMonth() != currentYearMonth.getMonth()) {
            style = "-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.4); -fx-cursor: hand; -fx-background-radius: 15px;";
        }
        
        if (date.equals(LocalDate.now())) {
            style += "-fx-text-fill: #D4A853; -fx-font-weight: bold; -fx-border-color: rgba(212,168,83,0.5); -fx-border-radius: 15px;";
        }
        
        if (date.equals(selectedDate)) {
            style += "-fx-background-color: #D4A853; -fx-text-fill: #1C0A04; -fx-font-weight: bold;";
        }
        
        label.setStyle(style);
    }

    private void loadCalendarEvents(LocalDate date) {
        selectedDate = date;
        currentFilter = "DateSelection";
        loadActivities("DateSelection");
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
                            "SELECT COUNT(*) FROM pass_slips WHERE time_out::date = CURRENT_DATE AND status != 'REJECTED'"
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
