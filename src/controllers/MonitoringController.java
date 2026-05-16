package controllers;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.time.Duration;
import java.time.LocalDateTime;

public class MonitoringController {

    public static void updateExpiredPassSlips() {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT id, time_out
                    FROM pass_slips
                    WHERE status = 'OUT'
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                int passSlipId =
                        resultSet.getInt("id");

                Timestamp timeOutTimestamp =
                        resultSet.getTimestamp("time_out");

                LocalDateTime timeOut =
                        timeOutTimestamp.toLocalDateTime();

                LocalDateTime now =
                        LocalDateTime.now();

                long hours =
                        Duration.between(timeOut, now).toHours();

                if(hours >= 1) {

                    String updateQuery = """
                            UPDATE pass_slips
                            SET status = 'EXPIRED'
                            WHERE id = ?
                            """;

                    PreparedStatement updateStatement =
                            connection.prepareStatement(updateQuery);

                    updateStatement.setInt(1, passSlipId);

                    updateStatement.executeUpdate();

                }

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static String getMonitoringData() {

        updateExpiredPassSlips();

        StringBuilder data = new StringBuilder();

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query = """
                    SELECT
                    pass_slips.id,
                    employees.first_name,
                    employees.last_name,
                    pass_slips.reason,
                    pass_slips.time_out,
                    pass_slips.time_in,
                    pass_slips.duration,
                    pass_slips.status
                    FROM pass_slips
                    JOIN employees
                    ON pass_slips.employee_id = employees.id
                    ORDER BY pass_slips.id DESC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                data.append("PASS SLIP ID: ")
                        .append(resultSet.getInt("id"))
                        .append("\n");

                data.append("EMPLOYEE: ")
                        .append(resultSet.getString("first_name"))
                        .append(" ")
                        .append(resultSet.getString("last_name"))
                        .append("\n");

                data.append("REASON: ")
                        .append(resultSet.getString("reason"))
                        .append("\n");

                data.append("TIME OUT: ")
                        .append(resultSet.getString("time_out"))
                        .append("\n");

                data.append("TIME IN: ")
                        .append(resultSet.getString("time_in"))
                        .append("\n");

                data.append("DURATION: ")
                        .append(resultSet.getString("duration"))
                        .append("\n");

                data.append("STATUS: ")
                        .append(resultSet.getString("status"))
                        .append("\n");

                data.append("\n-----------------------------------\n\n");

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return data.toString();

    }

}