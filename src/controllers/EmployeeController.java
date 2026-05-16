package controllers;

import database.DatabaseConnection;

import javafx.scene.control.Label;

import models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class EmployeeController {

    public static void addEmployee(
            String firstName,
            String lastName,
            String department,
            String position,
            String contact,
            LocalDate joinDate,
            Label messageLabel
    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String sql = """
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



    // LOAD EMPLOYEES
    public static List<Employee> getAllEmployees() {

        List<Employee> employees =
                new ArrayList<>();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT *
                    FROM employees
                    ORDER BY first_name ASC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                Employee employee =
                        new Employee(

                                resultSet.getInt("id"),

                                resultSet.getString("first_name"),

                                resultSet.getString("last_name")

                        );

                employees.add(employee);

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return employees;

    }

}
