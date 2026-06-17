package utils;

import javafx.scene.control.Button;

public final class SidebarHelper {

    private SidebarHelper() {
    }

    public static void initialize(
            Button btnDashboard,
            Button btnMonitoring,
            Button btnEmployeeDirectory,
            Button btnAddEmployee,
            Button btnImportEmployee,
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

        if (btnEmployeeDirectory != null)
            btnEmployeeDirectory.setOnAction(e -> NavigationHelper.navigateTo(btnEmployeeDirectory, "/fxml/EmployeeList.fxml"));

        if (btnAddEmployee != null)
            btnAddEmployee.setOnAction(e -> {
                Session.selectedEmployeeId = 0;
                NavigationHelper.navigateTo(btnAddEmployee, "/fxml/EmployeeForm.fxml");
            });

        if (btnImportEmployee != null)
            btnImportEmployee.setOnAction(e -> NavigationHelper.navigateTo(btnImportEmployee, "/fxml/EmployeeImport.fxml"));

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

        NavigationHelper.hideAdminSidebarItems(btnEmployeeDirectory, btnReports, btnUsers, btnPasswordReset);
        NavigationHelper.hideMonitoringForStaff(btnMonitoring);

        if ("STAFF".equalsIgnoreCase(Session.currentRole)) {
            hideButton(btnEmployeeDirectory);
            hideButton(btnAddEmployee);
            hideButton(btnImportEmployee);
        }

        if (activeButton != null)
            NavigationHelper.setActiveButton(activeButton);

        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));

        if (btnNotifications != null)
            btnNotifications.setOnAction(e -> NotificationHelper.toggle(btnNotifications));
    }

    private static void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }
}
