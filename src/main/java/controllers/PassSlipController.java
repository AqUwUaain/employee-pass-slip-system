package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import utils.NavigationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PassSlipController {

    @FXML
    private Button btnTopClose;

    @FXML
    private Button btnCancelSlipAction;

    @FXML
    private Button btnPrintSlipAction;

    @FXML
    private Button btnSaveSlipAction;

    @FXML
    private TextField txtSlipYear;

    @FXML
    private TextField txtSlipFormattedDate;

    @FXML
    private TextField txtSlipClientId;

    @FXML
    private TextField txtSlipEmployeeName;

    @FXML
    private TextArea txtSlipPurpose;

    @FXML
    private TextField txtSlipTimeOut;

    @FXML
    private TextField txtSlipTimeIn;

    @FXML
    private TextField txtSlipAdminSignee;

    @FXML
    private Label lblSlipStatusMessage;

    @FXML
    private void initialize() {

        LocalDateTime now =
                LocalDateTime.now();

        txtSlipYear.setText(
                String.valueOf(now.getYear())
        );
        txtSlipFormattedDate.setText(
                now.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM dd, yyyy"
                        )
                ).toUpperCase()
        );
        txtSlipTimeOut.setText(
                now.format(
                        DateTimeFormatter.ofPattern(
                                "hh:mm a"
                        )
                )
        );

        btnTopClose.setOnAction(
                event -> NavigationHelper.navigateToDashboard(
                        btnTopClose
                )
        );

        btnCancelSlipAction.setOnAction(
                event -> NavigationHelper.navigateToDashboard(
                        btnCancelSlipAction
                )
        );

        btnPrintSlipAction.setOnAction(
                event -> lblSlipStatusMessage.setText(
                        "Print is not implemented yet."
                )
        );

        btnSaveSlipAction.setOnAction(
                event -> issuePassSlip(
                        txtSlipClientId.getText(),
                        txtSlipPurpose.getText(),
                        lblSlipStatusMessage
                )
        );

    }

    public static void issuePassSlip(

            String employeeId,
            String reason,
            Label messageLabel

    ) {

        // EMPLOYEE VALIDATION
        if(employeeId == null || employeeId.isEmpty()) {

            messageLabel.setText(
                    "SELECT EMPLOYEE"
            );

            return;

        }



        // REASON VALIDATION
        if(reason == null || reason.trim().isEmpty()) {

            messageLabel.setText(
                    "REASON REQUIRED"
            );

            return;

        }



        // MINIMUM REASON LENGTH
        if(reason.trim().length() < 5) {

            messageLabel.setText(
                    "REASON TOO SHORT"
            );

            return;

        }



        try {

            Connection connection =
                    DatabaseConnection.connect();



            String query =

                    "INSERT INTO pass_slips " +
                            "(employee_id, reason, time_out, status) " +
                            "VALUES (?, ?, ?, ?)";



            PreparedStatement statement =
                    connection.prepareStatement(query);



            statement.setInt(
                    1,
                    Integer.parseInt(employeeId)
            );

            statement.setString(
                    2,
                    reason.trim()
            );

            statement.setTimestamp(
                    3,
                    java.sql.Timestamp.valueOf(
                            LocalDateTime.now()
                    )
            );

            statement.setString(
                    4,
                    "OUT"
            );



            int inserted =
                    statement.executeUpdate();



            if(inserted > 0) {

                // ACTIVITY LOG
                ActivityLogController.logActivity(
                        "Issued Pass Slip for Employee ID: "
                                + employeeId
                );



                messageLabel.setText(
                        "PASS SLIP ISSUED"
                );

            }

            else {

                messageLabel.setText(
                        "FAILED TO ISSUE PASS SLIP"
                );

            }

        }

        catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "DATABASE ERROR"
            );

        }

    }

}
