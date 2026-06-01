package controllers;

import database.DatabaseConnection;

import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class PassSlipController {

    public static void issuePassSlip(

            String employeeId,
            String reason,
            Label messageLabel

    ) {

        // EMPLOYEE VALIDATION
        if(employeeId == null || employeeId.isEmpty()) {

            messageLabel.setText(
                    "SELECT EMPLOYEE"
            );

            return;

        }



        // REASON VALIDATION
        if(reason == null || reason.trim().isEmpty()) {

            messageLabel.setText(
                    "REASON REQUIRED"
            );

            return;

        }



        // MINIMUM REASON LENGTH
        if(reason.trim().length() < 5) {

            messageLabel.setText(
                    "REASON TOO SHORT"
            );

            return;

        }



        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query =

                    "INSERT INTO pass_slips " +
                            "(employee_id, reason, time_out, status) " +
                            "VALUES (?, ?, ?, ?)";



            PreparedStatement statement =
                    connection.prepareStatement(query);



            statement.setInt(
                    1,
                    Integer.parseInt(employeeId)
            );

            statement.setString(
                    2,
                    reason.trim()
            );

            statement.setTimestamp(
                    3,
                    java.sql.Timestamp.valueOf(
                            LocalDateTime.now()
                    )
            );

            statement.setString(
                    4,
                    "OUT"
            );



            int inserted =
                    statement.executeUpdate();



            if(inserted > 0) {

                // ACTIVITY LOG
                ActivityLogController.logActivity(
                        "Issued Pass Slip for Employee ID: "
                                + employeeId
                );



                messageLabel.setText(
                        "PASS SLIP ISSUED"
                );

            }

            else {

                messageLabel.setText(
                        "FAILED TO ISSUE PASS SLIP"
                );

            }

        }

        catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "DATABASE ERROR"
            );

        }

    }

}
