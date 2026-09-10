package com.sanskritisathi.app;

public class NotificationModel {

    private String id;
    private String title;
    private String message;
    private String time;
    private boolean read;

    public NotificationModel() {
        // Required for Firebase Firestore
    }

    public NotificationModel(
            String id,
            String title,
            String message,
            String time,
            boolean read
    ) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
