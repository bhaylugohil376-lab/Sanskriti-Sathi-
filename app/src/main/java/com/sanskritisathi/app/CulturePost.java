package com.sanskritisathi.app;

public class CulturePost {

    private final String author;
    private final String category;
    private final String caption;
    private int likeCount;
    private final int profileImageResId;
    private final int postImageResId;

    private boolean liked;
    private boolean saved;

    public CulturePost(
            String author,
            String category,
            String caption,
            String likeCount,
            int profileImageResId,
            int postImageResId) {

        this.author = author;
        this.category = category;
        this.caption = caption;

        int count = 0;

        try {
            count = Integer.parseInt(likeCount);
        } catch (Exception ignored) {
        }

        this.likeCount = count;
        this.profileImageResId = profileImageResId;
        this.postImageResId = postImageResId;

        this.liked = false;
        this.saved = false;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getCaption() {
        return caption;
    }

    public String getLikeCount() {
        return String.valueOf(likeCount);
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

    public void toggleLike() {

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

    public void toggleSaved() {
        saved = !saved;
    }
}
