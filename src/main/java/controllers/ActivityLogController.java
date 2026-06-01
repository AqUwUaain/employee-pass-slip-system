package controllers;

import database.DatabaseConnection;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ActivityLogController {

    public static void logActivity(String action) {

        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query =
                    """
                    INSERT INTO activity_logs
                    (
                        username,
                        action
                    )
                    VALUES (?, ?)
                    """;



            PreparedStatement statement =
                    connection.prepareStatement(query);



            statement.setString(
                    1,
                    Session.currentUsername
            );



            statement.setString(
                    2,
                    action
            );



            statement.executeUpdate();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

}
