package ui;

import controllers.EmployeeListController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Employee;
import utils.Session;

public class EmployeeListUI {

    public static void show(Stage stage) {

        Label title =
                new Label("EMPLOYEE LIST");



        TableView<Employee> table =
                new TableView<>();



        // ID COLUMN
        TableColumn<Employee, Integer> idColumn =
                new TableColumn<>("ID");

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );



        // FIRST NAME COLUMN
        TableColumn<Employee, String> firstNameColumn =
                new TableColumn<>("First Name");

        firstNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("firstName")
        );



        // LAST NAME COLUMN
        TableColumn<Employee, String> lastNameColumn =
                new TableColumn<>("Last Name");

        lastNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("lastName")
        );



        table.getColumns().addAll(
                idColumn,
                firstNameColumn,
                lastNameColumn
        );



        table.setItems(
                EmployeeListController.getEmployees()
        );



        // CLICK EMPLOYEE ROW
        table.setOnMouseClicked(e -> {

            Employee selectedEmployee =
                    table.getSelectionModel()
                            .getSelectedItem();

            if(selectedEmployee != null) {

                EmployeeDetailsUI.show(
                        stage,
                        selectedEmployee.getId()
                );

            }

        });



        Button refreshButton =
                new Button("Refresh");



        refreshButton.setOnAction(e -> {

            table.setItems(
                    EmployeeListController.getEmployees()
            );

        });



        Button backButton =
                new Button("Back");



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
                table,
                refreshButton,
                backButton
        );



        Scene scene =
                new Scene(root, 600, 400);

        stage.setTitle("Employee List");

        stage.setScene(scene);

        stage.show();

    }

}
