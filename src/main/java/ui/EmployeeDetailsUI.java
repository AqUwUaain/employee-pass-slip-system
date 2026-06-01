package ui;

import controllers.DeleteEmployeeController;
import controllers.EmployeeController;
import controllers.EmployeeDetailsController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Employee;
import utils.Session;

public class EmployeeDetailsUI {

    public static void show(Stage stage, int employeeId) {

        Employee employee =
                EmployeeDetailsController
                        .getEmployeeById(employeeId);



        Label title =
                new Label("EMPLOYEE DETAILS");



        Label idLabel =
                new Label("ID: " + employee.getId());



        // EDITABLE FIELDS
        TextField firstNameField =
                new TextField(
                        employee.getFirstName()
                );



        TextField lastNameField =
                new TextField(
                        employee.getLastName()
                );



        TextField departmentField =
                new TextField(
                        employee.getDepartment()
                );



        TextField positionField =
                new TextField(
                        employee.getPosition()
                );



        TextField contactField =
                new TextField(
                        employee.getContact()
                );



        Label joinDateLabel =
                new Label(
                        "JOIN DATE: "
                                + employee.getJoinDate()
                );



        Label messageLabel =
                new Label();




        // UPDATE BUTTON
        Button updateButton =
                new Button("Update Employee");



        updateButton.setOnAction(e -> {

            boolean updated =
                    EmployeeController.updateEmployee(

                            employee.getId(),

                            firstNameField.getText(),

                            lastNameField.getText(),

                            departmentField.getText(),

                            positionField.getText(),

                            contactField.getText()

                    );



            if(updated) {

                messageLabel.setText(
                        "EMPLOYEE UPDATED SUCCESSFULLY"
                );

            }
            else {

                messageLabel.setText(
                        "FAILED TO UPDATE EMPLOYEE"
                );

            }

        });




        // DELETE BUTTON
        Button deleteButton =
                new Button("Delete Employee");



        deleteButton.setOnAction(e -> {

            boolean deleted =
                    DeleteEmployeeController
                            .deleteEmployee(
                                    employee.getId()
                            );

            if(deleted) {

                EmployeeListUI.show(stage);

            }
            else {

                messageLabel.setText(
                        "FAILED TO DELETE EMPLOYEE"
                );

            }

        });




        // BACK BUTTON
        Button backButton =
                new Button("Back");



        backButton.setOnAction(e -> {

            if(Session.currentRole.equals("ADMIN")) {

                EmployeeListUI.show(stage);

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

                new Label("First Name"),
                firstNameField,

                new Label("Last Name"),
                lastNameField,

                new Label("Department"),
                departmentField,

                new Label("Position"),
                positionField,

                new Label("Contact"),
                contactField,

                joinDateLabel,

                updateButton,

                deleteButton,

                backButton,

                messageLabel

        );




        Scene scene =
                new Scene(root, 500, 700);

        stage.setTitle("Employee Details");

        stage.setScene(scene);

        stage.show();

    }

}
