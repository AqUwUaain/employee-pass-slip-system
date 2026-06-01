package controllers;

import database.DatabaseConnection;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.AdminDashboardUI;
import ui.StaffDashboardUI;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    public static void login(
            TextField usernameField,
            PasswordField passwordField,
            Label messageLabel,
            Stage stage
    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query =
                    "SELECT * FROM users " +
                            "WHERE username = ? " +
                            "AND password = ?";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(
                    1,
                    usernameField.getText()
            );

            statement.setString(
                    2,
                    passwordField.getText()
            );

            ResultSet resultSet =
                    statement.executeQuery();



            if(resultSet.next()) {

                String role =
                        resultSet.getString("role");



                // SAVE CURRENT SESSION
                Session.currentRole = role;

                Session.currentUsername =
                        usernameField.getText();



                // ACTIVITY LOG
                ActivityLogController.logActivity(
                        "User Logged In"
                );



                // ADMIN
                if(role.equalsIgnoreCase("ADMIN")) {

                    AdminDashboardUI.show(stage);

                }



                // STAFF
                else if(role.equalsIgnoreCase("STAFF")) {

                    StaffDashboardUI.show(stage);

                }

            }
            else {

                messageLabel.setText(
                        "INVALID LOGIN"
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