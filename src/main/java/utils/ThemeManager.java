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

    /* ---- Light Theme Colors ---- */
    private static final String LIGHT_BG = "#FFF9DC";
    private static final String LIGHT_PANEL = "#FFF4C4";
    private static final String LIGHT_CARD = "#FFF6D5";
    private static final String LIGHT_BORDER = "#E6D9A8";
    private static final String LIGHT_HOVER = "#F3E4B2";
    private static final String LIGHT_TEXT_PRIMARY = "#2D2416";
    private static final String LIGHT_TEXT_SECONDARY = "#5A503F";
    private static final String LIGHT_TEXT_MUTED = "#7A715E";
    private static final String LIGHT_GOLD = "#D4A853";
    private static final String LIGHT_GOLD_DARK = "#92700C";

    /* ---- Dark Theme Colors ---- */
    private static final String DARK_BG = "#1C1917";
    private static final String DARK_PANEL = "#25211E";
    private static final String DARK_CARD = "#2E2925";
    private static final String DARK_BORDER = "#4A3D2A";
    private static final String DARK_TEXT_PRIMARY = "#F5F1E8";
    private static final String DARK_TEXT_SECONDARY = "#A8A29E";
    private static final String DARK_TEXT_MUTED = "#78716C";
    private static final String DARK_GOLD = "#D4A853";

    public static String getTextPrimary() {
        return isLightMode() ? LIGHT_TEXT_PRIMARY : DARK_TEXT_PRIMARY;
    }

    public static String getTextSecondary() {
        return isLightMode() ? LIGHT_TEXT_SECONDARY : DARK_TEXT_SECONDARY;
    }

    public static String getTextMuted() {
        return isLightMode() ? LIGHT_TEXT_MUTED : DARK_TEXT_MUTED;
    }

    public static String getGoldText() {
        return isLightMode() ? LIGHT_GOLD_DARK : DARK_GOLD;
    }

    public static String getCardBg() {
        return isLightMode() ? LIGHT_CARD : DARK_CARD;
    }

    public static String getPageBg() {
        return isLightMode() ? LIGHT_BG : DARK_BG;
    }

    public static String getTableHover() {
        return isLightMode() ? LIGHT_HOVER : DARK_BORDER;
    }

    public static String getActiveStyle() {
        return isLightMode()
                ? "-fx-background-color: #FFF4C4;" +
                  "-fx-text-fill: #92700C;" +
                  "-fx-font-weight: bold;" +
                  "-fx-border-color: #D4A853;" +
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
