package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeListController {

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
                        resultSet.getString("last_name")
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
