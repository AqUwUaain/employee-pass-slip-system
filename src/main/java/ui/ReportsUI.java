package ui;

import controllers.ReportsController;

import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import models.ActivityLog;

import utils.Session;

public class ReportsUI {

    public static void show(Stage stage) {

        Label title =
                new Label("REPORTS");



        Label totalEmployeesLabel =
                new Label();

        Label totalPassSlipsLabel =
                new Label();

        Label totalOutLabel =
                new Label();

        Label totalReturnedLabel =
                new Label();

        Label totalExpiredLabel =
                new Label();



        ReportsController.loadReports(

                totalEmployeesLabel,

                totalPassSlipsLabel,

                totalOutLabel,

                totalReturnedLabel,

                totalExpiredLabel

        );




        // ACTIVITY LOG TABLE
        TableView<ActivityLog> table =
                new TableView<>();



        // ID COLUMN
        TableColumn<ActivityLog, Integer> idColumn =
                new TableColumn<>("ID");

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );



        // USERNAME COLUMN
        TableColumn<ActivityLog, String> usernameColumn =
                new TableColumn<>("Username");

        usernameColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );



        // ACTION COLUMN
        TableColumn<ActivityLog, String> actionColumn =
                new TableColumn<>("Action");

        actionColumn.setCellValueFactory(
                new PropertyValueFactory<>("action")
        );



        // DATE COLUMN
        TableColumn<ActivityLog, String> dateColumn =
                new TableColumn<>("Date");

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdAt")
        );



        table.getColumns().addAll(

                idColumn,

                usernameColumn,

                actionColumn,

                dateColumn

        );



        table.setItems(
                ReportsController.getLogs()
        );




        Button refreshButton =
                new Button("Refresh");



        refreshButton.setOnAction(e -> {

            ReportsController.loadReports(

                    totalEmployeesLabel,

                    totalPassSlipsLabel,

                    totalOutLabel,

                    totalReturnedLabel,

                    totalExpiredLabel

            );



            table.setItems(
                    ReportsController.getLogs()
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

                totalEmployeesLabel,

                totalPassSlipsLabel,

                totalOutLabel,

                totalReturnedLabel,

                totalExpiredLabel,

                table,

                refreshButton,

                backButton

        );




        Scene scene =
                new Scene(root, 900, 600);




        stage.setTitle("Reports");

        stage.setScene(scene);

        stage.show();

    }

}
