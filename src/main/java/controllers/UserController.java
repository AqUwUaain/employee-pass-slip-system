package controllers;

import database.DatabaseConnection;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserController {

    public static void createUser(

            TextField usernameField,
            PasswordField passwordField,
            ComboBox<String> roleBox,
            Label messageLabel

    ) {

        String username =
                usernameField.getText().trim();

        String password =
                passwordField.getText().trim();

        String role =
                roleBox.getValue();



        // USERNAME VALIDATION
        if(username.isEmpty()) {

            messageLabel.setText(
                    "USERNAME REQUIRED"
            );

            return;

        }



        // PASSWORD VALIDATION
        if(password.isEmpty()) {

            messageLabel.setText(
                    "PASSWORD REQUIRED"
            );

            return;

        }



        // PASSWORD LENGTH
        if(password.length() < 4) {

            messageLabel.setText(
                    "PASSWORD TOO SHORT"
            );

            return;

        }



        // ROLE VALIDATION
        if(role == null) {

            messageLabel.setText(
                    "SELECT ROLE"
            );

            return;

        }



        try {

            Connection connection =
                    DatabaseConnection.connect();



            // CHECK DUPLICATE USERNAME
            String checkQuery =
                    "SELECT * FROM users WHERE username = ?";

            PreparedStatement checkStatement =
                    connection.prepareStatement(checkQuery);

            checkStatement.setString(
                    1,
                    username
            );

            ResultSet resultSet =
                    checkStatement.executeQuery();



            if(resultSet.next()) {

                messageLabel.setText(
                        "USERNAME ALREADY EXISTS"
                );

                return;

            }



            // INSERT USER
            String insertQuery =

                    "INSERT INTO users " +
                            "(username, password, role) " +
                            "VALUES (?, ?, ?)";



            PreparedStatement statement =
                    connection.prepareStatement(insertQuery);



            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    password
            );

            statement.setString(
                    3,
                    role
            );



            int inserted =
                    statement.executeUpdate();




            if(inserted > 0) {

                // ACTIVITY LOG
                ActivityLogController.logActivity(
                        "Created User: "
                                + username
                );



                messageLabel.setText(
                        "USER CREATED"
                );



                // CLEAR FIELDS
                usernameField.clear();

                passwordField.clear();

                roleBox.setValue(null);

            }

            else {

                messageLabel.setText(
                        "FAILED TO CREATE USER"
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