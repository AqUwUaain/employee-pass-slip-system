package ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardUI {

    public static void show(Stage stage) {

        Label title = new Label("ADMIN DASHBOARD");

        Button employeeButton = new Button("Employee Management");

        Button passSlipButton = new Button("Pass Slip Issuance");

        Button returnButton = new Button("Return Employee");

        Button monitoringButton = new Button("Monitoring");

        Button reportsButton = new Button("Reports");

        Button logoutButton = new Button("Logout");



        // OPEN EMPLOYEE MANAGEMENT
        employeeButton.setOnAction(e -> {

            EmployeeUI.show(stage);

        });



        // OPEN PASS SLIP UI
        passSlipButton.setOnAction(e -> {

            PassSlipUI.show(stage);

        });



        // OPEN RETURN UI
        returnButton.setOnAction(e -> {

            ReturnUI.show(stage);

        });



        // OPEN MONITORING UI
        monitoringButton.setOnAction(e -> {

            MonitoringUI.show(stage);

        });



        // OPEN REPORTS UI
        reportsButton.setOnAction(e -> {

            ReportsUI.show(stage);

        });



        // LOGOUT
        logoutButton.setOnAction(e -> {

            LoginUI.show(stage);

        });



        VBox root = new VBox(15);

        root.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                title,
                employeeButton,
                passSlipButton,
                returnButton,
                monitoringButton,
                reportsButton,
                logoutButton
        );

        Scene scene = new Scene(root, 500, 500);

        stage.setTitle("Dashboard");

        stage.setScene(scene);

        stage.show();

    }

}
