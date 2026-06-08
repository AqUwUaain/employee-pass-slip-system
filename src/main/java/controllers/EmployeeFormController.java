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
    private Label messageLabel;

    @FXML
    private Button btnSaveEmployee;

    @FXML
    private Button btnBack;

    @FXML
    private void initialize() {

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
