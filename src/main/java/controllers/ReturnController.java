package controllers;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.Duration;
import java.time.LocalDateTime;

public class ReturnController {

    public static boolean recordReturn(int passSlipId) {

        try {

            Connection connection = DatabaseConnection.connect();




            // GET TIME OUT
            String getQuery = """
                    SELECT time_out
                    FROM pass_slips
                    WHERE id = ?
                    """;

            PreparedStatement getStatement =
                    connection.prepareStatement(getQuery);

            getStatement.setInt(1, passSlipId);

            ResultSet resultSet = getStatement.executeQuery();




            if(resultSet.next()) {

                LocalDateTime timeOut =
                        resultSet.getTimestamp("time_out")
                                .toLocalDateTime();

                LocalDateTime timeIn =
                        LocalDateTime.now();




                // COMPUTE DURATION
                Duration duration =
                        Duration.between(timeOut, timeIn);

                long hours = duration.toHours();

                long minutes = duration.toMinutes() % 60;

                String durationText =
                        hours + " hrs " + minutes + " mins";




                // UPDATE PASS SLIP
                String updateQuery = """
                        UPDATE pass_slips
                        SET
                        time_in = ?,
                        duration = ?,
                        status = 'RETURNED'
                        WHERE id = ?
                        """;

                PreparedStatement updateStatement =
                        connection.prepareStatement(updateQuery);

                updateStatement.setTimestamp(
                        1,
                        java.sql.Timestamp.valueOf(timeIn)
                );

                updateStatement.setString(
                        2,
                        durationText
                );

                updateStatement.setInt(
                        3,
                        passSlipId
                );

                int updated =
                        updateStatement.executeUpdate();




                if(updated > 0) {

                    // ACTIVITY LOG
                    ActivityLogController.logActivity(
                            "Returned Pass Slip ID: "
                                    + passSlipId
                    );

                }




                return updated > 0;

            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return false;

    }

}