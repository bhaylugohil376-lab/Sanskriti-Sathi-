package com.sanskritisathi.app;

public class Story {

    private String id;
    private String username;
    private String profileImage;
    private String storyImage;
    private String caption;
    private String visibility; // Public / Followers
    private long createdAt;
    private int views;
    private boolean liked;
    private boolean ownStory;

    public Story(String id,
                 String username,
                 String profileImage,
                 String storyImage,
                 String caption,
                 String visibility,
                 long createdAt,
                 int views,
                 boolean ownStory) {

        this.id = id;
        this.username = username;
        this.profileImage = profileImage;
        this.storyImage = storyImage;
        this.caption = caption;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.views = views;
        this.ownStory = ownStory;
        this.liked = false;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getStoryImage() {
        return storyImage;
    }

    public String getCaption() {
        return caption;
    }

    public String getVisibility() {
        return visibility;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getViews() {
        return views;
    }

    public boolean isLiked() {
        return liked;
    }

    public boolean isOwnStory() {
        return ownStory;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isExpired() {
        long twentyFourHours = 24L * 60L * 60L * 1000L;
        return System.currentTimeMillis() - createdAt >= twentyFourHours;
    }
}
