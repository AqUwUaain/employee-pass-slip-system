package ui;

import controllers.ReportsController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import utils.Session;

public class ReportsUI {

    public static void show(Stage stage) {

        Label title = new Label("REPORTS");

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
                refreshButton,
                backButton
        );



        Scene scene =
                new Scene(root, 500, 400);

        stage.setTitle("Reports");

        stage.setScene(scene);

        stage.show();

    }

}
