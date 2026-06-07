package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utils.NavigationHelper;

public class StaffDashboardController {

    @FXML
    private Button btnOpenPassSlip;

    @FXML
    private Button btnOpenReturn;

    @FXML
    private Button btnOpenMonitoring;

    @FXML
    private Button btnLogout;

    @FXML
    private void initialize() {

        btnOpenPassSlip.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenPassSlip,
                        "/fxml/PassSlip.fxml"
                )
        );

        btnOpenReturn.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenReturn,
                        "/fxml/Return.fxml"
                )
        );

        btnOpenMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnOpenMonitoring,
                        "/fxml/Monitoring.fxml"
                )
        );

        btnLogout.setOnAction(
                event -> NavigationHelper.logout(btnLogout)
        );

    }
}
