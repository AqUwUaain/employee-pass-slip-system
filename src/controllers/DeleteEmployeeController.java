package controllers;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DeleteEmployeeController {

    public static boolean deleteEmployee(int employeeId) {

        try {

            Connection connection =
                    DatabaseConnection.connect();



            // DELETE ACTIVITY LOGS FIRST
            String deleteLogsQuery =
                    "DELETE FROM activity_logs WHERE employee_id = ?";

            PreparedStatement logsStatement =
                    connection.prepareStatement(
                            deleteLogsQuery
                    );

            logsStatement.setInt(1, employeeId);

            logsStatement.executeUpdate();



            // DELETE PASS SLIPS
            String deletePassSlipsQuery =
                    "DELETE FROM pass_slips WHERE employee_id = ?";

            PreparedStatement passSlipStatement =
                    connection.prepareStatement(
                            deletePassSlipsQuery
                    );

            passSlipStatement.setInt(1, employeeId);

            passSlipStatement.executeUpdate();



            // DELETE EMPLOYEE
            String deleteEmployeeQuery =
                    "DELETE FROM employees WHERE id = ?";

            PreparedStatement employeeStatement =
                    connection.prepareStatement(
                            deleteEmployeeQuery
                    );

            employeeStatement.setInt(1, employeeId);

            int rowsAffected =
                    employeeStatement.executeUpdate();



            if(rowsAffected > 0) {

                // ACTIVITY LOG
                ActivityLogController.logActivity(
                        "Deleted Employee ID: "
                                + employeeId
                );

            }



            return rowsAffected > 0;

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return false;

    }

}