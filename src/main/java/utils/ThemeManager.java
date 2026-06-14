package utils;

import javafx.scene.control.Button;

public final class ThemeManager {

    private static final String DARK_CSS =
            ThemeManager.class.getResource("/css/style.css").toExternalForm();

    private ThemeManager() {
    }

    public static String getCssPath() {
        return DARK_CSS;
    }

    public static String getActiveStyle() {
        return "-fx-background-color: #3D2A2A;" +
               "-fx-text-fill: #D4A853;" +
               "-fx-font-weight: bold;" +
               "-fx-border-color: #D4A853;" +
               "-fx-border-width: 0 0 0 3;" +
               "-fx-border-radius: 12;" +
               "-fx-background-radius: 12;" +
               "-fx-background-insets: 0;" +
               "-fx-focus-color: transparent;" +
               "-fx-faint-focus-color: transparent;";
    }
}
