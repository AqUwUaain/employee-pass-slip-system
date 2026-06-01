package models;

import java.time.LocalDateTime;

public class ActivityLog {

    private int id;

    private String username;

    private String action;

    private LocalDateTime createdAt;



    public ActivityLog(
            int id,
            String username,
            String action,
            LocalDateTime createdAt
    ) {

        this.id = id;

        this.username = username;

        this.action = action;

        this.createdAt = createdAt;

    }



    public int getId() {

        return id;

    }



    public String getUsername() {

        return username;

    }



    public String getAction() {

        return action;

    }



    public LocalDateTime getCreatedAt() {

        return createdAt;

    }

}