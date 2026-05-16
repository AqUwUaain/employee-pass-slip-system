package controllers;

import database.DatabaseConnection;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserController {

    public static void createUser(
            TextField usernameField,
            PasswordField passwordField,
            ComboBox<String> roleBox,
            Label messageLabel
    ) {

        try {

            String username =
                    usernameField.getText();

            String password =
                    passwordField.getText();

            String role =
                    roleBox.getValue();




            // VALIDATION
            if(username.isEmpty() ||
                    password.isEmpty() ||
                    role == null) {

                messageLabel.setText(
                        "COMPLETE ALL FIELDS"
                );

                return;
            }




            Connection connection =
                    DatabaseConnection.connect();




            String query =
                    """
                    INSERT INTO users
                    (username, password, role)
                    VALUES (?, ?, ?)
                    """;




            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);




            statement.executeUpdate();




            messageLabel.setText(
                    "USER CREATED SUCCESSFULLY"
            );




            usernameField.clear();
            passwordField.clear();
            roleBox.setValue(null);

        }

        catch (Exception e) {

            messageLabel.setText(
                    "FAILED TO CREATE USER"
            );

            e.printStackTrace();

        }

    }

}