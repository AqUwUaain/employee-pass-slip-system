package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    public static boolean show(javafx.scene.Node owner, String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initOwner(owner.getScene().getWindow());

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox dialog = new VBox(16);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPrefWidth(420);
        dialog.setMaxWidth(420);
        dialog.setPrefHeight(250);
        dialog.setMaxHeight(250);
        dialog.setStyle(
                "-fx-background-color: #1F1B1B; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #FCA5A5; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px; " +
                "-fx-padding: 30px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0.25, 0, 6);"
        );

        Label icon = new Label("\u26A0");
        icon.setStyle("-fx-font-size: 40px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F5F5F4;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #A8A29E; -fx-text-alignment: center; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(320);
        msgLabel.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle(
                "-fx-background-color: #3D3229; -fx-text-fill: #A8A29E; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 28px; -fx-cursor: hand;"
        );

        Button btnConfirm = new Button("Confirm");
        btnConfirm.setStyle(
                "-fx-background-color: #DC2626; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 8px 28px; -fx-cursor: hand;"
        );

        final boolean[] result = {false};

        btnCancel.setOnAction(e -> {
            result[0] = false;
            dialogStage.close();
        });

        btnConfirm.setOnAction(e -> {
            result[0] = true;
            dialogStage.close();
        });

        buttons.getChildren().addAll(btnCancel, btnConfirm);
        dialog.getChildren().addAll(icon, titleLabel, msgLabel, buttons);
        StackPane.setAlignment(dialog, Pos.CENTER);
        root.getChildren().add(dialog);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        Stage ownerStage = (Stage) owner.getScene().getWindow();
        dialogStage.setX(ownerStage.getX() + (ownerStage.getWidth() - 420) / 2);
        dialogStage.setY(ownerStage.getY() + (ownerStage.getHeight() - 250) / 2);

        dialogStage.showAndWait();

        return result[0];
    }
}
