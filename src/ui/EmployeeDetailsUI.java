package ui;

import controllers.DeleteEmployeeController;
import controllers.EmployeeDetailsController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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



        Label nameLabel =
                new Label(
                        "NAME: "
                                + employee.getFirstName()
                                + " "
                                + employee.getLastName()
                );



        Label departmentLabel =
                new Label(
                        "DEPARTMENT: "
                                + employee.getDepartment()
                );



        Label positionLabel =
                new Label(
                        "POSITION: "
                                + employee.getPosition()
                );



        Label contactLabel =
                new Label(
                        "CONTACT: "
                                + employee.getContact()
                );



        Label joinDateLabel =
                new Label(
                        "JOIN DATE: "
                                + employee.getJoinDate()
                );



        Label messageLabel =
                new Label();



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



        Button backButton =
                new Button("Back");



        // BACK BUTTON
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
                nameLabel,
                departmentLabel,
                positionLabel,
                contactLabel,
                joinDateLabel,
                deleteButton,
                backButton,
                messageLabel
        );



        Scene scene =
                new Scene(root, 500, 500);

        stage.setTitle("Employee Details");

        stage.setScene(scene);

        stage.show();

    }

}
