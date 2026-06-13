package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.Employee;
import utils.NavigationHelper;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EmployeeListController {

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
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnHamburgerMenuToggle;

    @FXML
    private VBox cardCreateEmployee;

    @FXML
    private Label lblTotalEmployees;

    @FXML
    private Label lblTotalDepartments;

    @FXML
    private TextField txtSearchEmployee;

    @FXML
    private Button btnFilterAll;

    @FXML
    private Button btnFilterEducation;

    @FXML
    private Button btnFilterIT;

    @FXML
    private Button btnFilterEngineering;

    @FXML
    private Button btnFilterHRM;

    @FXML
    private Button btnFilterAccountancy;

    @FXML
    private TableView<Employee> employeeTableView;

    @FXML
    private TableColumn<Employee, String> colEmployeeName;

    @FXML
    private TableColumn<Employee, String> colDepartment;

    @FXML
    private TableColumn<Employee, String> colPosition;

    @FXML
    private TableColumn<Employee, String> colAction;

    private FilteredList<Employee> filteredEmployees;

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

        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarUsers, "/fxml/User.fxml"));

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarLogReturn, "/fxml/Return.fxml"));
        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml"));
        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));
        if (btnNotificationsAlert != null)
            btnNotificationsAlert.setOnAction(e -> NavigationHelper.navigateTo(btnNotificationsAlert, "/fxml/ActivityLog.fxml"));
        if (btnHamburgerMenuToggle != null)
            btnHamburgerMenuToggle.setOnAction(e -> NavigationHelper.navigateTo(btnHamburgerMenuToggle, "/fxml/User.fxml"));

        cardCreateEmployee.setOnMouseClicked(
                event -> {
                    Session.selectedEmployeeId = 0;
                    NavigationHelper.navigateTo(
                            cardCreateEmployee,
                            "/fxml/EmployeeForm.fxml"
                    );
                }
        );

        colEmployeeName.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getFirstName()
                                + " "
                                + cellData.getValue().getLastName()
                )
        );

        colDepartment.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDepartment()
                )
        );

        colPosition.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getPosition()
                )
        );

        colAction.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper("View Profile")
        );

        employeeTableView.setOnMouseClicked(
                event -> {
                    Employee selectedEmployee =
                            employeeTableView.getSelectionModel()
                                    .getSelectedItem();

                    if (selectedEmployee != null
                            && event.getClickCount() >= 2) {
                        Session.selectedEmployeeId =
                                selectedEmployee.getId();

                        NavigationHelper.navigateTo(
                                employeeTableView,
                                "/fxml/EmployeeDetails.fxml"
                        );
                    }
                }
        );

        txtSearchEmployee.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        applyFilter(newValue, null)
        );

        btnFilterAll.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), null)
        );

        btnFilterEducation.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "Education")
        );

        btnFilterIT.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "IT")
        );

        btnFilterEngineering.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "Engineering")
        );

        btnFilterHRM.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "HRM")
        );

        btnFilterAccountancy.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "Accountancy")
        );

        loadEmployeesAsync();

    }

    private void loadEmployeesAsync() {

        Task<ObservableList<Employee>> task = new Task<>() {
            @Override
            protected ObservableList<Employee> call() {
                return getEmployees();
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<Employee> employees = task.getValue();
            filteredEmployees = new FilteredList<>(employees, employee -> true);
            employeeTableView.setItems(filteredEmployees);

            lblTotalEmployees.setText(String.valueOf(employees.size()));

            Set<String> departments = employees.stream()
                    .map(Employee::getDepartment)
                    .filter(value -> value != null && !value.isBlank())
                    .collect(Collectors.toSet());

            lblTotalDepartments.setText(String.valueOf(departments.size()));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public static ObservableList<Employee> getEmployees() {

        ObservableList<Employee> employeeList =
                FXCollections.observableArrayList();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query =
                    "SELECT * FROM employees ORDER BY id ASC";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                Employee employee = new Employee(
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

                employeeList.add(employee);

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return employeeList;

    }

    private void applyFilter(String keyword, String department) {

        if (filteredEmployees == null) return;

        filteredEmployees.setPredicate(employee -> {

            boolean matchesKeyword =
                    keyword == null
                            || keyword.isBlank()
                            || (employee.getFirstName()
                            + " "
                            + employee.getLastName())
                            .toLowerCase()
                            .contains(keyword.toLowerCase())
                            || String.valueOf(employee.getId())
                            .contains(keyword);

            boolean matchesDepartment =
                    department == null
                            || department.equalsIgnoreCase(
                            employee.getDepartment()
                    );

            return matchesKeyword && matchesDepartment;

        });

    }

}
