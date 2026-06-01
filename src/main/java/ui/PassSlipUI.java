package ui;

import controllers.EmployeeController;
import controllers.PassSlipController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import models.Employee;
import utils.Session;

public class PassSlipUI {

    public static void show(Stage stage) {

        Label title = new Label("PASS SLIP ISSUANCE");



        ComboBox<Employee> employeeComboBox =
                new ComboBox<>();

        employeeComboBox.getItems().addAll(
                EmployeeController.getAllEmployees()
        );

        employeeComboBox.setPromptText(
                "Select Employee"
        );



        TextArea reasonArea = new TextArea();

        reasonArea.setPromptText("Reason");



        Button issueButton =
                new Button("Issue Pass Slip");

        Button backButton =
                new Button("Back");

        Label messageLabel =
                new Label();




        // ISSUE PASS SLIP
        issueButton.setOnAction(e -> {

            Employee selectedEmployee =
                    employeeComboBox.getValue();

            if(selectedEmployee == null) {

                messageLabel.setText(
                        "SELECT EMPLOYEE"
                );

                return;

            }

            int employeeId =
                    selectedEmployee.getId();

            PassSlipController.issuePassSlip(
                    String.valueOf(employeeId),
                    reasonArea.getText(),
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




        VBox root = new VBox(15);

        root.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                title,
                employeeComboBox,
                reasonArea,
                issueButton,
                backButton,
                messageLabel
        );

        Scene scene = new Scene(root, 500, 400);

        stage.setTitle("Pass Slip");

        stage.setScene(scene);

        stage.show();

    }

}