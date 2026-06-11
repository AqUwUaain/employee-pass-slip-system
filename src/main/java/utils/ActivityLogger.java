package utils;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ActivityLogger {

    public static void log(String action, String description) {
        log(action, description, 0);
    }

    public static void log(String action, String description, int employeeId) {
        String sql = "INSERT INTO activity_logs (action, description, user_id, username, employee_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, action);
                    pstmt.setString(2, description);
                    pstmt.setInt(3, Session.currentUserId);
                    pstmt.setString(4, Session.currentUsername != null ? Session.currentUsername : "System");
                    pstmt.setInt(5, employeeId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }
}
