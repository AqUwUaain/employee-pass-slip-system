package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.ActivityLog;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.SidebarHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MonitoringController {

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
    private Button btnThemeToggle;

    @FXML
    private Button btnRefreshMonitoringFeed;

    @FXML
    private Label lblTotalLogs;

    @FXML
    private Label lblTodayEvents;

    @FXML
    private Label lblUniqueUsers;

    @FXML
    private TextField txtSearchMonitoringLogs;

    @FXML
    private TableView<ActivityLog> monitoringTableView;

    @FXML
    private TableColumn<ActivityLog, String> colTimestamp;

    @FXML
    private TableColumn<ActivityLog, String> colUsername;

    @FXML
    private TableColumn<ActivityLog, String> colAction;

    @FXML
    private TableColumn<ActivityLog, String> colDescription;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private ObservableList<ActivityLog> monitoringData;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, null,
                btnSidebarMonitoring, btnThemeToggle
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        btnRefreshMonitoringFeed.setOnAction(event -> {
            loadLogsAsync();
            loadSummaryAsync();
        });

        setupTableColumns();

        txtSearchMonitoringLogs.textProperty().addListener(
                (observable, oldValue, newValue) -> filterMonitoringData(newValue)
        );

        loadLogsAsync();
        loadSummaryAsync();

    }

    private void setupTableColumns() {

        colTimestamp.setCellValueFactory(
                cellData -> {
                    ActivityLog log = cellData.getValue();
                    String ts = log.getTimestamp() != null
                            ? log.getTimestamp().format(formatter) : "";
                    return new ReadOnlyStringWrapper(ts);
                }
        );

        colUsername.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getUsername() != null
                                ? cellData.getValue().getUsername() : ""
                )
        );

        colAction.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getAction() != null
                                ? cellData.getValue().getAction() : ""
                )
        );

        colDescription.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDescription() != null
                                ? cellData.getValue().getDescription() : ""
                )
        );

    }

    private void loadLogsAsync() {

        Task<ObservableList<ActivityLog>> task = new Task<>() {
            @Override
            protected ObservableList<ActivityLog> call() {
                return fetchLogsData();
            }
        };

        task.setOnSucceeded(e -> {
            monitoringData = task.getValue();
            monitoringTableView.setItems(monitoringData);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    private ObservableList<ActivityLog> fetchLogsData() {

        ObservableList<ActivityLog> data = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {

            String query = """
                    SELECT id, action, description, user_id, username, timestamp, employee_id
                    FROM activity_logs
                    ORDER BY timestamp DESC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                LocalDateTime timestamp =
                        resultSet.getTimestamp("timestamp") != null
                                ? resultSet.getTimestamp("timestamp").toLocalDateTime()
                                : null;

                ActivityLog log = new ActivityLog(
                        resultSet.getInt("id"),
                        resultSet.getString("action"),
                        resultSet.getString("description"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        timestamp,
                        resultSet.getInt("employee_id")
                );

                data.add(log);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;

    }

    private void filterMonitoringData(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            monitoringTableView.setItems(monitoringData);
            return;
        }

        ObservableList<ActivityLog> filtered =
                FXCollections.observableArrayList();

        for (ActivityLog log : monitoringData) {
            if ((log.getUsername() != null && log.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                    || (log.getAction() != null && log.getAction().toLowerCase().contains(keyword.toLowerCase()))
                    || (log.getDescription() != null && log.getDescription().toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(log);
            }
        }

        monitoringTableView.setItems(filtered);

    }

    private void loadSummaryAsync() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try (Connection connection = DatabaseConnection.connect()) {

                    if (connection == null) {
                        Platform.runLater(() -> {
                            lblTotalLogs.setText("0");
                            lblTodayEvents.setText("0");
                            lblUniqueUsers.setText("0");
                        });
                        return null;
                    }

                    PreparedStatement totalStmt = connection.prepareStatement(
                            "SELECT COUNT(*) FROM activity_logs"
                    );
                    ResultSet totalResult = totalStmt.executeQuery();
                    int totalLogs = totalResult.next() ? totalResult.getInt(1) : 0;

                    PreparedStatement todayStmt = connection.prepareStatement(
                            "SELECT COUNT(*) FROM activity_logs WHERE DATE(timestamp) = CURRENT_DATE"
                    );
                    ResultSet todayResult = todayStmt.executeQuery();
                    int todayEvents = todayResult.next() ? todayResult.getInt(1) : 0;

                    PreparedStatement usersStmt = connection.prepareStatement(
                            "SELECT COUNT(DISTINCT user_id) FROM activity_logs"
                    );
                    ResultSet usersResult = usersStmt.executeQuery();
                    int uniqueUsers = usersResult.next() ? usersResult.getInt(1) : 0;

                    int fTotal = totalLogs;
                    int fToday = todayEvents;
                    int fUsers = uniqueUsers;
                    Platform.runLater(() -> {
                        lblTotalLogs.setText(String.valueOf(fTotal));
                        lblTodayEvents.setText(String.valueOf(fToday));
                        lblUniqueUsers.setText(String.valueOf(fUsers));
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblTotalLogs.setText("0");
                        lblTodayEvents.setText("0");
                        lblUniqueUsers.setText("0");
                    });
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public static void updateExpiredPassSlips() {

        try (Connection connection = DatabaseConnection.connect()) {

            String query = """
                    SELECT id, time_out, estimated_return
                    FROM pass_slips
                    WHERE status = 'OUT'
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                int passSlipId = resultSet.getInt("id");

                Timestamp timeOutTimestamp =
                        resultSet.getTimestamp("time_out");

                LocalDateTime timeOut =
                        timeOutTimestamp.toLocalDateTime();

                LocalDateTime now = LocalDateTime.now(PhilTime.ZONE);

                LocalDateTime cutoff = timeOut.toLocalDate().atTime(21, 0);

                if (now.isAfter(cutoff)) {
                    String updateQuery = """
                            UPDATE pass_slips
                            SET status = 'OVERDUE'
                            WHERE id = ?
                            """;
                    PreparedStatement updateStatement =
                            connection.prepareStatement(updateQuery);
                    updateStatement.setInt(1, passSlipId);
                    updateStatement.executeUpdate();
                    continue;
                }

                Timestamp estimatedReturnTimestamp =
                        resultSet.getTimestamp("estimated_return");

                boolean shouldExpire;
                if (estimatedReturnTimestamp != null) {
                    LocalDateTime estimatedReturn =
                            estimatedReturnTimestamp.toLocalDateTime();
                    shouldExpire = now.isAfter(estimatedReturn.plusHours(1));
                } else {
                    long hours =
                            Duration.between(timeOut, now).toHours();
                    shouldExpire = hours >= 2;
                }

                if (shouldExpire) {

                    String updateQuery = """
                            UPDATE pass_slips
                            SET status = 'OVERDUE'
                            WHERE id = ?
                            """;

                    PreparedStatement updateStatement =
                            connection.prepareStatement(updateQuery);

                    updateStatement.setInt(1, passSlipId);

                    updateStatement.executeUpdate();

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
