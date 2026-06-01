package ui;

import controllers.MonitoringController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import utils.Session;

public class MonitoringUI {

    public static void show(Stage stage) {

        Label title = new Label("MONITORING SYSTEM");

        TextArea monitoringArea = new TextArea();

        monitoringArea.setEditable(false);

        monitoringArea.setPrefHeight(300);

        monitoringArea.setText(
                MonitoringController.getMonitoringData()
        );



        Button refreshButton = new Button("Refresh");

        Button backButton = new Button("Back");




        // REFRESH DATA
        refreshButton.setOnAction(e -> {

            monitoringArea.setText(
                    MonitoringController.getMonitoringData()
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
                monitoringArea,
                refreshButton,
                backButton
        );

        Scene scene = new Scene(root, 700, 500);

        stage.setTitle("Monitoring");

        stage.setScene(scene);

        stage.show();

    }

}