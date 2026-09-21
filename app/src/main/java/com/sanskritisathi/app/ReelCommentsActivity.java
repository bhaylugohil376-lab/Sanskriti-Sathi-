package com.sanskritisathi.app;

public class Reel {

    private String id;
    private String userId;
    private String username;
    private String videoUrl;
    private String caption;
    private String visibility;

    private int likes;
    private int comments;
    private int views;

    private boolean ownReel;
    private boolean liked;

    public Reel(
            String id,
            String userId,
            String username,
            String videoUrl,
            String caption,
            String visibility,
            int likes,
            int comments,
            int views,
            boolean ownReel) {

        this.id = id;
        this.userId = userId;
        this.username = username;
        this.videoUrl = videoUrl;
        this.caption = caption;
        this.visibility = visibility;
        this.likes = likes;
        this.comments = comments;
        this.views = views;
        this.ownReel = ownReel;
        this.liked = false;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getVisibility() {
        return visibility;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public int getViews() {
        return views;
    }

    public boolean isOwnReel() {
        return ownReel;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setLikes(int likes) {
        this.likes = Math.max(0, likes);
    }

    public void setComments(int comments) {
        this.comments = Math.max(0, comments);
    }

    public void setViews(int views) {
        this.views = Math.max(0, views);
    }

    public void setOwnReel(boolean ownReel) {
        this.ownReel = ownReel;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
