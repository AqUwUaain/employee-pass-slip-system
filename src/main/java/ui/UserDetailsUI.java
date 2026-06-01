package ui;

import controllers.DeleteUserController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import database.DatabaseConnection;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDetailsUI {

    public static void show(Stage stage, int userId) {

        String username = "";
        String role = "";



        try {

            Connection connection =
                    DatabaseConnection.connect();

            String query =
                    "SELECT * FROM users WHERE id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setInt(1, userId);

            ResultSet resultSet =
                    statement.executeQuery();



            if(resultSet.next()) {

                username =
                        resultSet.getString("username");

                role =
                        resultSet.getString("role");

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }



        Label title =
                new Label("USER DETAILS");



        Label idLabel =
                new Label("ID: " + userId);



        Label usernameLabel =
                new Label("USERNAME: " + username);



        Label roleLabel =
                new Label("ROLE: " + role);



        Label messageLabel =
                new Label();



        Button deleteButton =
                new Button("Delete User");



        deleteButton.setOnAction(e -> {

            boolean deleted =
                    DeleteUserController
                            .deleteUser(userId);

            if(deleted) {

                UserListUI.show(stage);

            }
            else {

                messageLabel.setText(
                        "FAILED TO DELETE USER"
                );

            }

        });



        Button backButton =
                new Button("Back");



        // BACK BUTTON
        backButton.setOnAction(e -> {

            if(Session.currentRole.equals("ADMIN")) {

                AdminDashboardUI.show(stage);

            }
            else {

                StaffDashboardUI.show(stage);

            }

        });



        VBox root =
                new VBox(15);

        root.setAlignment(Pos.CENTER);



        root.getChildren().addAll(
                title,
                idLabel,
                usernameLabel,
                roleLabel,
                deleteButton,
                backButton,
                messageLabel
        );



        Scene scene =
                new Scene(root, 400, 400);

        stage.setTitle("User Details");

        stage.setScene(scene);

        stage.show();

    }

}
