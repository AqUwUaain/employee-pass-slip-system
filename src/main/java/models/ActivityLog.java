package models;

import java.time.LocalDateTime;

public class ActivityLog {

    private int id;
    private String action;
    private String description;
    private int userId;
    private String username;
    private LocalDateTime timestamp;
    private int employeeId;

    public ActivityLog(
            int id,
            String action,
            String description,
            int userId,
            String username,
            LocalDateTime timestamp,
            int employeeId
    ) {
        this.id = id;
        this.action = action;
        this.description = description;
        this.userId = userId;
        this.username = username;
        this.timestamp = timestamp;
        this.employeeId = employeeId;
    }

    public int getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getEmployeeId() {
        return employeeId;
    }

}
