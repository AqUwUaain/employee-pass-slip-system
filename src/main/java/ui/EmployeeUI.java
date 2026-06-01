package ui;

import controllers.EmployeeController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.Session;

public class EmployeeUI {

    public static void show(Stage stage) {

        Label title =
                new Label("EMPLOYEE MANAGEMENT");



        TextField firstNameField =
                new TextField();

        firstNameField.setPromptText(
                "First Name"
        );



        TextField lastNameField =
                new TextField();

        lastNameField.setPromptText(
                "Last Name"
        );



        TextField departmentField =
                new TextField();

        departmentField.setPromptText(
                "Department"
        );



        TextField positionField =
                new TextField();

        positionField.setPromptText(
                "Position"
        );



        TextField contactField =
                new TextField();

        contactField.setPromptText(
                "Contact Number"
        );



        DatePicker joinDatePicker =
                new DatePicker();




        Button saveButton =
                new Button("Save Employee");



        Button clearButton =
                new Button("Clear");



        Button backButton =
                new Button("Back");



        Label messageLabel =
                new Label();




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




        // CLEAR FIELDS
        clearButton.setOnAction(e -> {

            firstNameField.clear();

            lastNameField.clear();

            departmentField.clear();

            positionField.clear();

            contactField.clear();

            joinDatePicker.setValue(null);

            messageLabel.setText("");

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




        VBox layout =
                new VBox(15);

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

                clearButton,

                backButton,

                messageLabel

        );




        Scene scene =
                new Scene(layout, 500, 550);




        stage.setTitle(
                "Employee Management"
        );

        stage.setScene(scene);

        stage.show();

    }

}
