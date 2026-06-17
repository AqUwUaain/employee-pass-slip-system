package utils;

import controllers.LogoutConfirmController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NavigationHelper {

    private NavigationHelper() {
    }

    private static final Map<String, FXMLLoader> loaderCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, FXMLLoader> eldest) {
            return size() > 12;
        }
    };

    public static void setActiveButton(Button button) {
        if (button == null) return;
        if (!button.getStyleClass().contains("nav-active")) {
            button.getStyleClass().add("nav-active");
        }
    }

    public static void navigateTo(Node source, String fxmlPath) {
        Stage stage = (Stage) source.getScene().getWindow();
        navigateTo(stage, fxmlPath);
    }

    public static void navigateTo(Stage stage, String fxmlPath) {
        try {
            boolean wasFullScreen = stage.isFullScreen();
            double prevWidth = stage.getWidth();
            double prevHeight = stage.getHeight();

            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(ThemeManager.getCssPath());
            stage.setScene(scene);
            Session.currentFxmlPath = fxmlPath;

            if (wasFullScreen) {
                stage.setFullScreen(true);
            } else {
                double newPrefWidth = root.prefWidth(-1);
                double newPrefHeight = root.prefHeight(-1);
                double newWidth = Math.max(prevWidth, newPrefWidth > 0 ? newPrefWidth : 0);
                double newHeight = Math.max(prevHeight, newPrefHeight > 0 ? newPrefHeight : 0);
                stage.setWidth(newWidth);
                stage.setHeight(newHeight);
            }

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load FXML: " + fxmlPath, e);
        }
    }

    public static void navigateToDashboard(Node source) {
        navigateTo(source, getDashboardFxml());
    }

    public static String getDashboardFxml() {
        if ("STAFF".equalsIgnoreCase(Session.currentRole)) {
            return "/fxml/StaffDashboard.fxml";
        }
        return "/fxml/Dashboard.fxml";
    }

    public static void hideAdminSidebarItems(
            Button employees,
            Button reports,
            Button users,
            Button passwordReset
    ) {
        if (!"STAFF".equalsIgnoreCase(Session.currentRole)) return;

        if (employees != null) { employees.setVisible(false); employees.setManaged(false); }
        if (reports != null) { reports.setVisible(false); reports.setManaged(false); }
        if (users != null) { users.setVisible(false); users.setManaged(false); }
        if (passwordReset != null) { passwordReset.setVisible(false); passwordReset.setManaged(false); }
    }

    public static void hideMonitoringForStaff(Button btnSidebarMonitoring) {
        if (!"STAFF".equalsIgnoreCase(Session.currentRole)) return;
        if (btnSidebarMonitoring != null) {
            btnSidebarMonitoring.setVisible(false);
            btnSidebarMonitoring.setManaged(false);
        }
    }

    public static String getCurrentFxmlPath(Node source) {
        return Session.currentFxmlPath != null ? Session.currentFxmlPath : "/fxml/Dashboard.fxml";
    }

    // ==================== FIXED LOGOUT METHOD ====================
    public static void logout(Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource("/fxml/LogoutConfirm.fxml"));
            Parent root = loader.load();

            LogoutConfirmController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(source.getScene().getWindow());
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            Stage ownerStage = (Stage) source.getScene().getWindow();
            double dialogWidth = root.prefWidth(-1);
            double dialogHeight = root.prefHeight(-1);
            if (dialogWidth <= 0) dialogWidth = 400;
            if (dialogHeight <= 0) dialogHeight = 250;
            dialogStage.setX(ownerStage.getX() + (ownerStage.getWidth() - dialogWidth) / 2);
            dialogStage.setY(ownerStage.getY() + (ownerStage.getHeight() - dialogHeight) / 2);

            dialogStage.showAndWait();

            if (controller.isConfirmed()) {
                controllers.ActivityLogController.logActivity("User Logged Out", 0);
                Session.clear();
                loaderCache.clear();
                navigateTo(source, "/fxml/Login.fxml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}