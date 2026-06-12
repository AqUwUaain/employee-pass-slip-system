package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.Employee;
import utils.NavigationHelper;
import utils.Session;

public class EmployeeFormController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField positionField;

    @FXML
    private TextField contactField;

    @FXML
    private DatePicker joinDatePicker;

    @FXML
    private TextField managerField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField emergencyContactField;

    @FXML
    private Label titleLabel;

    @FXML
    private Label formTitleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button btnSaveEmployee;

    @FXML
    private Button btnBack;

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
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnHamburgerMenuToggle;

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
        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml"));
        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));
        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> NavigationHelper.navigateTo(btnNotificationsAlert, "/fxml/ActivityLog.fxml"));
        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(e -> NavigationHelper.navigateTo(btnHamburgerMenuToggle, "/fxml/User.fxml"));

        if (Session.selectedEmployeeId > 0) {
            loadEmployee();
        }

        btnSaveEmployee.setOnAction(
                event -> saveEmployee()
        );

        btnBack.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnBack,
                        "/fxml/EmployeeList.fxml"
                )
        );

    }

    private void loadEmployee() {

        Employee employee =
                EmployeeDetailsController.getEmployeeById(
                        Session.selectedEmployeeId
                );

        if (employee == null) {
            messageLabel.setText("Employee record not found.");
            return;
        }

        titleLabel.setText("Edit Employee");
        formTitleLabel.setText("Edit Employee");
        btnSaveEmployee.setText("Update Employee");

        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        departmentField.setText(employee.getDepartment());
        positionField.setText(employee.getPosition());
        contactField.setText(employee.getContact());
        joinDatePicker.setValue(employee.getJoinDate());

        if (employee.getManager() != null) {
            managerField.setText(employee.getManager());
        }
        if (employee.getEmail() != null) {
            emailField.setText(employee.getEmail());
        }
        if (employee.getAddress() != null) {
            addressField.setText(employee.getAddress());
        }
        if (employee.getEmergencyContact() != null) {
            emergencyContactField.setText(employee.getEmergencyContact());
        }

    }

    private void saveEmployee() {

        if (Session.selectedEmployeeId > 0) {

            boolean updated =
                    EmployeeController.updateEmployee(
                            Session.selectedEmployeeId,
                            firstNameField.getText(),
                            lastNameField.getText(),
                            departmentField.getText(),
                            positionField.getText(),
                            contactField.getText(),
                            managerField.getText(),
                            emailField.getText(),
                            addressField.getText(),
                            emergencyContactField.getText()
                    );

            if (updated) {
                NavigationHelper.navigateTo(
                        btnSaveEmployee,
                        "/fxml/EmployeeDetails.fxml"
                );
            }
            else {
                messageLabel.setText("Failed to update employee.");
            }

            return;
        }

        EmployeeController.addEmployee(
                firstNameField.getText(),
                lastNameField.getText(),
                departmentField.getText(),
                positionField.getText(),
                contactField.getText(),
                joinDatePicker.getValue(),
                managerField.getText(),
                emailField.getText(),
                addressField.getText(),
                emergencyContactField.getText(),
                messageLabel
        );

    }
}
