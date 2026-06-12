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
import models.PassSlip;
import utils.NavigationHelper;

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
    private Button btnRefreshMonitoringFeed;

    @FXML
    private Label lblScannerStatus;

    @FXML
    private Label lblTotalInCampusCount;

    @FXML
    private Label lblActiveAlertsCount;

    @FXML
    private TextField txtSearchMonitoringLogs;

    @FXML
    private TableView<PassSlip> monitoringTableView;

    @FXML
    private TableColumn<PassSlip, String> colTimestamp;

    @FXML
    private TableColumn<PassSlip, String> colEmployeeID;

    @FXML
    private TableColumn<PassSlip, String> colFullName;

    @FXML
    private TableColumn<PassSlip, String> colDepartment;

    @FXML
    private TableColumn<PassSlip, String> colAccessType;

    private ObservableList<PassSlip> monitoringData;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        btnRefreshMonitoringFeed.setOnAction(event -> loadMonitoringDataAsync());

        setupTableColumns();

        txtSearchMonitoringLogs.textProperty().addListener(
                (observable, oldValue, newValue) -> filterMonitoringData(newValue)
        );

        loadMonitoringDataAsync();
        loadSummaryAsync();

    }

    private void setupTableColumns() {

        colTimestamp.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeOut = ps.getTimeOut() != null
                            ? ps.getTimeOut().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeOut);
                }
        );

        colEmployeeID.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        String.valueOf(cellData.getValue().getEmployeeId())
                )
        );

        colFullName.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getEmployeeName()
                )
        );

        colDepartment.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDepartment() != null
                                ? cellData.getValue().getDepartment() : ""
                )
        );

        colAccessType.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getStatus()
                )
        );

        // Add context menu for status management
        monitoringTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<PassSlip> row = new javafx.scene.control.TableRow<>();
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

            javafx.scene.control.MenuItem approveItem = new javafx.scene.control.MenuItem("Approve (Set OUT)");
            approveItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "OUT"));

            javafx.scene.control.MenuItem rejectItem = new javafx.scene.control.MenuItem("Reject");
            rejectItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "REJECTED"));

            javafx.scene.control.MenuItem returnItem = new javafx.scene.control.MenuItem("Mark as RETURNED");
            returnItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "RETURNED"));

            contextMenu.getItems().addAll(approveItem, rejectItem, returnItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((javafx.scene.control.ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

    }

    private void updatePassSlipStatus(PassSlip passSlip, String newStatus) {
        if (passSlip == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = database.DatabaseConnection.connect()) {
                    String sql = "UPDATE pass_slips SET status = ? WHERE id = ?";
                    if (newStatus.equals("RETURNED")) {
                        sql = "UPDATE pass_slips SET status = ?, time_in = CURRENT_TIMESTAMP WHERE id = ?";
                    }
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, newStatus);
                        pstmt.setInt(2, passSlip.getId());
                        pstmt.executeUpdate();
                    }
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            loadMonitoringDataAsync();
            loadSummaryAsync();
            utils.ActivityLogger.log("Pass Slip Updated", "Status changed to " + newStatus + " for Pass Slip #" + passSlip.getId(), passSlip.getEmployeeId());
        });

        new Thread(task).start();
    }

    private void loadMonitoringDataAsync() {

        Task<ObservableList<PassSlip>> task = new Task<>() {
            @Override
            protected ObservableList<PassSlip> call() {
                updateExpiredPassSlips();
                return fetchMonitoringData();
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

    private ObservableList<PassSlip> fetchMonitoringData() {

        ObservableList<PassSlip> data = FXCollections.observableArrayList();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT
                        ps.id,
                        ps.employee_id,
                        e.first_name,
                        e.last_name,
                        e.department,
                        ps.reason,
                        ps.time_out,
                        ps.time_in,
                        ps.duration,
                        ps.duration_minutes,
                        ps.status
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    ORDER BY ps.id DESC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                LocalDateTime timeOut =
                        resultSet.getTimestamp("time_out") != null
                                ? resultSet.getTimestamp("time_out").toLocalDateTime()
                                : null;

                LocalDateTime timeIn =
                        resultSet.getTimestamp("time_in") != null
                                ? resultSet.getTimestamp("time_in").toLocalDateTime()
                                : null;

                PassSlip passSlip = new PassSlip(
                        resultSet.getInt("id"),
                        resultSet.getInt("employee_id"),
                        resultSet.getString("first_name")
                                + " " + resultSet.getString("last_name"),
                        resultSet.getString("department"),
                        resultSet.getString("reason"),
                        timeOut,
                        timeIn,
                        resultSet.getLong("duration_minutes"),
                        resultSet.getString("status")
                );

                data.add(passSlip);

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

        ObservableList<PassSlip> filtered =
                FXCollections.observableArrayList();

        for (PassSlip ps : monitoringData) {
            if (ps.getEmployeeName().toLowerCase().contains(keyword.toLowerCase())
                    || ps.getStatus().toLowerCase().contains(keyword.toLowerCase())
                    || String.valueOf(ps.getEmployeeId()).contains(keyword)
                    || (ps.getDepartment() != null
                        && ps.getDepartment().toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(ps);
            }
        }

        monitoringTableView.setItems(filtered);

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

            while (resultSet.next()) {

                int passSlipId = resultSet.getInt("id");

                Timestamp timeOutTimestamp =
                        resultSet.getTimestamp("time_out");

                LocalDateTime timeOut =
                        timeOutTimestamp.toLocalDateTime();

                LocalDateTime now = LocalDateTime.now();

                long hours =
                        Duration.between(timeOut, now).toHours();

                if (hours >= 1) {

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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadSummaryAsync() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {

                    Connection connection =
                            DatabaseConnection.connect();

                    if (connection == null) {
                        Platform.runLater(() -> {
                            lblScannerStatus.setText("OFFLINE");
                            lblTotalInCampusCount.setText("0");
                            lblActiveAlertsCount.setText("0");
                        });
                        return null;
                    }

                    Platform.runLater(() -> lblScannerStatus.setText("ONLINE"));

                    PreparedStatement outStatement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM pass_slips WHERE status = 'OUT'"
                    );

                    ResultSet outResult = outStatement.executeQuery();

                    int outCount = 0;
                    if (outResult.next()) {
                        outCount = outResult.getInt(1);
                    }

                    int finalOutCount = outCount;
                    Platform.runLater(() -> lblTotalInCampusCount.setText(
                            String.valueOf(finalOutCount)
                    ));

                    PreparedStatement expiredStatement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM pass_slips WHERE status = 'EXPIRED'"
                    );

                    ResultSet expiredResult = expiredStatement.executeQuery();

                    int expiredCount = 0;
                    if (expiredResult.next()) {
                        expiredCount = expiredResult.getInt(1);
                    }

                    int finalExpiredCount = expiredCount;
                    Platform.runLater(() -> lblActiveAlertsCount.setText(
                            String.valueOf(finalExpiredCount)
                    ));

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblScannerStatus.setText("OFFLINE");
                        lblTotalInCampusCount.setText("0");
                        lblActiveAlertsCount.setText("0");
                    });
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

}
