package utils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public final class ThemeManager {

    private static final Preferences prefs =
            Preferences.userNodeForPackage(ThemeManager.class);

    private static final String DARK_CSS =
            ThemeManager.class.getResource("/css/style.css").toExternalForm();
    private static final String LIGHT_CSS =
            ThemeManager.class.getResource("/css/style-light.css").toExternalForm();

    private ThemeManager() {
    }

    public static boolean isLightMode() {
        return prefs.getBoolean("light_mode", false);
    }

    public static String getCssPath() {
        return isLightMode() ? LIGHT_CSS : DARK_CSS;
    }

    public static void toggleTheme() {
        prefs.putBoolean("light_mode", !isLightMode());
    }

    public static void applyToScene(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getCssPath());
    }

    public static void applyToAllWindows() {
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w instanceof Stage s && s.getScene() != null) {
                applyToScene(s.getScene());
            }
        }
    }

    public static void setThemeToggleLabel(Button btn) {
        if (btn == null) return;
        btn.setText(isLightMode() ? "\u263E" : "\u2600");
    }

    public static String getTextPrimary() {
        return isLightMode() ? "#1C1917" : "#F5F5F4";
    }

    public static String getTextSecondary() {
        return isLightMode() ? "#1C1917" : "#A8A29E";
    }

    public static String getTextMuted() {
        return isLightMode() ? "#57534E" : "#78716C";
    }

    public static String getGoldText() {
        return isLightMode() ? "#92700C" : "#D4A853";
    }

    public static String getCardBg() {
        return isLightMode() ? "#FFFFFF" : "#252220";
    }

    public static String getPageBg() {
        return isLightMode() ? "#F5F0EB" : "#1E1B18";
    }

    public static String getTableHover() {
        return isLightMode() ? "#F5F0EB" : "#3D3229";
    }

    public static String getActiveStyle() {
        return isLightMode()
                ? "-fx-background-color: #F5EDE3;" +
                  "-fx-text-fill: #92700C;" +
                  "-fx-font-weight: bold;" +
                  "-fx-border-color: #92700C;" +
                  "-fx-border-width: 0 0 0 3;" +
                  "-fx-border-radius: 12;" +
                  "-fx-background-radius: 12;" +
                  "-fx-background-insets: 0;" +
                  "-fx-focus-color: transparent;" +
                  "-fx-faint-focus-color: transparent;"
                : "-fx-background-color: #3D2A2A;" +
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
