package com.sanskritisathi.app;

public class CulturePost {

    private final String author;
    private final String category;
    private final String caption;
    private final String likeCount;
    private final int profileImageResId;
    private final int postImageResId;

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
        this.likeCount = likeCount;
        this.profileImageResId = profileImageResId;
        this.postImageResId = postImageResId;
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
        return likeCount;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public int getPostImageResId() {
        return postImageResId;
    }
}
