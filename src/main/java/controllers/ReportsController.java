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
import javafx.stage.FileChooser;
import models.ActivityLog;
import models.PassSlip;
import utils.NavigationHelper;
import utils.SidebarHelper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Alert;

public class ReportsController {

    @FXML
    private Button btnOpenDashboard;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Label lblTotalEmployees;

    @FXML
    private Label lblTotalPassSlips;

    @FXML
    private Label lblMonthlyCount;

    @FXML
    private TextField txtSearchHistory;

    @FXML
    private TableView<PassSlip> historyTableView;

    @FXML
    private TableColumn<PassSlip, String> colTimestamp;

    @FXML
    private TableColumn<PassSlip, String> colEmployeeID;

    @FXML
    private TableColumn<PassSlip, String> colFullName;

    @FXML
    private TableColumn<PassSlip, String> colDepartment;

    @FXML
    private TableColumn<PassSlip, String> colReason;

    @FXML
    private TableColumn<PassSlip, String> colExpectedReturn;

    @FXML
    private TableColumn<PassSlip, String> colStatus;

    @FXML
    private TableColumn<PassSlip, String> colDuration;

    @FXML
    private TableColumn<PassSlip, String> colTimeIn;

    @FXML
    private Button btnExportCsv;

    @FXML
    private Button btnExportExcel;

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
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private ObservableList<PassSlip> historyData;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, null,
                btnSidebarReports, btnThemeToggle,
                btnManageEmployees, manageEmployeesSubMenu
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

        btnOpenDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnOpenDashboard)
        );

        if (btnExportCsv != null) {
            btnExportCsv.setOnAction(event -> exportReportCsv());
        }

        if (btnExportExcel != null) {
            btnExportExcel.setOnAction(event -> exportReportExcel());
        }

        setupHistoryTable();

        txtSearchHistory.textProperty().addListener(
                (observable, oldValue, newValue) -> filterHistoryData(newValue)
        );

        loadReportsAsync();
        loadHistoryAsync();

    }

    private void setupHistoryTable() {
        colTimestamp.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeOut = ps.getTimeOut() != null ? ps.getTimeOut().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeOut);
                }
        );

        colEmployeeID.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getEmployeeId()))
        );

        colFullName.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmployeeName())
        );

        colDepartment.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDepartment() != null ? cellData.getValue().getDepartment() : ""
                )
        );

        colReason.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""
                )
        );

        colExpectedReturn.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEstimatedReturnText())
        );

        colStatus.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus())
        );

        colDuration.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDurationText())
        );

        colTimeIn.setCellValueFactory(
                cellData -> {
                    PassSlip ps = cellData.getValue();
                    String timeIn = ps.getTimeIn() != null ? ps.getTimeIn().format(formatter) : "";
                    return new ReadOnlyStringWrapper(timeIn);
                }
        );
    }

    private void loadHistoryAsync() {
        Task<ObservableList<PassSlip>> task = new Task<>() {
            @Override
            protected ObservableList<PassSlip> call() {
                MonitoringController.updateExpiredPassSlips();
                return fetchHistoryData();
            }
        };

        task.setOnSucceeded(e -> {
            historyData = task.getValue();
            historyTableView.setItems(historyData);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<PassSlip> fetchHistoryData() {
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
                        ps.status
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    ORDER BY ps.time_out DESC
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

                data.add(passSlip);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private void filterHistoryData(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            historyTableView.setItems(historyData);
            return;
        }

        ObservableList<PassSlip> filtered = FXCollections.observableArrayList();

        for (PassSlip ps : historyData) {
            if (ps.getEmployeeName().toLowerCase().contains(keyword.toLowerCase())
                    || ps.getStatus().toLowerCase().contains(keyword.toLowerCase())
                    || String.valueOf(ps.getEmployeeId()).contains(keyword)
                    || (ps.getReason() != null && ps.getReason().toLowerCase().contains(keyword.toLowerCase()))
                    || (ps.getDepartment() != null && ps.getDepartment().toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(ps);
            }
        }

        historyTableView.setItems(filtered);
    }

    private void loadReportsAsync() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try (Connection connection = DatabaseConnection.connect()) {

                    if (connection == null) return null;

                    PreparedStatement employeeStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM employees");
                    ResultSet employeeResult = employeeStatement.executeQuery();
                    int empCount = employeeResult.next() ? employeeResult.getInt(1) : 0;

                    PreparedStatement passSlipStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM pass_slips");
                    ResultSet passSlipResult = passSlipStatement.executeQuery();
                    int psCount = passSlipResult.next() ? passSlipResult.getInt(1) : 0;

                    PreparedStatement monthlyStatement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM pass_slips WHERE EXTRACT(YEAR FROM time_out) = EXTRACT(YEAR FROM CURRENT_DATE) AND EXTRACT(MONTH FROM time_out) = EXTRACT(MONTH FROM CURRENT_DATE)"
                    );
                    ResultSet monthlyResult = monthlyStatement.executeQuery();
                    int monthlyCount = monthlyResult.next() ? monthlyResult.getInt(1) : 0;

                    Platform.runLater(() -> {
                        if (lblTotalEmployees != null)
                            lblTotalEmployees.setText(String.valueOf(empCount));
                        if (lblTotalPassSlips != null)
                            lblTotalPassSlips.setText(String.valueOf(psCount));
                        if (lblMonthlyCount != null)
                            lblMonthlyCount.setText(String.valueOf(monthlyCount));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public static ObservableList<ActivityLog> getLogs() {
        return getLogs(0);
    }

    public static ObservableList<ActivityLog> getLogs(int limit) {

        ObservableList<ActivityLog> logs =
                FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) return logs;

            String query = "SELECT * FROM activity_logs ORDER BY timestamp DESC";
            if (limit > 0) {
                query += " LIMIT " + limit;
            }

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    ActivityLog log = new ActivityLog(
                            resultSet.getInt("id"),
                            resultSet.getString("action"),
                            resultSet.getString("description"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("username"),
                            resultSet.getTimestamp("timestamp").toLocalDateTime(),
                            resultSet.getInt("employee_id")
                    );
                    logs.add(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }

    public static ObservableList<ActivityLog> getPassSlipLogs(int limit) {

        ObservableList<ActivityLog> logs =
                FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) return logs;

            String query = """
                    SELECT * FROM activity_logs
                    WHERE action LIKE '%Pass Slip%'
                       OR action LIKE '%PRINT_SLIP%'
                    ORDER BY timestamp DESC""";
            if (limit > 0) {
                query += " LIMIT " + limit;
            }

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    ActivityLog log = new ActivityLog(
                            resultSet.getInt("id"),
                            resultSet.getString("action"),
                            resultSet.getString("description"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("username"),
                            resultSet.getTimestamp("timestamp").toLocalDateTime(),
                            resultSet.getInt("employee_id")
                    );
                    logs.add(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }

    public static ObservableList<ActivityLog> getPassSlipLogsForDate(java.time.LocalDate date) {
        ObservableList<ActivityLog> logs = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) return logs;
            String query = """
                    SELECT * FROM activity_logs
                    WHERE timestamp::date = ?
                      AND (action LIKE '%Pass Slip%'
                           OR action LIKE '%PRINT_SLIP%')
                    ORDER BY timestamp DESC""";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDate(1, java.sql.Date.valueOf(date));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        logs.add(new ActivityLog(
                                resultSet.getInt("id"),
                                resultSet.getString("action"),
                                resultSet.getString("description"),
                                resultSet.getInt("user_id"),
                                resultSet.getString("username"),
                                resultSet.getTimestamp("timestamp").toLocalDateTime(),
                                resultSet.getInt("employee_id")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    public static ObservableList<ActivityLog> getLogsForDate(java.time.LocalDate date) {
        ObservableList<ActivityLog> logs = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) return logs;
            String query = "SELECT * FROM activity_logs WHERE timestamp::date = ? ORDER BY timestamp DESC";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDate(1, java.sql.Date.valueOf(date));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        logs.add(new ActivityLog(
                                resultSet.getInt("id"),
                                resultSet.getString("action"),
                                resultSet.getString("description"),
                                resultSet.getInt("user_id"),
                                resultSet.getString("username"),
                                resultSet.getTimestamp("timestamp").toLocalDateTime(),
                                resultSet.getInt("employee_id")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    public static ObservableList<ActivityLog> getLogsForUser(int userId, int limit) {

        ObservableList<ActivityLog> logs =
                FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) return logs;

            String query = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC";
            if (limit > 0) {
                query += " LIMIT " + limit;
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ActivityLog log = new ActivityLog(
                                resultSet.getInt("id"),
                                resultSet.getString("action"),
                                resultSet.getString("description"),
                                resultSet.getInt("user_id"),
                                resultSet.getString("username"),
                                resultSet.getTimestamp("timestamp").toLocalDateTime(),
                                resultSet.getInt("employee_id")
                        );
                        logs.add(log);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }

    public static ObservableList<PassSlip> getPassSlips() {
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
                        ps.status
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    ORDER BY ps.time_out DESC
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

                data.add(passSlip);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private void exportReportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV Report");
        fileChooser.setInitialFileName("pass_slip_report.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv")
        );

        File file = fileChooser.showSaveDialog(btnExportCsv.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file));
             Connection connection = DatabaseConnection.connect()) {

            writer.println("ID,Employee,Department,Reason,Time Out,Time In,Duration,Status");

            PreparedStatement statement = connection.prepareStatement(
                    """
                    SELECT ps.id, e.first_name || ' ' || e.last_name AS name,
                           e.department, ps.reason, ps.time_out, ps.time_in,
                           ps.duration, ps.status
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    ORDER BY ps.time_out DESC
                    """
            );

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                writer.println(
                        result.getInt("id") + ","
                                + "\"" + result.getString("name").replace("\"", "\"\"") + "\","
                                + "\"" + result.getString("department") + "\","
                                + "\"" + result.getString("reason").replace("\"", "\"\"") + "\","
                                + result.getTimestamp("time_out") + ","
                                + (result.getTimestamp("time_in") != null ? result.getTimestamp("time_in") : "") + ","
                                + "\"" + (result.getString("duration") != null ? result.getString("duration") : "") + "\","
                                + result.getString("status")
                );
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("CSV exported successfully to:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export CSV.");
            alert.showAndWait();
        }
    }

    private void exportReportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Excel Report");
        fileChooser.setInitialFileName("pass_slip_report.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(btnExportExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook();
             Connection connection = DatabaseConnection.connect()) {
            Sheet sheet = workbook.createSheet("Pass Slip Report");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Employee", "Department", "Reason", "Time Out", "Time In", "Duration", "Status"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            PreparedStatement statement = connection.prepareStatement(
                    """
                    SELECT ps.id, e.first_name || ' ' || e.last_name AS name,
                           e.department, ps.reason, ps.time_out, ps.time_in,
                           ps.duration, ps.status
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    ORDER BY ps.time_out DESC
                    """
            );

            ResultSet result = statement.executeQuery();
            int rowNum = 1;

            while (result.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getInt("id"));
                row.createCell(1).setCellValue(result.getString("name"));
                row.createCell(2).setCellValue(result.getString("department"));
                row.createCell(3).setCellValue(result.getString("reason"));
                row.createCell(4).setCellValue(
                        result.getTimestamp("time_out") != null ? result.getTimestamp("time_out").toString() : ""
                );
                row.createCell(5).setCellValue(
                        result.getTimestamp("time_in") != null ? result.getTimestamp("time_in").toString() : ""
                );
                row.createCell(6).setCellValue(
                        result.getString("duration") != null ? result.getString("duration") : ""
                );
                row.createCell(7).setCellValue(result.getString("status"));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Excel exported successfully to:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export Excel.");
            alert.showAndWait();
        }
    }

}
