package com.sanskritisathi.app;

public class SearchUser {

    private final String id;
    private final String username;
    private final String displayName;
    private final String avatarUrl;

    public SearchUser(
            String id,
            String username,
            String displayName,
            String avatarUrl
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
