package controllers;

import database.DatabaseConnection;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.ActivityLog;
import utils.NavigationHelper;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class ReportsController {

    @FXML
    private Button btnOpenDashboard;

    @FXML
    private Button btnOpenEmployees;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnHamburgerMenuToggle;

    @FXML
    private Label lblTotalEmployees;

    @FXML
    private Label lblTotalPassSlips;

    @FXML
    private Label lblMonthlyCount;

    @FXML
    private VBox recentHistoryList;

    @FXML
    private Button btnExportCsv;

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
    private void initialize() {

        if (btnSidebarDashboard != null)
            btnSidebarDashboard.setOnAction(e -> NavigationHelper.navigateToDashboard(btnSidebarDashboard));
        if (btnSidebarMonitoring != null)
            btnSidebarMonitoring.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarMonitoring, "/fxml/Monitoring.fxml"));
        if (btnSidebarEmployees != null)
            btnSidebarEmployees.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarEmployees, "/fxml/EmployeeController.fxml"));
        if (btnSidebarReports != null)
            btnSidebarReports.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarReports, "/fxml/Reports.fxml"));
        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarUsers, "/fxml/User.fxml"));
        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));

        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> NavigationHelper.navigateTo(btnNotificationsAlert, "/fxml/ActivityLog.fxml"));
        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(e -> NavigationHelper.navigateTo(btnHamburgerMenuToggle, "/fxml/User.fxml"));

        btnOpenDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnOpenDashboard)
        );

        btnOpenEmployees.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenEmployees,
                        "/fxml/EmployeeController.fxml"
                )
        );

        if (btnExportCsv != null) {
            btnExportCsv.setOnAction(event -> exportReportCsv());
        }

        loadReportsAsync();
        loadRecentHistoryAsync();

    }

    private void loadReportsAsync() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {

                    Connection connection =
                            DatabaseConnection.connect();

                    if (connection == null) return null;

                    PreparedStatement employeeStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM employees");
                    ResultSet employeeResult = employeeStatement.executeQuery();
                    int empCount = employeeResult.next() ? employeeResult.getInt(1) : 0;

                    PreparedStatement passSlipStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM pass_slips");
                    ResultSet passSlipResult = passSlipStatement.executeQuery();
                    int psCount = passSlipResult.next() ? passSlipResult.getInt(1) : 0;

                    PreparedStatement outStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM pass_slips WHERE status = 'OUT'");
                    ResultSet outResult = outStatement.executeQuery();
                    int outCount = outResult.next() ? outResult.getInt(1) : 0;

                    PreparedStatement returnedStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM pass_slips WHERE status = 'RETURNED'");
                    ResultSet returnedResult = returnedStatement.executeQuery();
                    int retCount = returnedResult.next() ? returnedResult.getInt(1) : 0;

                    PreparedStatement expiredStatement =
                            connection.prepareStatement("SELECT COUNT(*) FROM pass_slips WHERE status = 'EXPIRED'");
                    ResultSet expiredResult = expiredStatement.executeQuery();
                    int expCount = expiredResult.next() ? expiredResult.getInt(1) : 0;

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

    public static void loadReports(

            Label totalEmployeesLabel,

            Label totalPassSlipsLabel,

            Label totalOutLabel,

            Label totalReturnedLabel,

            Label totalExpiredLabel

    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String employeeQuery =
                    "SELECT COUNT(*) FROM employees";

            PreparedStatement employeeStatement =
                    connection.prepareStatement(employeeQuery);

            ResultSet employeeResult =
                    employeeStatement.executeQuery();

            if (employeeResult.next()) {
                totalEmployeesLabel.setText(
                        "TOTAL EMPLOYEES: " + employeeResult.getInt(1)
                );
            }

            String passSlipQuery =
                    "SELECT COUNT(*) FROM pass_slips";

            PreparedStatement passSlipStatement =
                    connection.prepareStatement(passSlipQuery);

            ResultSet passSlipResult =
                    passSlipStatement.executeQuery();

            if (passSlipResult.next()) {
                totalPassSlipsLabel.setText(
                        "TOTAL PASS SLIPS: " + passSlipResult.getInt(1)
                );
            }

            String outQuery = """
                    SELECT COUNT(*) FROM pass_slips WHERE status = 'OUT'
                    """;

            PreparedStatement outStatement =
                    connection.prepareStatement(outQuery);

            ResultSet outResult = outStatement.executeQuery();

            if (outResult.next()) {
                totalOutLabel.setText("TOTAL OUT: " + outResult.getInt(1));
            }

            String returnedQuery = """
                    SELECT COUNT(*) FROM pass_slips WHERE status = 'RETURNED'
                    """;

            PreparedStatement returnedStatement =
                    connection.prepareStatement(returnedQuery);

            ResultSet returnedResult = returnedStatement.executeQuery();

            if (returnedResult.next()) {
                totalReturnedLabel.setText("TOTAL RETURNED: " + returnedResult.getInt(1));
            }

            String expiredQuery = """
                    SELECT COUNT(*) FROM pass_slips WHERE status = 'EXPIRED'
                    """;

            PreparedStatement expiredStatement =
                    connection.prepareStatement(expiredQuery);

            ResultSet expiredResult = expiredStatement.executeQuery();

            if (expiredResult.next()) {
                totalExpiredLabel.setText("TOTAL EXPIRED: " + expiredResult.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public static ObservableList<String[]> getEmployeePassSlipFrequency() {

        ObservableList<String[]> frequencyList =
                FXCollections.observableArrayList();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT
                        e.id,
                        e.first_name || ' ' || e.last_name AS employee_name,
                        e.department,
                        COUNT(ps.id) AS total_slips,
                        SUM(CASE WHEN ps.status = 'OUT' THEN 1 ELSE 0 END) AS currently_out,
                        SUM(CASE WHEN ps.status = 'RETURNED' THEN 1 ELSE 0 END) AS returned_count,
                        SUM(CASE WHEN ps.status = 'EXPIRED' THEN 1 ELSE 0 END) AS expired_count
                    FROM employees e
                    LEFT JOIN pass_slips ps ON e.id = ps.employee_id
                    GROUP BY e.id, e.first_name, e.last_name, e.department
                    ORDER BY total_slips DESC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                frequencyList.add(new String[]{
                        String.valueOf(resultSet.getInt("id")),
                        resultSet.getString("employee_name"),
                        resultSet.getString("department"),
                        String.valueOf(resultSet.getInt("total_slips")),
                        String.valueOf(resultSet.getInt("currently_out")),
                        String.valueOf(resultSet.getInt("returned_count")),
                        String.valueOf(resultSet.getInt("expired_count"))
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return frequencyList;

    }

    public static ObservableList<String[]> getEmployeePassSlipReasons(int employeeId) {

        ObservableList<String[]> reasonsList =
                FXCollections.observableArrayList();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT id, reason, time_out, time_in, duration, status
                    FROM pass_slips
                    WHERE employee_id = ?
                    ORDER BY time_out DESC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setInt(1, employeeId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                reasonsList.add(new String[]{
                        String.valueOf(resultSet.getInt("id")),
                        resultSet.getString("reason"),
                        resultSet.getTimestamp("time_out") != null
                                ? resultSet.getTimestamp("time_out").toString() : "",
                        resultSet.getTimestamp("time_in") != null
                                ? resultSet.getTimestamp("time_in").toString() : "N/A",
                        resultSet.getString("duration") != null
                                ? resultSet.getString("duration") : "N/A",
                        resultSet.getString("status")
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reasonsList;

    }

    public static int getDailyPassSlipCount() {

        try {

            Connection connection = DatabaseConnection.connect();

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM pass_slips WHERE DATE(time_out) = CURRENT_DATE"
            );

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    public static int getMonthlyPassSlipCount() {

        try {

            Connection connection = DatabaseConnection.connect();

            PreparedStatement statement = connection.prepareStatement(
                    """
                    SELECT COUNT(*) FROM pass_slips
                    WHERE EXTRACT(YEAR FROM time_out) = EXTRACT(YEAR FROM CURRENT_DATE)
                    AND EXTRACT(MONTH FROM time_out) = EXTRACT(MONTH FROM CURRENT_DATE)
                    AND status != 'REJECTED'
                    """
            );

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    private void loadRecentHistoryAsync() {

        Task<ObservableList<ActivityLog>> task = new Task<>() {
            @Override
            protected ObservableList<ActivityLog> call() {
                return getLogs();
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<ActivityLog> logs = task.getValue();
            recentHistoryList.getChildren().clear();
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (ActivityLog log : logs) {

                HBox row = new HBox(10);
                row.setStyle("-fx-padding: 10px 14px; -fx-background-radius: 8px;");
                row.setOnMouseEntered(ev -> row.setStyle("-fx-padding: 10px 14px; -fx-background-radius: 8px; -fx-background-color: #3D3229;"));
                row.setOnMouseExited(ev -> row.setStyle("-fx-padding: 10px 14px; -fx-background-radius: 8px;"));

                Label userLabel = new Label(log.getUsername());
                userLabel.setPrefWidth(180);
                userLabel.setStyle("-fx-text-fill: #E7E5E4; -fx-font-size: 13px;");

                Label actionLabel = new Label(log.getAction());
                actionLabel.setPrefWidth(320);
                actionLabel.setStyle("-fx-text-fill: #D6CCC2; -fx-font-size: 13px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label timeLabel = new Label(
                        log.getTimestamp().format(formatter)
                );
                timeLabel.setStyle("-fx-text-fill: #78716C; -fx-font-size: 12px;");

                row.getChildren().addAll(userLabel, actionLabel, spacer, timeLabel);

                recentHistoryList.getChildren().add(row);

            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    private void exportReportCsv() {

        try {

            PrintWriter writer = new PrintWriter(new FileWriter("pass_slip_report.csv"));

            writer.println("ID,Employee,Department,Reason,Time Out,Time In,Duration,Status");

            Connection connection = DatabaseConnection.connect();

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

            writer.close();

            System.out.println("Report exported to pass_slip_report.csv");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
