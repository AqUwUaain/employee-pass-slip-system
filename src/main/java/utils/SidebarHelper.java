package utils;

import javafx.scene.control.Button;

public final class SidebarHelper {

    private SidebarHelper() {
    }

    public static void initialize(
            Button btnDashboard,
            Button btnMonitoring,
            Button btnEmployees,
            Button btnReports,
            Button btnLogReturn,
            Button btnUsers,
            Button btnSignatures,
            Button btnPasswordReset,
            Button btnLogout,
            Button btnNotifications,
            Button activeButton
    ) {
        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> NavigationHelper.navigateToDashboard(btnDashboard));

        if (btnMonitoring != null)
            btnMonitoring.setOnAction(e -> NavigationHelper.navigateTo(btnMonitoring, "/fxml/Monitoring.fxml"));

        if (btnEmployees != null)
            btnEmployees.setOnAction(e -> NavigationHelper.navigateTo(btnEmployees, "/fxml/EmployeeController.fxml"));

        if (btnReports != null)
            btnReports.setOnAction(e -> NavigationHelper.navigateTo(btnReports, "/fxml/Reports.fxml"));

        if (btnLogReturn != null)
            btnLogReturn.setOnAction(e -> NavigationHelper.navigateTo(btnLogReturn, "/fxml/Return.fxml"));

        if (btnUsers != null)
            btnUsers.setOnAction(e -> NavigationHelper.navigateTo(btnUsers, "/fxml/User.fxml"));

        if (btnSignatures != null)
            btnSignatures.setOnAction(e -> NavigationHelper.navigateTo(btnSignatures, "/fxml/SignatureManager.fxml"));

        if (btnPasswordReset != null)
            btnPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnPasswordReset, "/fxml/PasswordResetRequests.fxml"));

        NavigationHelper.hideAdminSidebarItems(btnEmployees, btnReports, btnUsers, btnPasswordReset);
        NavigationHelper.hideMonitoringForStaff(btnMonitoring);

        if (activeButton != null)
            NavigationHelper.setActiveButton(activeButton);

        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));

        if (btnNotifications != null)
            btnNotifications.setOnAction(e -> NotificationHelper.toggle(btnNotifications));
    }
}
