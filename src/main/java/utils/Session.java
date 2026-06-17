package utils;

public class Session {

    public static volatile int currentUserId;

    public static volatile String currentUsername;

    public static volatile String currentRole;

    public static volatile int selectedEmployeeId;

    public static volatile String currentFxmlPath;

    public static void clear() {

        currentUserId = 0;
        currentUsername = null;
        currentRole = null;
        selectedEmployeeId = 0;
        currentFxmlPath = null;

    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(currentRole);
    }

}
