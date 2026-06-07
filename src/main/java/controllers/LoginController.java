package controllers;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.NavigationHelper;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {

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
                    emailField.getText()
            );

            statement.setString(
                    2,
                    passwordField.getText()
            );

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                Session.currentUserId =
                        resultSet.getInt("id");

                String role =
                        resultSet.getString("role");

                Session.currentRole = role;

                Session.currentUsername =
                        emailField.getText();

                ActivityLogController.logActivity(
                        "User Logged In"
                );

                if (role.equalsIgnoreCase("ADMIN")) {

                    NavigationHelper.navigateTo(
                            emailField,
                            "/fxml/Dashboard.fxml"
                    );

                }
                else if (role.equalsIgnoreCase("STAFF")) {

                    NavigationHelper.navigateTo(
                            emailField,
                            "/fxml/StaffDashboard.fxml"
                    );

                }
                else {

                    messageLabel.setText(
                            "Unknown user role."
                    );

                }

            }
            else {

                messageLabel.setText(
                        "Invalid username or password."
                );

            }

        }
        catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "Database error."
            );

        }

    }
}
