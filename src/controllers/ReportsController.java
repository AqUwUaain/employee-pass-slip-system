package controllers;

import database.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.Label;

import models.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportsController {

    public static void loadReports(

            Label totalEmployeesLabel,

            Label totalPassSlipsLabel,

            Label totalOutLabel,

            Label totalReturnedLabel,

            Label totalExpiredLabel

    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();




            // TOTAL EMPLOYEES
            String employeeQuery =
                    "SELECT COUNT(*) FROM employees";

            PreparedStatement employeeStatement =
                    connection.prepareStatement(employeeQuery);

            ResultSet employeeResult =
                    employeeStatement.executeQuery();

            if(employeeResult.next()) {

                totalEmployeesLabel.setText(
                        "TOTAL EMPLOYEES: "
                                + employeeResult.getInt(1)
                );

            }




            // TOTAL PASS SLIPS
            String passSlipQuery =
                    "SELECT COUNT(*) FROM pass_slips";

            PreparedStatement passSlipStatement =
                    connection.prepareStatement(passSlipQuery);

            ResultSet passSlipResult =
                    passSlipStatement.executeQuery();

            if(passSlipResult.next()) {

                totalPassSlipsLabel.setText(
                        "TOTAL PASS SLIPS: "
                                + passSlipResult.getInt(1)
                );

            }




            // TOTAL OUT
            String outQuery = """
                    SELECT COUNT(*)
                    FROM pass_slips
                    WHERE status = 'OUT'
                    """;

            PreparedStatement outStatement =
                    connection.prepareStatement(outQuery);

            ResultSet outResult =
                    outStatement.executeQuery();

            if(outResult.next()) {

                totalOutLabel.setText(
                        "TOTAL OUT: "
                                + outResult.getInt(1)
                );

            }




            // TOTAL RETURNED
            String returnedQuery = """
                    SELECT COUNT(*)
                    FROM pass_slips
                    WHERE status = 'RETURNED'
                    """;

            PreparedStatement returnedStatement =
                    connection.prepareStatement(returnedQuery);

            ResultSet returnedResult =
                    returnedStatement.executeQuery();

            if(returnedResult.next()) {

                totalReturnedLabel.setText(
                        "TOTAL RETURNED: "
                                + returnedResult.getInt(1)
                );

            }




            // TOTAL EXPIRED
            String expiredQuery = """
                    SELECT COUNT(*)
                    FROM pass_slips
                    WHERE status = 'EXPIRED'
                    """;

            PreparedStatement expiredStatement =
                    connection.prepareStatement(expiredQuery);

            ResultSet expiredResult =
                    expiredStatement.executeQuery();

            if(expiredResult.next()) {

                totalExpiredLabel.setText(
                        "TOTAL EXPIRED: "
                                + expiredResult.getInt(1)
                );

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

    }






    // GET ACTIVITY LOGS
    public static ObservableList<ActivityLog> getLogs() {

        ObservableList<ActivityLog> logs =
                FXCollections.observableArrayList();

        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query = """
                    SELECT *
                    FROM activity_logs
                    ORDER BY created_at DESC
                    """;



            PreparedStatement statement =
                    connection.prepareStatement(query);



            ResultSet resultSet =
                    statement.executeQuery();



            while(resultSet.next()) {

                ActivityLog log =
                        new ActivityLog(

                                resultSet.getInt("id"),

                                resultSet.getString("username"),

                                resultSet.getString("action"),

                                resultSet.getTimestamp("created_at")
                                        .toLocalDateTime()

                        );



                logs.add(log);

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return logs;

    }

}