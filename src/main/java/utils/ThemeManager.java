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
        return "";
    }
}
