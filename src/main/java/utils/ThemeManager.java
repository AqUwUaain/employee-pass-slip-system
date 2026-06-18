package utils;

import java.util.prefs.Preferences;

public final class ThemeManager {

    private static final String DARK_CSS =
            ThemeManager.class.getResource("/css/style.css").toExternalForm();
    private static final String LIGHT_CSS =
            ThemeManager.class.getResource("/css/style-light.css").toExternalForm();

    private static final Preferences prefs =
            Preferences.userNodeForPackage(ThemeManager.class);

    private static boolean isDark = prefs.getBoolean("dark_mode", true);

    private ThemeManager() {
    }

    public static boolean isDark() {
        return isDark;
    }

    public static void toggle() {
        isDark = !isDark;
        prefs.putBoolean("dark_mode", isDark);
    }

    public static String getCssPath() {
        return isDark ? DARK_CSS : LIGHT_CSS;
    }

    public static String getDarkCssPath() {
        return DARK_CSS;
    }

    public static String getActiveStyle() {
        return "";
    }
}
