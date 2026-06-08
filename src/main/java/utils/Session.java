package utils;

public class Session {

    public static int currentUserId;

    public static String currentUsername;

    public static String currentRole;

    public static int selectedEmployeeId;

    public static void clear() {

        currentUserId = 0;
        currentUsername = null;
        currentRole = null;
        selectedEmployeeId = 0;

    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(currentRole);
    }

}
