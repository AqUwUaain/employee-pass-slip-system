package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.Employee;
import utils.NavigationHelper;
import utils.Session;
import utils.SidebarHelper;

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
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployees, btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarPasswordReset,
                btnLogout, btnNotificationsAlert,
                btnSidebarEmployees
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

        if (Session.selectedEmployeeId > 0) {
            loadEmployee();
        }

        joinDatePicker.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                joinDatePicker.setValue(
                        joinDatePicker.getValue() != null
                                ? joinDatePicker.getValue().plusDays(1)
                                : java.time.LocalDate.now()
                );
            } else if (event.getDeltaY() < 0) {
                joinDatePicker.setValue(
                        joinDatePicker.getValue() != null
                                ? joinDatePicker.getValue().minusDays(1)
                                : java.time.LocalDate.now()
                );
            }
            event.consume();
        });

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
