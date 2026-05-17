package controllers;

import database.DatabaseConnection;
import javafx.scene.control.Label;
import models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

public class EmployeeController {

    // ADD EMPLOYEE
    public static void addEmployee(

            String firstName,
            String lastName,
            String department,
            String position,
            String contact,
            LocalDate joinDate,
            Label messageLabel

    ) {

        // EMPTY FIELD VALIDATION
        if(firstName.isBlank() ||
                lastName.isBlank() ||
                department.isBlank() ||
                position.isBlank() ||
                contact.isBlank() ||
                joinDate == null) {

            messageLabel.setText(
                    "COMPLETE ALL FIELDS"
            );

            return;

        }



        // CONTACT VALIDATION
        if(!contact.matches("[0-9]+")) {

            messageLabel.setText(
                    "CONTACT MUST BE NUMBERS ONLY"
            );

            return;

        }



        // CONTACT LENGTH
        if(contact.length() < 7) {

            messageLabel.setText(
                    "INVALID CONTACT NUMBER"
            );

            return;

        }



        try {

            Connection connection =
                    DatabaseConnection.connect();



            String sql =
                    """
                    INSERT INTO employees
                    (
                    first_name,
                    last_name,
                    department,
                    position,
                    contact,
                    join_date
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;



            PreparedStatement preparedStatement =
                    connection.prepareStatement(sql);

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, department);
            preparedStatement.setString(4, position);
            preparedStatement.setString(5, contact);

            preparedStatement.setDate(
                    6,
                    java.sql.Date.valueOf(joinDate)
            );



            preparedStatement.executeUpdate();



            // ACTIVITY LOG
            ActivityLogController.logActivity(
                    "Added Employee: "
                            + firstName
                            + " "
                            + lastName
            );



            messageLabel.setText(
                    "EMPLOYEE ADDED SUCCESSFULLY"
            );

        }

        catch (Exception e) {

            messageLabel.setText(
                    "FAILED TO ADD EMPLOYEE"
            );

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
            String contact

    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query =
                    """
                    UPDATE employees
                    SET
                        first_name = ?,
                        last_name = ?,
                        department = ?,
                        position = ?,
                        contact = ?
                    WHERE id = ?
                    """;



            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, department);
            statement.setString(4, position);
            statement.setString(5, contact);
            statement.setInt(6, id);



            int updated =
                    statement.executeUpdate();



            if(updated > 0) {

                // ACTIVITY LOG
                ActivityLogController.logActivity(
                        "Updated Employee ID: " + id
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



            String query =
                    "SELECT * FROM employees";



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

                                resultSet.getDate("join_date")
                                        .toLocalDate()

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