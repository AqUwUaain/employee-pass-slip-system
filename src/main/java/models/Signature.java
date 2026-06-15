package models;

public class Signature {

    private int id;
    private int userId;
    private String signatureName;
    private byte[] imageData;

    public Signature(int id, int userId, String signatureName, byte[] imageData) {
        this.id = id;
        this.userId = userId;
        this.signatureName = signatureName;
        this.imageData = imageData;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public byte[] getImageData() {
        return imageData;
    }

}
