package utils;

import controllers.LogoutConfirmController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    public static void navigateTo(Node source, String fxmlPath) {

        Stage stage =
                (Stage) source.getScene().getWindow();

        navigateTo(stage, fxmlPath);

    }

    public static void navigateTo(Stage stage, String fxmlPath) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            NavigationHelper.class.getResource(fxmlPath)
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(root);

            stage.setScene(scene);
            stage.show();

        }
        catch (IOException e) {

            throw new RuntimeException(
                    "Unable to load FXML: " + fxmlPath,
                    e
            );

        }

    }

    public static void navigateToDashboard(Node source) {

        navigateTo(
                source,
                getDashboardFxml()
        );

    }

    public static String getDashboardFxml() {

        if ("STAFF".equalsIgnoreCase(Session.currentRole)) {
            return "/fxml/StaffDashboard.fxml";
        }

        return "/fxml/Dashboard.fxml";

    }

    public static void logout(Node source) {

        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource("/fxml/LogoutConfirm.fxml"));
            Parent root = loader.load();

            LogoutConfirmController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initOwner(source.getScene().getWindow());
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
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
