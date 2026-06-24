package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import models.PassSlip;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.Session;
import utils.SidebarHelper;
import utils.TimerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReturnController {

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
    private Button btnNotificationsAlert;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnThemeToggle;

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
    private TextField txtMonitoringSearch;

    @FXML
    private Button btnRefreshMonitoring;

    @FXML
    private ComboBox<String> cmbStatusFilter;

    @FXML
    private TableView<PassSlip> monitoringTableView;

    @FXML
    private TableColumn<PassSlip, String> colMonitorTimestamp;

    @FXML
    private TableColumn<PassSlip, String> colMonitorActualTimeOut;

    @FXML
    private TableColumn<PassSlip, String> colMonitorEmployeeID;

    @FXML
    private TableColumn<PassSlip, String> colMonitorFullName;

    @FXML
    private TableColumn<PassSlip, String> colMonitorDepartment;

    @FXML
    private TableColumn<PassSlip, String> colMonitorReason;

    @FXML
    private TableColumn<PassSlip, String> colMonitorExpectedReturn;

    @FXML
    private TableColumn<PassSlip, String> colMonitorTimeIn;

    @FXML
    private TableColumn<PassSlip, String> colMonitorStatus;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private ObservableList<PassSlip> monitoringData;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    private int activePassSlipId;
    private int activeEmployeeId;

    @FXML
    private void initialize() {

        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, btnNotificationsAlert,
                btnSidebarLogReturn, btnThemeToggle,
                btnManageEmployees, manageEmployeesSubMenu
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        btnConfirmReturnLog.setOnAction(event -> processReturn());

        txtReturnSearchId.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isBlank()) {
                        activePassSlipId = 0;
                        activeEmployeeId = 0;
                        txtReturnEmployeeName.clear();
                        lblReturnStatusMessage.setText("");
                        return;
                    }
                    try {
                        int searchValue = Integer.parseInt(newValue.trim());
                        fetchActiveSlip();
                    } catch (NumberFormatException ignored) {
                    }
                }
        );

        btnRefreshMonitoring.setOnAction(event -> loadMonitoringDataAsync());

        cmbStatusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "OUT", "RETURNED", "RETURNED EARLY", "LATE", "NO RE-ENTRY", "EXPIRED", "PENDING", "REJECTED", "CANCELLED"
        ));
        cmbStatusFilter.setPromptText("All Statuses");
        cmbStatusFilter.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> filterMonitoringData(txtMonitoringSearch.getText())
        );

        txtMonitoringSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> filterMonitoringData(newValue)
        );

        setupMonitoringTable();
        loadSummaryAsync();
        loadMonitoringDataAsync();

    }

    public static boolean recordReturn(int passSlipId, LocalDateTime customTimeIn) {

        try (Connection connection = DatabaseConnection.connect()) {

            String getQuery = """
                    SELECT time_out, actual_time_out, estimated_return, employee_id
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

                LocalDateTime actualTimeOut = resultSet.getTimestamp("actual_time_out") != null
                        ? resultSet.getTimestamp("actual_time_out").toLocalDateTime() : null;

                int employeeId = resultSet.getInt("employee_id");

                LocalDateTime estimatedReturn = null;
                if (resultSet.getTimestamp("estimated_return") != null) {
                    estimatedReturn = resultSet.getTimestamp("estimated_return").toLocalDateTime();
                }

                LocalDateTime timeIn = customTimeIn != null ? customTimeIn : LocalDateTime.now(PhilTime.ZONE);

                LocalDateTime durationStart = actualTimeOut != null ? actualTimeOut : timeOut;
                Duration duration = Duration.between(durationStart, timeIn);
                long totalMinutes = duration.toMinutes();
                long hours = duration.toHours();
                long minutes = totalMinutes % 60;

                String durationText = hours + " hrs " + minutes + " mins";

                String status = "RETURNED";
                if (estimatedReturn != null) {
                    if (timeIn.isBefore(estimatedReturn)) {
                        status = "RETURNED EARLY";
                    } else if (timeIn.isAfter(estimatedReturn)) {
                        status = "LATE";
                    }
                }

                String updateQuery = """
                        UPDATE pass_slips
                        SET time_in = ?, duration = ?, duration_minutes = ?, status = ?
                        WHERE id = ?
                        """;

                PreparedStatement updateStatement =
                        connection.prepareStatement(updateQuery);

                updateStatement.setTimestamp(1, java.sql.Timestamp.valueOf(timeIn));
                updateStatement.setString(2, durationText);
                updateStatement.setLong(3, totalMinutes);
                updateStatement.setString(4, status);
                updateStatement.setInt(5, passSlipId);

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

            try (Connection connection = DatabaseConnection.connect()) {

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

        LocalDateTime customTimeIn = null;
        String timeInText = txtReturnTimeInStamp.getText().trim();
        if (!timeInText.isEmpty()) {
            try {
                DateTimeFormatter parser = DateTimeFormatter.ofPattern("hh:mm a");
                LocalTime parsedTime = LocalTime.parse(timeInText.toUpperCase(), parser);
                customTimeIn = LocalDateTime.now(PhilTime.ZONE).withHour(parsedTime.getHour()).withMinute(parsedTime.getMinute()).withSecond(0).withNano(0);
            } catch (Exception e) {
                lblReturnStatusMessage.setText("Invalid time format. Use HH:MM AM/PM.");
                return;
            }
        }

        String statusBefore = getPassSlipStatus(activePassSlipId);
        boolean success = recordReturn(activePassSlipId, customTimeIn);

        if (success) {
            String statusAfter = getPassSlipStatus(activePassSlipId);
            String msg = "Return recorded successfully.";
            if (statusAfter != null) {
                msg = "Return recorded — " + statusAfter + ".";
            }
            lblReturnStatusMessage.setText(msg);
            lblReturnStatusMessage.setStyle("-fx-text-fill: #34D399; -fx-font-size: 13px; -fx-font-weight: bold;");
            txtReturnSearchId.clear();
            txtReturnEmployeeName.clear();
            txtReturnTimeInStamp.clear();
            activePassSlipId = 0;
            activeEmployeeId = 0;
            loadSummaryAsync();
            loadMonitoringDataAsync();
        } else {
            lblReturnStatusMessage.setText("Unable to record return.");
            lblReturnStatusMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px;");
        }

    }

    private String getPassSlipStatus(int passSlipId) {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement ps = connection.prepareStatement("SELECT status FROM pass_slips WHERE id = ?");
            ps.setInt(1, passSlipId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (Exception ignored) {}
        return null;
    }

    private void setupMonitoringTable() {
        colMonitorTimestamp.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeOut = ps.getTimeOut() != null ? ps.getTimeOut().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeOut);
                }
        );

        colMonitorActualTimeOut.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String actualTimeOut = ps.getActualTimeOut() != null ? ps.getActualTimeOut().format(formatter) : "";
                    return new ReadOnlyStringWrapper(actualTimeOut);
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

        colMonitorReason.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""
                )
        );

        colMonitorExpectedReturn.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String expected = ps.getEstimatedReturn() != null ? ps.getEstimatedReturn().format(formatter) : "";
                    return new ReadOnlyStringWrapper(expected);
                }
        );

        colMonitorTimeIn.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeIn = ps.getTimeIn() != null ? ps.getTimeIn().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeIn);
                }
        );

        colMonitorStatus.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus())
        );

        colMonitorStatus.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "OUT" -> "#FBBF24";
                        case "RETURNED" -> "#34D399";
                        case "RETURNED EARLY" -> "#60A5FA";
                        case "LATE" -> "#F97316";
                        case "NO RE-ENTRY" -> "#EF4444";
                        case "EXPIRED" -> "#DC2626";
                        case "CANCELLED" -> "#9CA3AF";
                        default -> "#A8A29E";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        monitoringTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<PassSlip> row = new javafx.scene.control.TableRow<>();
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

            javafx.scene.control.MenuItem approveItem = new javafx.scene.control.MenuItem("Approve (Set OUT)");
            approveItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "OUT"));

            javafx.scene.control.MenuItem rejectItem = new javafx.scene.control.MenuItem("Reject");
            rejectItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "REJECTED"));

            javafx.scene.control.MenuItem returnItem = new javafx.scene.control.MenuItem("Mark as RETURNED");
            returnItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "RETURNED"));

            javafx.scene.control.MenuItem cancelItem = new javafx.scene.control.MenuItem("Cancelled");
            cancelItem.setOnAction(event -> updatePassSlipStatus(row.getItem(), "CANCELLED"));

            javafx.scene.control.MenuItem viewSigItem = new javafx.scene.control.MenuItem("View Requester Signature");
            viewSigItem.setOnAction(event -> showRequesterSignature(row.getItem()));

            javafx.scene.control.MenuItem reprintItem = new javafx.scene.control.MenuItem("Reprint Slip");
            reprintItem.setOnAction(event -> {
                PassSlip ps = row.getItem();
                if (ps != null) {
                    PassSlipController.reprintSlip(ps, row.getScene().getWindow());
                }
            });

            contextMenu.getItems().addAll(approveItem, rejectItem, returnItem, cancelItem, viewSigItem, reprintItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((javafx.scene.control.ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && newItem.getStatus() != null) {
                    String status = newItem.getStatus();
                    String color;
                    switch (status) {
                        case "OUT" -> color = "#FBBF24";
                        case "RETURNED" -> color = "#34D399";
                        case "RETURNED EARLY" -> color = "#60A5FA";
                        case "LATE" -> color = "#F97316";
                        case "NO RE-ENTRY" -> color = "#EF4444";
                        case "EXPIRED" -> color = "#DC2626";
                        case "PENDING" -> color = "#D97706";
                        case "REJECTED" -> color = "#EF4444";
                        case "CANCELLED" -> color = "#9CA3AF";
                        default -> color = "#A8A29E";
                    }
                    row.setStyle("-fx-text-fill: " + color + ";");

                    boolean isStaff = "STAFF".equalsIgnoreCase(Session.currentRole);
                    approveItem.setDisable(isStaff);
                    rejectItem.setDisable(isStaff);
                    cancelItem.setDisable(isStaff);
                    returnItem.setDisable(false);
                } else {
                    row.setStyle("");
                }
            });

            return row;
        });
    }

    private void showRequesterSignature(PassSlip passSlip) {
        if (passSlip == null || passSlip.getRequesterSignature() == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("No Signature");
            alert.setHeaderText(null);
            alert.setContentText("This request does not include a requester signature.");
            alert.showAndWait();
            return;
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Requester Signature");
        alert.setHeaderText("Signature for Pass Slip #" + passSlip.getId());

        ByteArrayInputStream bis = new ByteArrayInputStream(passSlip.getRequesterSignature());
        javafx.scene.image.Image fxImage = new javafx.scene.image.Image(bis);
        ImageView imageView = new ImageView(fxImage);
        imageView.setFitWidth(300);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        alert.setGraphic(imageView);
        alert.setContentText("Requested by: " + passSlip.getEmployeeName());
        alert.showAndWait();
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

        try (Connection connection = DatabaseConnection.connect()) {

            String query = """
                    SELECT
                        ps.id,
                        ps.employee_id,
                        e.first_name,
                        e.last_name,
                        e.department,
                        ps.reason,
                        ps.time_out,
                        ps.actual_time_out,
                        ps.time_in,
                        ps.estimated_return,
                        ps.duration,
                        ps.duration_minutes,
                        ps.status,
                        ps.requester_signature
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    ORDER BY ps.id DESC
                    """;

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime timeOut = resultSet.getTimestamp("time_out") != null
                        ? resultSet.getTimestamp("time_out").toLocalDateTime() : null;

                LocalDateTime actualTimeOut = resultSet.getTimestamp("actual_time_out") != null
                        ? resultSet.getTimestamp("actual_time_out").toLocalDateTime() : null;

                LocalDateTime timeIn = resultSet.getTimestamp("time_in") != null
                        ? resultSet.getTimestamp("time_in").toLocalDateTime() : null;

                LocalDateTime estimatedReturn = resultSet.getTimestamp("estimated_return") != null
                        ? resultSet.getTimestamp("estimated_return").toLocalDateTime() : null;

                PassSlip passSlip = new PassSlip(
                        resultSet.getInt("id"),
                        resultSet.getInt("employee_id"),
                        resultSet.getString("first_name") + " " + resultSet.getString("last_name"),
                        resultSet.getString("department"),
                        resultSet.getString("reason"),
                        timeOut,
                        actualTimeOut,
                        timeIn,
                        estimatedReturn,
                        resultSet.getLong("duration_minutes"),
                        resultSet.getString("status")
                );
                passSlip.setRequesterSignature(resultSet.getBytes("requester_signature"));

                data.add(passSlip);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private void filterMonitoringData(String keyword) {
        ObservableList<PassSlip> filtered = FXCollections.observableArrayList();
        String statusFilter = cmbStatusFilter.getValue();

        for (PassSlip ps : monitoringData) {
            boolean matchesStatus = (statusFilter == null || statusFilter.isBlank()
                    || statusFilter.equals("ALL")
                    || ps.getStatus().equalsIgnoreCase(statusFilter));
            boolean matchesKeyword = (keyword == null || keyword.isBlank() ||
                    ps.getEmployeeName().toLowerCase().contains(keyword.toLowerCase())
                    || ps.getStatus().toLowerCase().contains(keyword.toLowerCase())
                    || String.valueOf(ps.getEmployeeId()).contains(keyword)
                    || (ps.getDepartment() != null
                        && ps.getDepartment().toLowerCase().contains(keyword.toLowerCase()))
                    || (ps.getReason() != null
                        && ps.getReason().toLowerCase().contains(keyword.toLowerCase())));

            if (matchesStatus && matchesKeyword) {
                filtered.add(ps);
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
