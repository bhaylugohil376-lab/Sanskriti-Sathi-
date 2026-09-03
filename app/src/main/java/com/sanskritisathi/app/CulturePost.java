package com.sanskritisathi.app;

public class CulturePost {

    private String author;
    private String category;
    private String caption;

    private int likeCount;
    private int profileImageResId;
    private int postImageResId;

    private boolean liked;
    private boolean saved;

    public CulturePost(
            String author,
            String category,
            String caption,
            int likeCount,
            int profileImageResId,
            int postImageResId
    ) {
        this.author = author;
        this.category = category;
        this.caption = caption;
        this.likeCount = Math.max(0, likeCount);
        this.profileImageResId = profileImageResId;
        this.postImageResId = postImageResId;
        this.liked = false;
        this.saved = false;
    }

    // =========================
    // GETTERS
    // =========================

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getCaption() {
        return caption;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public int getPostImageResId() {
        return postImageResId;
    }

    public boolean isLiked() {
        return liked;
    }

    public boolean isSaved() {
        return saved;
    }

    // =========================
    // LIKE
    // =========================

    public void toggleLiked() {

        if (liked) {

            if (likeCount > 0) {
                likeCount--;
            }

            liked = false;

        } else {

            likeCount++;
            liked = true;
        }
    }

    // =========================
    // SAVE
    // =========================

    public void toggleSaved() {
        saved = !saved;
    }

    // =========================
    // SETTERS
    // =========================

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = Math.max(0, likeCount);
    }

    public void setProfileImageResId(int profileImageResId) {
        this.profileImageResId = profileImageResId;
    }

    public void setPostImageResId(int postImageResId) {
        this.postImageResId = postImageResId;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
