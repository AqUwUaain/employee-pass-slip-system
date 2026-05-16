package ui;

import controllers.EmployeeController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmployeeUI {

    public static void show(Stage stage) {

        Label title = new Label("EMPLOYEE MANAGEMENT");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField departmentField = new TextField();
        departmentField.setPromptText("Department");

        TextField positionField = new TextField();
        positionField.setPromptText("Position");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact");

        DatePicker joinDatePicker = new DatePicker();

        Button saveButton = new Button("Save Employee");

        Button backButton = new Button("Back");

        Label messageLabel = new Label();



        // SAVE EMPLOYEE
        saveButton.setOnAction(e -> {

            EmployeeController.addEmployee(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    departmentField.getText(),
                    positionField.getText(),
                    contactField.getText(),
                    joinDatePicker.getValue(),
                    messageLabel
            );

        });



        // BACK TO DASHBOARD
        backButton.setOnAction(e -> {

            DashboardUI.show(stage);

        });



        VBox layout = new VBox(15);

        layout.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                title,
                firstNameField,
                lastNameField,
                departmentField,
                positionField,
                contactField,
                joinDatePicker,
                saveButton,
                backButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 500, 500);

        stage.setTitle("Employee Management");

        stage.setScene(scene);

        stage.show();

    }

}
