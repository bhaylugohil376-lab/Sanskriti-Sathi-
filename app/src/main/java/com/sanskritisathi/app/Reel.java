package com.sanskritisathi.app;

import com.google.gson.annotations.SerializedName;

public class Reel {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("username")
    private String username;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("caption")
    private String caption;

    @SerializedName("visibility")
    private String visibility;

    @SerializedName("created_at")
    private long createdAt;

    @SerializedName("likes")
    private int likes;

    @SerializedName("comments")
    private int comments;

    @SerializedName("views")
    private int views;

    @SerializedName("liked")
    private boolean liked;

    @SerializedName("own_reel")
    private boolean ownReel;

    // MANDATORY DEFAULT CONSTRUCTOR FOR GSON / SUPABASE PARSING
    public Reel() {
    }

    public Reel(
            String id,
            String userId,
            String username,
            String videoUrl,
            String thumbnailUrl,
            String caption,
            String visibility,
            long createdAt,
            int likes,
            int comments,
            int views,
            boolean liked,
            boolean ownReel) {

        this.id = id;
        this.userId = userId;
        this.username = username;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.caption = caption;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.likes = likes;
        this.comments = comments;
        this.views = views;
        this.liked = liked;
        this.ownReel = ownReel;
    }

    // =========================================================
    // GETTERS
    // =========================================================

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

    public String getThumbnailUrl() {
        return thumbnailUrl;
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

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public int getViews() {
        return views;
    }

    public boolean isLiked() {
        return liked;
    }

    public boolean isOwnReel() {
        return ownReel;
    }

    // =========================================================
    // SETTERS
    // =========================================================

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

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setOwnReel(boolean ownReel) {
        this.ownReel = ownReel;
    }
}
