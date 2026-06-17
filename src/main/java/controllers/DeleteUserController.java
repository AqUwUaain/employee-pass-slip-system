package controllers;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DeleteUserController {

    public static boolean deleteUser(int userId) {

        try (Connection connection = DatabaseConnection.connect()) {

            String query =
                    "DELETE FROM users WHERE id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setInt(1, userId);

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected > 0) {
                ActivityLogController.logActivity("Deleted User ID: " + userId, 0);
            }

            return rowsAffected > 0;

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return false;

    }

}