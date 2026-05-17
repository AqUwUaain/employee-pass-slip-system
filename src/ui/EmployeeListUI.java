package ui;

import controllers.EmployeeListController;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Employee;
import utils.Session;

public class EmployeeListUI {

    public static void show(Stage stage) {

        Label title =
                new Label("EMPLOYEE LIST");



        // SEARCH FIELD
        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Employee..."
        );



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



        // FILTERED LIST
        FilteredList<Employee> filteredData =
                new FilteredList<>(

                        EmployeeListController.getEmployees(),

                        b -> true
                );



        // SEARCH LISTENER
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    filteredData.setPredicate(employee -> {

                        // SHOW ALL IF EMPTY
                        if(newValue == null ||
                                newValue.isEmpty()) {

                            return true;

                        }

                        String keyword =
                                newValue.toLowerCase();



                        // SEARCH ID
                        if(String.valueOf(employee.getId())
                                .contains(keyword)) {

                            return true;

                        }



                        // SEARCH FIRST NAME
                        if(employee.getFirstName()
                                .toLowerCase()
                                .contains(keyword)) {

                            return true;

                        }



                        // SEARCH LAST NAME
                        if(employee.getLastName()
                                .toLowerCase()
                                .contains(keyword)) {

                            return true;

                        }



                        return false;

                    });

                });



        table.setItems(filteredData);



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
                searchField,
                table,
                refreshButton,
                backButton
        );



        Scene scene =
                new Scene(root, 700, 500);

        stage.setTitle("Employee List");

        stage.setScene(scene);

        stage.show();

    }

}