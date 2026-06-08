package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        Session.clear();
        loaderCache.clear();
        navigateTo(source, "/fxml/Login.fxml");

    }
}
