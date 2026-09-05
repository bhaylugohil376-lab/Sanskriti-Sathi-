package com.sanskritisathi.app;

public class GroupMessage {

    private String messageId;
    private String userId;
    private String username;
    private String text;
    private long createdAt;

    public GroupMessage(
            String messageId,
            String userId,
            String username,
            String text,
            long createdAt
    ) {
        this.messageId = messageId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getMessageId() {
        return messageId;
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
