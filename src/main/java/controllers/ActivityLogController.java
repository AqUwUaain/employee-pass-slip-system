package controllers;

import database.DatabaseConnection;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.PassSlip;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.Session;
import utils.SidebarHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogController {

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
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnThemeToggle;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    @FXML
    private TextField txtSearchMonitoringLogs;

    @FXML
    private Button btnClearLogsFilter;

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
    private TableColumn<PassSlip, String> colReason;

    @FXML
    private TableColumn<PassSlip, String> colStatus;

    @FXML
    private TableColumn<PassSlip, String> colDuration;

    @FXML
    private TableColumn<PassSlip, String> colTimeIn;

    private ObservableList<PassSlip> monitoringData;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {

        if (!Session.isAdmin()) {
            showAccessRestrictedDialog();
            return;
        }

        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarPasswordReset,
                btnLogout, btnNotificationsAlert,
                btnSidebarMonitoring, btnThemeToggle
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        setupMonitoringTable();

        btnClearLogsFilter.setOnAction(event -> {
            txtSearchMonitoringLogs.clear();
            monitoringTableView.setItems(monitoringData);
        });

        txtSearchMonitoringLogs.textProperty().addListener(
                (observable, oldValue, newValue) -> filterMonitoringData(newValue)
        );

        loadMonitoringDataAsync();
    }

    private void setupMonitoringTable() {
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
                        ps.time_in,
                        ps.estimated_return,
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

                LocalDateTime estimatedReturn = resultSet.getTimestamp("estimated_return") != null
                        ? resultSet.getTimestamp("estimated_return").toLocalDateTime() : null;

                PassSlip passSlip = new PassSlip(
                        resultSet.getInt("id"),
                        resultSet.getInt("employee_id"),
                        resultSet.getString("first_name") + " " + resultSet.getString("last_name"),
                        resultSet.getString("department"),
                        resultSet.getString("reason"),
                        timeOut,
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
                    || (ps.getReason() != null && ps.getReason().toLowerCase().contains(keyword.toLowerCase()))
                    || (ps.getDepartment() != null && ps.getDepartment().toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(ps);
            }
        }

        monitoringTableView.setItems(filtered);
    }

    public static void logActivity(String action, int employeeId) {
        logActivity(action, "", employeeId);
    }

    public static void logActivity(String action, String description, int employeeId) {

        try (Connection connection =
                    DatabaseConnection.connect()) {

            String query = """
                    INSERT INTO activity_logs (username, action, description, employee_id, user_id, timestamp)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(1, Session.currentUsername != null ? Session.currentUsername : "System");
            statement.setString(2, action);
            statement.setString(3, description);
            statement.setInt(4, employeeId);
            statement.setInt(5, Session.currentUserId);
            statement.setTimestamp(6, java.sql.Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));

            statement.executeUpdate();

        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showAccessRestrictedDialog() {
        StackPane root = (StackPane) monitoringTableView.getScene().getRoot();

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

        VBox dialog = new VBox(16);
        dialog.setAlignment(javafx.geometry.Pos.CENTER);
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

        Label message = new Label("Only Administrator accounts can access the Activity History module.");
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #A8A29E; -fx-text-alignment: center; -fx-wrap-text: true;");
        message.setMaxWidth(320);
        message.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button okButton = new Button("OK");
        okButton.setStyle(
                "-fx-background-color: #D4A853; -fx-text-fill: #1C0A04; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 32px; -fx-cursor: hand;"
        );
        okButton.setOnAction(e -> {
            root.getChildren().remove(overlay);
            NavigationHelper.navigateToDashboard(monitoringTableView);
        });

        dialog.getChildren().addAll(icon, title, message, okButton);
        StackPane.setAlignment(dialog, javafx.geometry.Pos.CENTER);
        overlay.getChildren().add(dialog);
        root.getChildren().add(overlay);
    }

}
