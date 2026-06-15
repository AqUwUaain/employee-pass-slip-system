package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utils.NavigationHelper;

public class StaffDashboardController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarLogReturn;

    @FXML
    private Button btnSidebarSignatures;

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

        NavigationHelper.setActiveButton(btnSidebarDashboard);

        btnSidebarDashboard.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarDashboard,
                        "/fxml/StaffDashboard.fxml"
                )
        );

        btnSidebarMonitoring.setOnAction(
                event -> NavigationHelper.navigateTo(
                        btnSidebarMonitoring,
                        "/fxml/Monitoring.fxml"
                )
        );

        if (btnSidebarLogReturn != null)
            btnSidebarLogReturn.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarLogReturn,
                            "/fxml/Return.fxml"
                    )
            );

        if (btnSidebarSignatures != null)
            btnSidebarSignatures.setOnAction(
                    event -> NavigationHelper.navigateTo(
                            btnSidebarSignatures,
                            "/fxml/SignatureManager.fxml"
                    )
            );

        if (btnLogout != null)
            btnLogout.setOnAction(
                    event -> NavigationHelper.logout(btnLogout)
            );

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

    }
}
