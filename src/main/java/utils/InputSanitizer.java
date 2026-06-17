package utils;

public class InputSanitizer {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final int MAX_EMAIL_LENGTH = 255;

    public static String sanitizeName(String input) {
        if (input == null) return "";
        return input.replaceAll("[<>\"'&;]", "")
                    .replaceAll("\\s+", " ")
                    .trim()
                    .substring(0, Math.min(input.trim().length(), MAX_NAME_LENGTH));
    }

    public static String sanitizeText(String input) {
        if (input == null) return "";
        return input.replaceAll("[<>\"']", "")
                    .trim()
                    .substring(0, Math.min(input.trim().length(), MAX_TEXT_LENGTH));
    }

    public static String sanitizeEmail(String input) {
        if (input == null) return "";
        return input.replaceAll("[<>\"'&;\\s]", "")
                    .trim()
                    .toLowerCase()
                    .substring(0, Math.min(input.trim().length(), MAX_EMAIL_LENGTH));
    }

    public static String sanitizeEmployeeId(String input) {
        if (input == null) return "";
        return input.replaceAll("[^0-9]", "").trim();
    }
}
