package models;

import java.sql.Timestamp;

public class PasswordResetRequest {

    private int id;
    private String email;
    private Timestamp requestedAt;
    private String status;
    private Timestamp approvedAt;
    private boolean used;

    public PasswordResetRequest(int id, String email, Timestamp requestedAt, String status) {
        this.id = id;
        this.email = email;
        this.requestedAt = requestedAt;
        this.status = status;
        this.approvedAt = null;
        this.used = false;
    }

    public PasswordResetRequest(int id, String email, Timestamp requestedAt, String status, Timestamp approvedAt, boolean used) {
        this.id = id;
        this.email = email;
        this.requestedAt = requestedAt;
        this.status = status;
        this.approvedAt = approvedAt;
        this.used = used;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getApprovedAt() {
        return approvedAt;
    }

    public boolean isUsed() {
        return used;
    }
}
