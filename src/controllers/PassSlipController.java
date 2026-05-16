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

        try {

            Connection connection = DatabaseConnection.connect();

            String sql = "INSERT INTO pass_slips " +
                    "(employee_id, reason, time_out, status) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement preparedStatement =
                    connection.prepareStatement(sql);

            preparedStatement.setInt(
                    1,
                    Integer.parseInt(employeeId)
            );

            preparedStatement.setString(2, reason);

            preparedStatement.setTimestamp(
                    3,
                    java.sql.Timestamp.valueOf(LocalDateTime.now())
            );

            preparedStatement.setString(4, "OUT");

            preparedStatement.executeUpdate();



            // ACTIVITY LOG

            String logSql =
                    "INSERT INTO activity_logs " +
                            "(action, employee_id) VALUES (?, ?)";

            PreparedStatement logStatement =
                    connection.prepareStatement(logSql);

            logStatement.setString(
                    1,
                    "PASS SLIP ISSUED"
            );

            logStatement.setInt(
                    2,
                    Integer.parseInt(employeeId)
            );

            logStatement.executeUpdate();



            messageLabel.setText(
                    "PASS SLIP ISSUED SUCCESSFULLY"
            );

        }

        catch (Exception e) {

            messageLabel.setText(
                    "FAILED TO ISSUE PASS SLIP"
            );

            e.printStackTrace();

        }

    }

}
