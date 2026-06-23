package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.Employee;
import utils.NavigationHelper;
import utils.Session;
import utils.SidebarHelper;

import java.time.LocalDate;
import java.time.YearMonth;

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
    private VBox joinDateCalendar;

    @FXML
    private TextField joinDateField;

    @FXML
    private Label btnCalPrevMonth;

    @FXML
    private Label btnCalNextMonth;

    @FXML
    private Label btnCalPrevYear;

    @FXML
    private Label btnCalNextYear;

    @FXML
    private Label lblCalMonthYear;

    @FXML
    private GridPane gridJoinDateCalendar;

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
    private Button btnSidebarRequests;

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

    private YearMonth calYearMonth = YearMonth.now();
    private LocalDate selectedJoinDate;

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, btnNotificationsAlert,
                btnSidebarAddEmployee, btnThemeToggle
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

        btnCalPrevMonth.setOnMouseClicked(e -> {
            calYearMonth = calYearMonth.minusMonths(1);
            loadJoinDateCalendar();
        });

        btnCalNextMonth.setOnMouseClicked(e -> {
            calYearMonth = calYearMonth.plusMonths(1);
            loadJoinDateCalendar();
        });

        btnCalPrevYear.setOnMouseClicked(e -> {
            calYearMonth = calYearMonth.minusYears(1);
            loadJoinDateCalendar();
        });

        btnCalNextYear.setOnMouseClicked(e -> {
            calYearMonth = calYearMonth.plusYears(1);
            loadJoinDateCalendar();
        });

        joinDateField.setOnMouseClicked(e -> {
            joinDateCalendar.setVisible(!joinDateCalendar.isVisible());
            joinDateCalendar.setManaged(!joinDateCalendar.isManaged());
        });

        loadJoinDateCalendar();

        btnSaveEmployee.setOnAction(
                event -> saveEmployee()
        );

        btnBack.setOnAction(
                event -> NavigationHelper.goBack(btnBack)
        );

    }

    private void loadJoinDateCalendar() {
        lblCalMonthYear.setText(calYearMonth.getMonth().name() + " " + calYearMonth.getYear());
        gridJoinDateCalendar.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstOfMonth = calYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = calYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            final int dayOfMonth = day;
            LocalDate date = calYearMonth.atDay(dayOfMonth);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(30);
            dayLabel.setPrefHeight(22);
            dayLabel.getStyleClass().add("cal-day");

            if (date.equals(LocalDate.now())) {
                dayLabel.getStyleClass().add("cal-today");
            }

            if (date.equals(selectedJoinDate)) {
                dayLabel.getStyleClass().add("cal-selected");
            }

            if (date.isAfter(LocalDate.now())) {
                dayLabel.setOpacity(0.3);
                dayLabel.getStyleClass().remove("cal-day");
                dayLabel.getStyleClass().add("cal-day-disabled");
            } else {
                dayLabel.setOnMouseClicked(event -> {
                    selectedJoinDate = calYearMonth.atDay(dayOfMonth);
                    joinDateField.setText((selectedJoinDate.getMonthValue()) + "/" + selectedJoinDate.getDayOfMonth() + "/" + selectedJoinDate.getYear());
                    joinDateCalendar.setVisible(false);
                    joinDateCalendar.setManaged(false);
                    loadJoinDateCalendar();
                });
            }

            gridJoinDateCalendar.add(dayLabel, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
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

        if (employee.getJoinDate() != null) {
            selectedJoinDate = employee.getJoinDate();
            calYearMonth = YearMonth.from(selectedJoinDate);
            joinDateField.setText((selectedJoinDate.getMonthValue()) + "/" + selectedJoinDate.getDayOfMonth() + "/" + selectedJoinDate.getYear());
            loadJoinDateCalendar();
        }

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
                selectedJoinDate,
                managerField.getText(),
                emailField.getText(),
                addressField.getText(),
                emergencyContactField.getText(),
                messageLabel
        );

    }
}
