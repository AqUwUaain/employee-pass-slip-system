package ui;

import controllers.UserController;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import utils.Session;

public class UserUI {

    public static void show(Stage stage) {

        Label title =
                new Label("CREATE USER");



        TextField usernameField =
                new TextField();

        usernameField.setPromptText("Username");



        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText("Password");



        ComboBox<String> roleBox =
                new ComboBox<>();

        roleBox.setItems(
                FXCollections.observableArrayList(
                        "ADMIN",
                        "STAFF"
                )
        );

        roleBox.setPromptText("Select Role");



        Button createButton =
                new Button("Create User");



        Button backButton =
                new Button("Back");



        Label messageLabel =
                new Label();




        // CREATE USER
        createButton.setOnAction(e -> {

            UserController.createUser(
                    usernameField,
                    passwordField,
                    roleBox,
                    messageLabel
            );

        });




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
                usernameField,
                passwordField,
                roleBox,
                createButton,
                backButton,
                messageLabel
        );




        Scene scene =
                new Scene(root, 400, 400);

        stage.setTitle("Create User");

        stage.setScene(scene);

        stage.show();

    }

}