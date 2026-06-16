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
import models.PassSlip;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.TimerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReturnController {

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
    private Button btnNotificationsAlert;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnFetchActiveSlip;

    @FXML
    private Button btnConfirmReturnLog;

    @FXML
    private Label lblTotalCurrentlyOut;

    @FXML
    private Label lblTotalReturnedToday;

    @FXML
    private Label lblReturnStatusMessage;

    @FXML
    private TextField txtReturnSearchId;

    @FXML
    private TextField txtReturnEmployeeName;

    @FXML
    private TextField txtReturnTimeInStamp;

    @FXML
    private TableView<PassSlip> tblOutstandingOutboundView;

    @FXML
    private TableColumn<PassSlip, String> colReturnSlipId;

    @FXML
    private TableColumn<PassSlip, String> colReturnEmployeeID;

    @FXML
    private TableColumn<PassSlip, String> colReturnFullName;

    @FXML
    private TableColumn<PassSlip, String> colReturnDepartment;

    @FXML
    private TableColumn<PassSlip, String> colReturnTimeOut;

    @FXML
    private TableColumn<PassSlip, String> colReturnExpectedIn;

    @FXML
    private TextField txtMonitoringSearch;

    @FXML
    private Button btnRefreshMonitoring;

    @FXML
    private TableView<PassSlip> monitoringTableView;

    @FXML
    private TableColumn<PassSlip, String> colMonitorTimestamp;

    @FXML
    private TableColumn<PassSlip, String> colMonitorEmployeeID;

    @FXML
    private TableColumn<PassSlip, String> colMonitorFullName;

    @FXML
    private TableColumn<PassSlip, String> colMonitorDepartment;

    @FXML
    private TableColumn<PassSlip, String> colMonitorStatus;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private ObservableList<PassSlip> monitoringData;
    private ObservableList<PassSlip> outstandingSlipsData;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int activePassSlipId;
    private int activeEmployeeId;

    @FXML
    private void initialize() {

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnSidebarDashboard)
        );

        btnSidebarMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(btnSidebarMonitoring, "/fxml/Monitoring.fxml")
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        if (manageEmployeesSubMenu != null) {
            manageEmployeesSubMenu.setVisible(true);
            manageEmployeesSubMenu.setManaged(true);
        }

        btnSidebarEmployees.setOnAction(
                event -> NavigationHelper.navigateTo(btnSidebarEmployees, "/fxml/EmployeeController.fxml")
        );

        btnSidebarReports.setOnAction(
                event -> NavigationHelper.navigateTo(btnSidebarReports, "/fxml/Reports.fxml")
        );

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(
                    event -> NavigationHelper.navigateTo(btnSidebarLogReturn, "/fxml/Return.fxml")
            );

        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(
                    event -> NavigationHelper.navigateTo(btnSidebarUsers, "/fxml/User.fxml")
            );

        if (btnSidebarSignatures != null) btnSidebarSignatures.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarSignatures, "/fxml/SignatureManager.fxml"));

        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(
                    event -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml")
            );

        NavigationHelper.hideAdminSidebarItems(
            btnSidebarEmployees,
            btnSidebarReports,
            btnSidebarUsers,
            btnSidebarPasswordReset
        );

        NavigationHelper.setActiveButton(btnSidebarLogReturn);

        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));
        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> utils.NotificationHelper.toggle(btnNotificationsAlert));

        btnFetchActiveSlip.setOnAction(event -> fetchActiveSlip());

        btnConfirmReturnLog.setOnAction(event -> processReturn());

        txtReturnSearchId.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isBlank()) {
                        activePassSlipId = 0;
                        activeEmployeeId = 0;
                        txtReturnEmployeeName.clear();
                        lblReturnStatusMessage.setText("");
                        tblOutstandingOutboundView.setItems(outstandingSlipsData);
                        return;
                    }
                    try {
                        int searchValue = Integer.parseInt(newValue.trim());
                        filterOutstandingSlips(String.valueOf(searchValue));
                        fetchActiveSlip();
                    } catch (NumberFormatException ignored) {
                    }
                }
        );

        btnRefreshMonitoring.setOnAction(event -> loadMonitoringDataAsync());

        txtMonitoringSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> filterMonitoringData(newValue)
        );

        setupMonitoringTable();
        setupOutstandingTable();
        loadSummaryAsync();
        loadMonitoringDataAsync();
        loadOutstandingSlipsAsync();

    }

    public static boolean recordReturn(int passSlipId) {

        try {

            Connection connection = DatabaseConnection.connect();

            String getQuery = """
                    SELECT time_out, employee_id
                    FROM pass_slips
                    WHERE id = ?
                    """;

            PreparedStatement getStatement =
                    connection.prepareStatement(getQuery);

            getStatement.setInt(1, passSlipId);

            ResultSet resultSet = getStatement.executeQuery();

            if (resultSet.next()) {

                LocalDateTime timeOut =
                        resultSet.getTimestamp("time_out").toLocalDateTime();

                int employeeId = resultSet.getInt("employee_id");

                LocalDateTime timeIn = LocalDateTime.now(PhilTime.ZONE);

                Duration duration = Duration.between(timeOut, timeIn);
                long totalMinutes = duration.toMinutes();
                long hours = duration.toHours();
                long minutes = totalMinutes % 60;

                String durationText = hours + " hrs " + minutes + " mins";

                String updateQuery = """
                        UPDATE pass_slips
                        SET time_in = ?, duration = ?, duration_minutes = ?, status = 'RETURNED'
                        WHERE id = ?
                        """;

                PreparedStatement updateStatement =
                        connection.prepareStatement(updateQuery);

                updateStatement.setTimestamp(1, java.sql.Timestamp.valueOf(timeIn));
                updateStatement.setString(2, durationText);
                updateStatement.setLong(3, totalMinutes);
                updateStatement.setInt(4, passSlipId);

                int updated = updateStatement.executeUpdate();

                if (updated > 0) {
                    TimerService.markReturned(employeeId);

                    ActivityLogController.logActivity(
                            "Returned Pass Slip ID: " + passSlipId,
                            employeeId
                    );
                }

                return updated > 0;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    private void fetchActiveSlip() {

        try {

            int searchValue =
                    Integer.parseInt(txtReturnSearchId.getText().trim());

            Connection connection =
                    DatabaseConnection.connect();

            if (connection == null) {
                lblReturnStatusMessage.setText("Database unavailable.");
                return;
            }

            PreparedStatement statement = connection.prepareStatement("""
                    SELECT p.id, p.employee_id, e.first_name, e.last_name
                    FROM pass_slips p
                    JOIN employees e ON p.employee_id = e.id
                    WHERE p.status = 'OUT'
                      AND (p.id = ? OR e.id = ?)
                    ORDER BY p.id DESC
                    LIMIT 1
                    """);

            statement.setInt(1, searchValue);
            statement.setInt(2, searchValue);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                activePassSlipId = resultSet.getInt("id");
                activeEmployeeId = resultSet.getInt("employee_id");

                txtReturnEmployeeName.setText(
                        resultSet.getString("first_name")
                                + " "
                                + resultSet.getString("last_name")
                );

                lblReturnStatusMessage.setText(
                        "Active pass slip found — ID #" + activePassSlipId
                );

            } else {
                activePassSlipId = 0;
                activeEmployeeId = 0;
                txtReturnEmployeeName.clear();
                lblReturnStatusMessage.setText("No active pass slip found.");
            }

        } catch (Exception e) {
            activePassSlipId = 0;
            activeEmployeeId = 0;
            lblReturnStatusMessage.setText("Enter a valid slip or employee ID.");
        }

    }

    private void processReturn() {

        if (activePassSlipId <= 0) {
            lblReturnStatusMessage.setText("Find an active pass slip first.");
            return;
        }

        boolean success = recordReturn(activePassSlipId);

        if (success) {
            lblReturnStatusMessage.setText("Return recorded successfully.");
            txtReturnSearchId.clear();
            txtReturnEmployeeName.clear();
            txtReturnTimeInStamp.clear();
            activePassSlipId = 0;
            activeEmployeeId = 0;
            loadSummaryAsync();
            loadOutstandingSlipsAsync();
        } else {
            lblReturnStatusMessage.setText("Unable to record return.");
        }

    }

    private void setupMonitoringTable() {
        colMonitorTimestamp.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeOut = ps.getTimeOut() != null ? ps.getTimeOut().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeOut);
                }
        );

        colMonitorEmployeeID.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getEmployeeId()))
        );

        colMonitorFullName.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmployeeName())
        );

        colMonitorDepartment.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDepartment() != null ? cellData.getValue().getDepartment() : ""
                )
        );

        colMonitorStatus.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus())
        );

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

    private void setupOutstandingTable() {
        colReturnSlipId.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getId()))
        );

        colReturnEmployeeID.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getEmployeeId()))
        );

        colReturnFullName.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmployeeName())
        );

        colReturnDepartment.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDepartment() != null ? cellData.getValue().getDepartment() : ""
                )
        );

        colReturnTimeOut.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeOut = ps.getTimeOut() != null ? ps.getTimeOut().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeOut);
                }
        );

        colReturnExpectedIn.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper("N/A")
        );

        tblOutstandingOutboundView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<PassSlip> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    PassSlip selected = row.getItem();
                    txtReturnSearchId.setText(String.valueOf(selected.getId()));
                }
            });
            return row;
        });
    }

    private void loadOutstandingSlipsAsync() {
        Task<ObservableList<PassSlip>> task = new Task<>() {
            @Override
            protected ObservableList<PassSlip> call() {
                return fetchOutstandingSlips();
            }
        };

        task.setOnSucceeded(e -> {
            outstandingSlipsData = task.getValue();
            tblOutstandingOutboundView.setItems(outstandingSlipsData);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<PassSlip> fetchOutstandingSlips() {
        ObservableList<PassSlip> data = FXCollections.observableArrayList();

        try {
            Connection connection = DatabaseConnection.connect();

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
                    WHERE ps.status = 'OUT'
                    ORDER BY ps.id DESC
                    """;

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime timeOut = resultSet.getTimestamp("time_out") != null
                        ? resultSet.getTimestamp("time_out").toLocalDateTime() : null;

                LocalDateTime timeIn = resultSet.getTimestamp("time_in") != null
                        ? resultSet.getTimestamp("time_in").toLocalDateTime() : null;

                PassSlip passSlip = new PassSlip(
                        resultSet.getInt("id"),
                        resultSet.getInt("employee_id"),
                        resultSet.getString("first_name") + " " + resultSet.getString("last_name"),
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

    private void filterOutstandingSlips(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            tblOutstandingOutboundView.setItems(outstandingSlipsData);
            return;
        }

        ObservableList<PassSlip> filtered = FXCollections.observableArrayList();

        for (PassSlip ps : outstandingSlipsData) {
            if (String.valueOf(ps.getId()).contains(keyword)
                    || String.valueOf(ps.getEmployeeId()).contains(keyword)
                    || ps.getEmployeeName().toLowerCase().contains(keyword.toLowerCase())
                    || (ps.getDepartment() != null && ps.getDepartment().toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(ps);
            }
        }

        tblOutstandingOutboundView.setItems(filtered);
    }

    private void updatePassSlipStatus(PassSlip passSlip, String newStatus) {
        if (passSlip == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.connect()) {
                    String sql = "UPDATE pass_slips SET status = ? WHERE id = ?";
                    if (newStatus.equals("RETURNED")) {
                        sql = "UPDATE pass_slips SET status = ?, time_in = ? WHERE id = ?";
                    }
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, newStatus);
                        if (newStatus.equals("RETURNED")) {
                            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
                            pstmt.setInt(3, passSlip.getId());
                        } else {
                            pstmt.setInt(2, passSlip.getId());
                        }
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
                MonitoringController.updateExpiredPassSlips();
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
            Connection connection = DatabaseConnection.connect();

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

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime timeOut = resultSet.getTimestamp("time_out") != null
                        ? resultSet.getTimestamp("time_out").toLocalDateTime() : null;

                LocalDateTime timeIn = resultSet.getTimestamp("time_in") != null
                        ? resultSet.getTimestamp("time_in").toLocalDateTime() : null;

                PassSlip passSlip = new PassSlip(
                        resultSet.getInt("id"),
                        resultSet.getInt("employee_id"),
                        resultSet.getString("first_name") + " " + resultSet.getString("last_name"),
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

        ObservableList<PassSlip> filtered = FXCollections.observableArrayList();

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

    private void loadSummaryAsync() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {

                    Connection connection =
                            DatabaseConnection.connect();

                    if (connection == null) {
                        return null;
                    }

                    PreparedStatement outStatement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM pass_slips WHERE status = 'OUT'"
                    );

                    ResultSet outResult = outStatement.executeQuery();

                    int outCount = 0;
                    if (outResult.next()) {
                        outCount = outResult.getInt(1);
                    }

                    PreparedStatement returnedStatement = connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE status = 'RETURNED'
                              AND DATE(time_in) = CURRENT_DATE
                            """
                    );

                    ResultSet returnedResult = returnedStatement.executeQuery();

                    int retCount = 0;
                    if (returnedResult.next()) {
                        retCount = returnedResult.getInt(1);
                    }

                    int finalOutCount = outCount;
                    int finalRetCount = retCount;
                    Platform.runLater(() -> {
                        lblTotalCurrentlyOut.setText(String.valueOf(finalOutCount));
                        lblTotalReturnedToday.setText(String.valueOf(finalRetCount));
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblTotalCurrentlyOut.setText("0");
                        lblTotalReturnedToday.setText("0");
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
