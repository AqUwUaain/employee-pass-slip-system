package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.TimerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PassSlipController {

    @FXML
    private Button btnTopClose;

    @FXML
    private Button btnCancelSlipAction;

    @FXML
    private Button btnPrintSlipAction;

    @FXML
    private Button btnSaveSlipAction;

    @FXML
    private TextField txtSlipYear;

    @FXML
    private TextField txtSlipFormattedDate;

    @FXML
    private TextField txtSlipClientId;

    @FXML
    private TextField txtSlipEmployeeName;

    @FXML
    private TextArea txtSlipPurpose;

    @FXML
    private TextField txtSlipTimeOut;

    @FXML
    private TextField txtSlipTimeIn;

    @FXML
    private TextField txtSlipAdminSignee;

    @FXML
    private Label lblSlipStatusMessage;

    @FXML
    private ComboBox<String> cmbEmployeeSelect;

    private int selectedEmployeeId = 0;

    @FXML
    private void initialize() {

        LocalDateTime now = LocalDateTime.now(PhilTime.ZONE);

        txtSlipYear.setText(String.valueOf(now.getYear()));
        txtSlipFormattedDate.setText(
                now.format(
                        DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                ).toUpperCase()
        );
        txtSlipTimeOut.setText(
                now.format(
                        DateTimeFormatter.ofPattern("hh:mm a")
                )
        );

        loadEmployeeComboBox();

        cmbEmployeeSelect.setOnAction(event -> {
            String selected = cmbEmployeeSelect.getValue();
            if (selected != null && !selected.isEmpty()) {
                String[] parts = selected.split(" \\| ");
                if (parts.length >= 1) {
                    try {
                        selectedEmployeeId = Integer.parseInt(parts[0].trim());
                        txtSlipClientId.setText(String.valueOf(selectedEmployeeId));
                        loadEmployeeName(selectedEmployeeId);
                    } catch (NumberFormatException e) {
                        selectedEmployeeId = 0;
                    }
                }
            }
        });

        btnTopClose.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnTopClose)
        );

        btnCancelSlipAction.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnCancelSlipAction)
        );

        btnPrintSlipAction.setOnAction(
                event -> lblSlipStatusMessage.setText("Print is not implemented yet.")
        );

        btnSaveSlipAction.setOnAction(
                event -> issuePassSlip(
                        txtSlipClientId.getText(),
                        txtSlipPurpose.getText(),
                        lblSlipStatusMessage
                )
        );

    }

    private void loadEmployeeComboBox() {

        ObservableList<String> employeeOptions =
                FXCollections.observableArrayList();

        try {

            Connection connection = DatabaseConnection.connect();

            String query = "SELECT id, first_name, last_name FROM employees ORDER BY id";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("first_name")
                        + " " + resultSet.getString("last_name");
                employeeOptions.add(id + " | " + name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cmbEmployeeSelect.setItems(employeeOptions);

    }

    private void loadEmployeeName(int employeeId) {

        try {

            Connection connection = DatabaseConnection.connect();

            String query = "SELECT first_name, last_name FROM employees WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, employeeId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                txtSlipEmployeeName.setText(
                        resultSet.getString("first_name")
                                + " " + resultSet.getString("last_name")
                );
            } else {
                txtSlipEmployeeName.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void issuePassSlip(

            String employeeId,
            String reason,
            Label messageLabel

    ) {

        if (employeeId == null || employeeId.isEmpty()) {
            messageLabel.setText("SELECT EMPLOYEE");
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            messageLabel.setText("REASON REQUIRED");
            return;
        }

        if (reason.trim().length() < 5) {
            messageLabel.setText("REASON TOO SHORT");
            return;
        }

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    INSERT INTO pass_slips
                    (employee_id, reason, time_out, status)
                    VALUES (?, ?, ?, ?)
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setInt(1, Integer.parseInt(employeeId));
            statement.setString(2, reason.trim());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
            statement.setString(4, "OUT");

            int inserted = statement.executeUpdate();

            if (inserted > 0) {

                // Track in timer service for admin live timer
                String empName = getEmployeeNameById(
                        Integer.parseInt(employeeId),
                        connection
                );
                TimerService.markOut(Integer.parseInt(employeeId), empName);

                ActivityLogController.logActivity(
                        "Issued Pass Slip for Employee ID: " + employeeId,
                        Integer.parseInt(employeeId)
                );

                messageLabel.setText("PASS SLIP ISSUED");

            } else {
                messageLabel.setText("FAILED TO ISSUE PASS SLIP");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("DATABASE ERROR");
        }

    }

    private static String getEmployeeNameById(int id, Connection connection) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT first_name, last_name FROM employees WHERE id = ?"
            );
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

}
