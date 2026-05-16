package ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StaffDashboardUI {

    public static void show(Stage stage) {

        Label title =
                new Label("STAFF DASHBOARD");



        Button passSlipButton =
                new Button("Pass Slip Issuance");



        Button returnButton =
                new Button("Return Employee");



        Button logoutButton =
                new Button("Logout");



        // OPEN PASS SLIP
        passSlipButton.setOnAction(e -> {

            PassSlipUI.show(stage);

        });



        // OPEN RETURN
        returnButton.setOnAction(e -> {

            ReturnUI.show(stage);

        });



        // LOGOUT
        logoutButton.setOnAction(e -> {

            LoginUI.show(stage);

        });



        VBox root =
                new VBox(15);

        root.setAlignment(Pos.CENTER);



        root.getChildren().addAll(
                title,
                passSlipButton,
                returnButton,
                logoutButton
        );



        Scene scene =
                new Scene(root, 500, 400);

        stage.setTitle("Staff Dashboard");

        stage.setScene(scene);

        stage.show();

    }

}