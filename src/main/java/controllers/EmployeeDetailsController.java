package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import models.Employee;
import utils.NavigationHelper;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeDetailsController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnBackToDirectory;

    @FXML
    private Button btnEditEmployeeProfile;

    @FXML
    private Label lblHeaderEmployeeName;

    @FXML
    private Label lblHeaderDepartmentTag;

    @FXML
    private Label lblHeaderPositionTitle;

    @FXML
    private Label lblValEmployeeId;

    @FXML
    private Label lblValEmail;

    @FXML
    private Label lblValMobile;

    @FXML
    private Label lblValShiftSchedule;

    @FXML
    private Label lblValRegistryStatus;

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
                        "/fxml/EmployeeList.fxml"
                )
        );

        btnSidebarReports.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarReports,
                        "/fxml/Reports.fxml"
                )
        );

        btnBackToDirectory.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnBackToDirectory,
                        "/fxml/EmployeeList.fxml"
                )
        );

        btnEditEmployeeProfile.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnEditEmployeeProfile,
                        "/fxml/EmployeeForm.fxml"
                )
        );

        loadSelectedEmployee();

    }

    public static Employee getEmployeeById(int employeeId) {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query =
                    "SELECT * FROM employees WHERE id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setInt(1, employeeId);

            ResultSet resultSet =
                    statement.executeQuery();

            if(resultSet.next()) {

                return new Employee(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("department"),
                        resultSet.getString("position"),
                        resultSet.getString("contact"),
                        resultSet.getDate("join_date").toLocalDate()
                );

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

    private void loadSelectedEmployee() {

        Employee employee =
                getEmployeeById(
                        Session.selectedEmployeeId
                );

        if (employee == null) {
            lblHeaderEmployeeName.setText("Employee not found");
            return;
        }

        lblHeaderEmployeeName.setText(
                employee.getFirstName()
                        + " "
                        + employee.getLastName()
        );
        lblHeaderDepartmentTag.setText(
                employee.getDepartment()
        );
        lblHeaderPositionTitle.setText(
                employee.getPosition()
        );
        lblValEmployeeId.setText(
                String.valueOf(employee.getId())
        );
        lblValEmail.setText("Not stored in current schema");
        lblValMobile.setText(
                employee.getContact()
        );
        lblValShiftSchedule.setText(
                "Joined on " + employee.getJoinDate()
        );
        lblValRegistryStatus.setText(
                "Active Personnel Record"
        );

    }

}
