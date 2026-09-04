package com.sanskritisathi.app;

public class Reel {

    private String id;
    private String ownerUid;
    private String username;
    private String videoUrl;
    private String thumbnailUrl;
    private String caption;
    private String visibility;

    private long createdAt;
    private int likes;
    private int comments;
    private int views;

    private boolean liked;
    private boolean ownReel;

    public Reel(
            String id,
            String ownerUid,
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
        this.ownerUid = ownerUid;
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

    public String getId() {
        return id;
    }

    public String getOwnerUid() {
        return ownerUid;
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

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
