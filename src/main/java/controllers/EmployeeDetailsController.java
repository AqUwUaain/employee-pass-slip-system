package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
    private Label lblValManager;

    @FXML
    private Label lblValAddress;

    @FXML
    private Label lblValEmergencyContact;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    @FXML
    private void initialize() {
        NavigationHelper.setActiveButton(btnSidebarEmployees);

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

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarLogReturn, "/fxml/Return.fxml"));
        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarUsers, "/fxml/User.fxml"));
        if (btnSidebarSignatures != null)
            btnSidebarSignatures.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarSignatures, "/fxml/SignatureManager.fxml"));
        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml"));

        NavigationHelper.hideAdminSidebarItems(
            btnSidebarEmployees,
            btnSidebarReports,
            btnSidebarUsers,
            btnSidebarPasswordReset
        );

        NavigationHelper.hideMonitoringForStaff(btnSidebarMonitoring);

        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));
        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> utils.NotificationHelper.toggle(btnNotificationsAlert));
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
                        resultSet.getDate("join_date").toLocalDate(),
                        resultSet.getString("manager"),
                        resultSet.getString("email"),
                        resultSet.getString("address"),
                        resultSet.getString("emergency_contact")
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

        if (employee.getEmail() != null && !employee.getEmail().isBlank()) {
            lblValEmail.setText(employee.getEmail());
        } else {
            lblValEmail.setText("Not provided");
        }

        lblValMobile.setText(employee.getContact());
        lblValShiftSchedule.setText("Joined on " + employee.getJoinDate());
        lblValRegistryStatus.setText("Active Personnel Record");

        if (lblValManager != null) {
            lblValManager.setText(
                    employee.getManager() != null && !employee.getManager().isBlank()
                            ? employee.getManager() : "Not assigned"
            );
        }
        if (lblValAddress != null) {
            lblValAddress.setText(
                    employee.getAddress() != null && !employee.getAddress().isBlank()
                            ? employee.getAddress() : "Not provided"
            );
        }
        if (lblValEmergencyContact != null) {
            lblValEmergencyContact.setText(
                    employee.getEmergencyContact() != null && !employee.getEmergencyContact().isBlank()
                            ? employee.getEmergencyContact() : "Not provided"
            );
        }

    }

}
