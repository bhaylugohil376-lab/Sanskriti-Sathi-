package com.sanskritisathi.app;

public class ReelComment {

    private String id;
    private String reelId;
    private String userId;
    private String username;
    private String text;
    private long createdAt;

    public ReelComment(
            String id,
            String reelId,
            String userId,
            String username,
            String text,
            long createdAt) {

        this.id = id;
        this.reelId = reelId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getReelId() {
        return reelId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
