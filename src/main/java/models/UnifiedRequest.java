package models;

import java.sql.Timestamp;

public class UnifiedRequest {

    public enum Category {
        PASSWORD_RESET,
        PASS_SLIP,
        SIGNATURE
    }

    private int id;
    private Category category;
    private String requester;
    private String detail;
    private String status;
    private Timestamp createdAt;
    private Object rawData;

    public UnifiedRequest(int id, Category category, String requester, String detail,
                          String status, Timestamp createdAt, Object rawData) {
        this.id = id;
        this.category = category;
        this.requester = requester;
        this.detail = detail;
        this.status = status;
        this.createdAt = createdAt;
        this.rawData = rawData;
    }

    public int getId() { return id; }
    public Category getCategory() { return category; }
    public String getRequester() { return requester; }
    public String getDetail() { return detail; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public <T> T getRawData(Class<T> type) { return type.cast(rawData); }
}
