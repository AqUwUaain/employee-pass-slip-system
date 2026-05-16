package controllers;

import database.DatabaseConnection;

import javafx.scene.control.Label;

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

}