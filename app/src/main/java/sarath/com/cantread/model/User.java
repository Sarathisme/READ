package sarath.com.cantread.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    private String userID, imageLocalUrl, imageCloudUrl;
    private long timestamp;
    private String ocrText;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;

    public User() {
    }

    public User(String userID, String imageLocalUrl, String imageCloudUrl, long timestamp, String ocrText, boolean isStarred, String username) {
        this.userID = userID;
        this.imageLocalUrl = imageLocalUrl;
        this.imageCloudUrl = imageCloudUrl;
        this.timestamp = timestamp;
        this.ocrText = ocrText;
        this.isStarred = isStarred;
        this.username = username;
    }

    private boolean isStarred = false;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getImageLocalUrl() {
        return imageLocalUrl;
    }

    public void setImageLocalUrl(String imageLocalUrl) {
        this.imageLocalUrl = imageLocalUrl;
    }

    public String getImageCloudUrl() {
        return imageCloudUrl;
    }

    public void setImageCloudUrl(String imageCloudUrl) {
        this.imageCloudUrl = imageCloudUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }
}
