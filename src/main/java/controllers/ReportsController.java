package controllers;

import database.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.ActivityLog;
import utils.NavigationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class ReportsController {

    @FXML
    private Button btnOpenDashboard;

    @FXML
    private Button btnOpenEmployees;

    @FXML
    private Label lblTotalEmployees;

    @FXML
    private Label lblTotalPassSlips;

    @FXML
    private VBox recentHistoryList;

    @FXML
    private void initialize() {

        btnOpenDashboard.setOnAction(
                event -> NavigationHelper.navigateToDashboard(
                        btnOpenDashboard
                )
        );

        btnOpenEmployees.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenEmployees,
                        "/fxml/EmployeeController.fxml"
                )
        );

        loadReports(
                lblTotalEmployees,
                lblTotalPassSlips,
                new Label(),
                new Label(),
                new Label()
        );

        loadRecentHistory();

    }

    public static void loadReports(

            Label totalEmployeesLabel,

            Label totalPassSlipsLabel,

            Label totalOutLabel,

            Label totalReturnedLabel,

            Label totalExpiredLabel

    ) {

        try {

            Connection connection =
                    DatabaseConnection.connect();




            // TOTAL EMPLOYEES
            String employeeQuery =
                    "SELECT COUNT(*) FROM employees";

            PreparedStatement employeeStatement =
                    connection.prepareStatement(employeeQuery);

            ResultSet employeeResult =
                    employeeStatement.executeQuery();

            if(employeeResult.next()) {

                totalEmployeesLabel.setText(
                        "TOTAL EMPLOYEES: "
                                + employeeResult.getInt(1)
                );

            }




            // TOTAL PASS SLIPS
            String passSlipQuery =
                    "SELECT COUNT(*) FROM pass_slips";

            PreparedStatement passSlipStatement =
                    connection.prepareStatement(passSlipQuery);

            ResultSet passSlipResult =
                    passSlipStatement.executeQuery();

            if(passSlipResult.next()) {

                totalPassSlipsLabel.setText(
                        "TOTAL PASS SLIPS: "
                                + passSlipResult.getInt(1)
                );

            }




            // TOTAL OUT
            String outQuery = """
                    SELECT COUNT(*)
                    FROM pass_slips
                    WHERE status = 'OUT'
                    """;

            PreparedStatement outStatement =
                    connection.prepareStatement(outQuery);

            ResultSet outResult =
                    outStatement.executeQuery();

            if(outResult.next()) {

                totalOutLabel.setText(
                        "TOTAL OUT: "
                                + outResult.getInt(1)
                );

            }




            // TOTAL RETURNED
            String returnedQuery = """
                    SELECT COUNT(*)
                    FROM pass_slips
                    WHERE status = 'RETURNED'
                    """;

            PreparedStatement returnedStatement =
                    connection.prepareStatement(returnedQuery);

            ResultSet returnedResult =
                    returnedStatement.executeQuery();

            if(returnedResult.next()) {

                totalReturnedLabel.setText(
                        "TOTAL RETURNED: "
                                + returnedResult.getInt(1)
                );

            }




            // TOTAL EXPIRED
            String expiredQuery = """
                    SELECT COUNT(*)
                    FROM pass_slips
                    WHERE status = 'EXPIRED'
                    """;

            PreparedStatement expiredStatement =
                    connection.prepareStatement(expiredQuery);

            ResultSet expiredResult =
                    expiredStatement.executeQuery();

            if(expiredResult.next()) {

                totalExpiredLabel.setText(
                        "TOTAL EXPIRED: "
                                + expiredResult.getInt(1)
                );

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

    }






    // GET ACTIVITY LOGS
    public static ObservableList<ActivityLog> getLogs() {

        ObservableList<ActivityLog> logs =
                FXCollections.observableArrayList();

        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query = """
                    SELECT *
                    FROM activity_logs
                    ORDER BY created_at DESC
                    """;



            PreparedStatement statement =
                    connection.prepareStatement(query);



            ResultSet resultSet =
                    statement.executeQuery();



            while(resultSet.next()) {

                ActivityLog log =
                        new ActivityLog(

                                resultSet.getInt("id"),

                                resultSet.getString("username"),

                                resultSet.getString("action"),

                                resultSet.getTimestamp("created_at")
                                        .toLocalDateTime()

                        );



                logs.add(log);

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

        return logs;

    }

    private void loadRecentHistory() {

        recentHistoryList.getChildren().clear();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm"
                );

        for (ActivityLog log : getLogs()) {

            HBox row =
                    new HBox(10);

            Label userLabel =
                    new Label(log.getUsername());

            userLabel.setPrefWidth(180);

            Label actionLabel =
                    new Label(log.getAction());

            actionLabel.setPrefWidth(320);

            Region spacer =
                    new Region();

            HBox.setHgrow(
                    spacer,
                    Priority.ALWAYS
            );

            Label timeLabel =
                    new Label(
                            log.getCreatedAt().format(formatter)
                    );

            row.getChildren().addAll(
                    userLabel,
                    actionLabel,
                    spacer,
                    timeLabel
            );

            recentHistoryList.getChildren().add(row);

        }

    }

}
