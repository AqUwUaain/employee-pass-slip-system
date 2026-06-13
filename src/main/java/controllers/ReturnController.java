package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.TimerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.Duration;
import java.time.LocalDateTime;

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
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnThemeToggle;

    @FXML
    private Button btnHamburgerMenuToggle;

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
    private TableView<?> tblOutstandingOutboundView;

    @FXML
    private TableColumn<?, ?> colReturnSlipId;

    @FXML
    private TableColumn<?, ?> colReturnEmployeeID;

    @FXML
    private TableColumn<?, ?> colReturnFullName;

    @FXML
    private TableColumn<?, ?> colReturnDepartment;

    @FXML
    private TableColumn<?, ?> colReturnTimeOut;

    @FXML
    private TableColumn<?, ?> colReturnExpectedIn;

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

        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(
                    event -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml")
            );

        NavigationHelper.setActiveButton(btnSidebarLogReturn);

        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));
        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> utils.NotificationHelper.toggle(btnNotificationsAlert));
        if (btnThemeToggle != null) {
            utils.ThemeManager.setThemeToggleLabel(btnThemeToggle);
            btnThemeToggle.setOnAction(e -> {
                utils.ThemeManager.toggleTheme();
                utils.ThemeManager.applyToScene(btnThemeToggle.getScene());
                utils.ThemeManager.setThemeToggleLabel(btnThemeToggle);
            });
        }
        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(e -> NavigationHelper.navigateTo(btnHamburgerMenuToggle, "/fxml/User.fxml"));

        btnFetchActiveSlip.setOnAction(event -> fetchActiveSlip());

        btnConfirmReturnLog.setOnAction(event -> processReturn());

        loadSummaryAsync();

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
                        "Active pass slip found: " + activePassSlipId
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
        } else {
            lblReturnStatusMessage.setText("Unable to record return.");
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
