package controllers;

import database.DatabaseConnection;
import models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeDetailsController {

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
                        resultSet.getString("join_date")
                );

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

}
