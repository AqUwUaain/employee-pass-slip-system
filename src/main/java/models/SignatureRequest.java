package models;

import java.sql.Timestamp;

public class SignatureRequest {

    private int id;
    private int userId;
    private String staffName;
    private String signatureName;
    private byte[] imageData;
    private String status;
    private Timestamp createdAt;

    public SignatureRequest(int id, int userId, String staffName, String signatureName,
                            byte[] imageData, String status, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.staffName = staffName;
        this.signatureName = signatureName;
        this.imageData = imageData;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

}
