package ui;

import controllers.ReturnController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import utils.Session;

public class ReturnUI {

    public static void show(Stage stage) {

        Label title = new Label("EMPLOYEE RETURN");

        TextField passSlipIdField = new TextField();

        passSlipIdField.setPromptText("Pass Slip ID");

        Button returnButton = new Button("Record Return");

        Label messageLabel = new Label();

        Button backButton = new Button("Back");




        // RECORD RETURN
        returnButton.setOnAction(e -> {

            try {

                int passSlipId =
                        Integer.parseInt(
                                passSlipIdField.getText()
                        );

                boolean success =
                        ReturnController.recordReturn(passSlipId);

                if(success) {

                    messageLabel.setText(
                            "EMPLOYEE RETURN RECORDED"
                    );

                }
                else {

                    messageLabel.setText(
                            "PASS SLIP NOT FOUND"
                    );

                }

            }
            catch (Exception ex) {

                messageLabel.setText(
                        "INVALID INPUT"
                );

            }

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
                passSlipIdField,
                returnButton,
                messageLabel,
                backButton
        );

        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("Return System");

        stage.setScene(scene);

        stage.show();

    }

}
