package ui;

import controllers.LoginController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginUI {

    public static void show(Stage stage) {

        Label title =
                new Label("EMPLOYEE PASS SLIP SYSTEM");



        TextField usernameField =
                new TextField();

        usernameField.setPromptText("Username");



        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText("Password");



        Button loginButton =
                new Button("Login");



        Label messageLabel =
                new Label();




        loginButton.setOnAction(e -> {

            LoginController.login(
                    usernameField,
                    passwordField,
                    messageLabel,
                    stage
            );

        });




        VBox root =
                new VBox(15);

        root.setAlignment(Pos.CENTER);



        root.getChildren().addAll(
                title,
                usernameField,
                passwordField,
                loginButton,
                messageLabel
        );



        Scene scene =
                new Scene(root, 400, 300);

        stage.setTitle("Login");

        stage.setScene(scene);

        stage.show();

    }

}