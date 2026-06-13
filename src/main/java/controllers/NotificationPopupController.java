package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.ActivityLog;
import utils.NavigationHelper;

import utils.PhilTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationPopupController {

    @FXML
    private VBox notificationPopup;

    @FXML
    private VBox vboxNotifications;

    @FXML
    private Button btnViewAllNotifications;

    @FXML
    private void initialize() {
        btnViewAllNotifications.setOnAction(e -> {
            e.consume();
            NavigationHelper.navigateTo(btnViewAllNotifications, "/fxml/ActivityLog.fxml");
        });

        loadLatestActivities();
    }

    private void loadLatestActivities() {
        vboxNotifications.getChildren().clear();

        List<ActivityLog> logs = ReportsController.getLogs(5);

        if (logs.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 30 0;");
            Label noAct = new Label("No recent activities");
            noAct.setStyle("-fx-text-fill: #78716C; -fx-font-size: 13px;");
            emptyBox.getChildren().add(noAct);
            vboxNotifications.getChildren().add(emptyBox);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");

        for (ActivityLog log : logs) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10px 12px; -fx-background-radius: 8px;");
            row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 10px 12px; -fx-background-color: rgba(212,168,83,0.08); -fx-background-radius: 8px;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-padding: 10px 12px; -fx-background-radius: 8px;"));

            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #D4A853; -fx-font-size: 10px;");

            VBox textBox = new VBox(2);
            Label actionLabel = new Label(log.getAction());
            actionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #E7E5E4; -fx-font-weight: bold;");
            Label timeLabel = new Label(log.getTimestamp().format(formatter));
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #78716C;");
            textBox.getChildren().addAll(actionLabel, timeLabel);

            StackPane spacer = new StackPane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(dot, textBox, spacer);
            vboxNotifications.getChildren().add(row);
        }
    }
}
