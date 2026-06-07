package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import utils.NavigationHelper;

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

    private int activePassSlipId;

    @FXML
    private void initialize() {

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(
                        btnSidebarDashboard
                )
        );

        btnSidebarMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarMonitoring,
                        "/fxml/Monitoring.fxml"
                )
        );

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

        btnFetchActiveSlip.setOnAction(
                event -> fetchActiveSlip()
        );

        btnConfirmReturnLog.setOnAction(
                event -> processReturn()
        );

        loadSummary();

    }

    public static boolean recordReturn(int passSlipId) {

        try {

            Connection connection = DatabaseConnection.connect();




            // GET TIME OUT
            String getQuery = """
                    SELECT time_out
                    FROM pass_slips
                    WHERE id = ?
                    """;

            PreparedStatement getStatement =
                    connection.prepareStatement(getQuery);

            getStatement.setInt(1, passSlipId);

            ResultSet resultSet = getStatement.executeQuery();




            if(resultSet.next()) {

                LocalDateTime timeOut =
                        resultSet.getTimestamp("time_out")
                                .toLocalDateTime();

                LocalDateTime timeIn =
                        LocalDateTime.now();




                // COMPUTE DURATION
                Duration duration =
                        Duration.between(timeOut, timeIn);

                long hours = duration.toHours();

                long minutes = duration.toMinutes() % 60;

                String durationText =
                        hours + " hrs " + minutes + " mins";




                // UPDATE PASS SLIP
                String updateQuery = """
                        UPDATE pass_slips
                        SET
                        time_in = ?,
                        duration = ?,
                        status = 'RETURNED'
                        WHERE id = ?
                        """;

                PreparedStatement updateStatement =
                        connection.prepareStatement(updateQuery);

                updateStatement.setTimestamp(
                        1,
                        java.sql.Timestamp.valueOf(timeIn)
                );

                updateStatement.setString(
                        2,
                        durationText
                );

                updateStatement.setInt(
                        3,
                        passSlipId
                );

                int updated =
                        updateStatement.executeUpdate();




                if(updated > 0) {

                    // ACTIVITY LOG
                    ActivityLogController.logActivity(
                            "Returned Pass Slip ID: "
                                    + passSlipId
                    );

                }




                return updated > 0;

            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return false;

    }

    private void fetchActiveSlip() {

        try {

            int searchValue =
                    Integer.parseInt(
                            txtReturnSearchId.getText().trim()
                    );

            Connection connection =
                    DatabaseConnection.connect();

            if (connection == null) {
                lblReturnStatusMessage.setText("Database unavailable.");
                return;
            }

            PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            SELECT p.id, e.first_name, e.last_name
                            FROM pass_slips p
                            JOIN employees e
                              ON p.employee_id = e.id
                            WHERE p.status = 'OUT'
                              AND (p.id = ? OR e.id = ?)
                            ORDER BY p.id DESC
                            LIMIT 1
                            """
                    );

            statement.setInt(1, searchValue);
            statement.setInt(2, searchValue);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                activePassSlipId =
                        resultSet.getInt("id");

                txtReturnEmployeeName.setText(
                        resultSet.getString("first_name")
                                + " "
                                + resultSet.getString("last_name")
                );

                lblReturnStatusMessage.setText(
                        "Active pass slip found: "
                                + activePassSlipId
                );

            }
            else {
                activePassSlipId = 0;
                txtReturnEmployeeName.clear();
                lblReturnStatusMessage.setText("No active pass slip found.");
            }

        }
        catch (Exception e) {

            activePassSlipId = 0;
            lblReturnStatusMessage.setText("Enter a valid slip or employee ID.");

        }

    }

    private void processReturn() {

        if (activePassSlipId <= 0) {
            lblReturnStatusMessage.setText("Find an active pass slip first.");
            return;
        }

        boolean success =
                recordReturn(activePassSlipId);

        if (success) {
            lblReturnStatusMessage.setText("Return recorded successfully.");
            txtReturnSearchId.clear();
            txtReturnEmployeeName.clear();
            txtReturnTimeInStamp.clear();
            activePassSlipId = 0;
            loadSummary();
        }
        else {
            lblReturnStatusMessage.setText("Unable to record return.");
        }

    }

    private void loadSummary() {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            if (connection == null) {
                return;
            }

            PreparedStatement outStatement =
                    connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE status = 'OUT'
                            """
                    );

            ResultSet outResult =
                    outStatement.executeQuery();

            if (outResult.next()) {
                lblTotalCurrentlyOut.setText(
                        String.valueOf(
                                outResult.getInt(1)
                        )
                );
            }

            PreparedStatement returnedStatement =
                    connection.prepareStatement(
                            """
                            SELECT COUNT(*)
                            FROM pass_slips
                            WHERE status = 'RETURNED'
                              AND DATE(time_in) = CURRENT_DATE
                            """
                    );

            ResultSet returnedResult =
                    returnedStatement.executeQuery();

            if (returnedResult.next()) {
                lblTotalReturnedToday.setText(
                        String.valueOf(
                                returnedResult.getInt(1)
                        )
                );
            }

        }
        catch (Exception e) {

            lblTotalCurrentlyOut.setText("0");
            lblTotalReturnedToday.setText("0");

        }

    }

}
