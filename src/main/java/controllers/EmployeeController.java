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
import java.time.LocalDate;
import java.util.ArrayList;

public class EmployeeController {

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
    private VBox btnGatewayViewList;

    @FXML
    private VBox btnGatewayAddEmployee;

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

        btnGatewayViewList.setOnMouseClicked(
                event -> NavigationHelper.navigateTo(
                        btnGatewayViewList,
                        "/fxml/EmployeeList.fxml"
                )
        );

        btnGatewayAddEmployee.setOnMouseClicked(
                event -> {
                    Session.selectedEmployeeId = 0;
                    NavigationHelper.navigateTo(
                            btnGatewayAddEmployee,
                            "/fxml/EmployeeForm.fxml"
                    );
                }
        );

    }

    // ADD EMPLOYEE
    public static void addEmployee(

            String firstName,
            String lastName,
            String department,
            String position,
            String contact,
            LocalDate joinDate,
            String manager,
            String email,
            String address,
            String emergencyContact,
            Label messageLabel

    ) {

        if(firstName.isBlank() ||
                lastName.isBlank() ||
                department.isBlank() ||
                position.isBlank() ||
                contact.isBlank() ||
                joinDate == null) {

            messageLabel.setText("COMPLETE ALL FIELDS");
            return;

        }

        if(!contact.matches("[0-9]+")) {
            messageLabel.setText("CONTACT MUST BE NUMBERS ONLY");
            return;
        }

        if(contact.length() < 7) {
            messageLabel.setText("INVALID CONTACT NUMBER");
            return;
        }

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String sql = """
                    INSERT INTO employees
                    (first_name, last_name, department, position,
                     contact, join_date, manager, email, address, emergency_contact)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement preparedStatement =
                    connection.prepareStatement(sql);

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, department);
            preparedStatement.setString(4, position);
            preparedStatement.setString(5, contact);
            preparedStatement.setDate(6, java.sql.Date.valueOf(joinDate));
            preparedStatement.setString(7, manager != null ? manager : "");
            preparedStatement.setString(8, email != null ? email : "");
            preparedStatement.setString(9, address != null ? address : "");
            preparedStatement.setString(10, emergencyContact != null ? emergencyContact : "");

            preparedStatement.executeUpdate();

            ActivityLogController.logActivity(
                    "Added Employee: " + firstName + " " + lastName,
                    0
            );

            messageLabel.setText("EMPLOYEE ADDED SUCCESSFULLY");

        }

        catch (Exception e) {

            messageLabel.setText("FAILED TO ADD EMPLOYEE");
            e.printStackTrace();

        }

    }

    // UPDATE EMPLOYEE
    public static boolean updateEmployee(

            int id,
            String firstName,
            String lastName,
            String department,
            String position,
            String contact,
            String manager,
            String email,
            String address,
            String emergencyContact

    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    UPDATE employees
                    SET first_name = ?, last_name = ?, department = ?,
                        position = ?, contact = ?, manager = ?,
                        email = ?, address = ?, emergency_contact = ?
                    WHERE id = ?
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, department);
            statement.setString(4, position);
            statement.setString(5, contact);
            statement.setString(6, manager != null ? manager : "");
            statement.setString(7, email != null ? email : "");
            statement.setString(8, address != null ? address : "");
            statement.setString(9, emergencyContact != null ? emergencyContact : "");
            statement.setInt(10, id);

            int updated = statement.executeUpdate();

            if(updated > 0) {
                ActivityLogController.logActivity(
                        "Updated Employee ID: " + id,
                        id
                );
            }

            return updated > 0;

        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    // GET ALL EMPLOYEES
    public static ArrayList<Employee> getAllEmployees() {

        ArrayList<Employee> employeeList =
                new ArrayList<>();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = "SELECT * FROM employees";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                Employee employee =
                        new Employee(
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

}
