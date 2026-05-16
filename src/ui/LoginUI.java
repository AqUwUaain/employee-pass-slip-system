package ui;
import ui.LoginUI;
import controllers.LoginController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.DashboardUI;

public class LoginUI {

    public static void show(Stage stage) {

        Label title = new Label("EMPLOYEE PASS SLIP SYSTEM");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");

        Label messageLabel = new Label();

        loginButton.setOnAction(e -> {

            String username = usernameField.getText();
            String password = passwordField.getText();

            boolean success = LoginController.login(username, password);

            if (success) {
                DashboardUI.show(stage);
            } else {
                messageLabel.setText("INVALID ACCOUNT");
            }
        });

        VBox root = new VBox(15);

        root.getChildren().addAll(
                title,
                usernameField,
                passwordField,
                loginButton,
                messageLabel
        );

        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("Login");

        stage.setScene(scene);

        stage.show();
    }
}
