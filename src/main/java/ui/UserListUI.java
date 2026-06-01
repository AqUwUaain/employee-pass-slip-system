package ui;

import controllers.UserListController;
import models.User;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import utils.Session;

public class UserListUI {

    public static void show(Stage stage) {

        Label title =
                new Label("USER LIST");



        TableView<User> table =
                new TableView<>();



        // ID COLUMN
        TableColumn<User, Integer> idColumn =
                new TableColumn<>("ID");

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );



        // USERNAME COLUMN
        TableColumn<User, String> usernameColumn =
                new TableColumn<>("Username");

        usernameColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );



        // ROLE COLUMN
        TableColumn<User, String> roleColumn =
                new TableColumn<>("Role");

        roleColumn.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );



        table.getColumns().addAll(
                idColumn,
                usernameColumn,
                roleColumn
        );



        table.setItems(
                UserListController.getUsers()
        );



        // CLICK USER ROW
        table.setOnMouseClicked(e -> {

            User selectedUser =
                    table.getSelectionModel()
                            .getSelectedItem();

            if(selectedUser != null) {

                UserDetailsUI.show(
                        stage,
                        selectedUser.getId()
                );

            }

        });



        Button addUserButton =
                new Button("Add User");



        addUserButton.setOnAction(e -> {

            UserUI.show(stage);

        });




        Button refreshButton =
                new Button("Refresh");



        refreshButton.setOnAction(e -> {

            table.setItems(
                    UserListController.getUsers()
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
                addUserButton,
                refreshButton,
                backButton
        );




        Scene scene =
                new Scene(root, 600, 500);

        stage.setTitle("User List");

        stage.setScene(scene);

        stage.show();

    }

}
