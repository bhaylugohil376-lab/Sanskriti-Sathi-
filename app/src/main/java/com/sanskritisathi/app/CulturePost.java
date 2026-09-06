package com.sanskritisathi.app;

public class CulturePost {

    private String id;
    private String authorUid;
    private String author;
    private String category;
    private String caption;

    private String imageUrl;
    private String visibility;
    private long createdAt;

    private int likeCount;
    private int commentCount;

    private int profileImageResId;
    private int postImageResId;

    private boolean liked;
    private boolean saved;

    // =====================================================
    // OLD CONSTRUCTOR
    // Existing local data ke liye compatible
    // =====================================================

    public CulturePost(
            String author,
            String category,
            String caption,
            int likeCount,
            int profileImageResId,
            int postImageResId
    ) {
        this(
                "",
                "",
                author,
                category,
                caption,
                "",
                "Public",
                0L,
                likeCount,
                0,
                profileImageResId,
                postImageResId
        );
    }

    // =====================================================
    // EXISTING 8-ARGUMENT CONSTRUCTOR
    // =====================================================

    public CulturePost(
            String id,
            String authorUid,
            String author,
            String category,
            String caption,
            int likeCount,
            int profileImageResId,
            int postImageResId
    ) {
        this(
                id,
                authorUid,
                author,
                category,
                caption,
                "",
                "Public",
                0L,
                likeCount,
                0,
                profileImageResId,
                postImageResId
        );
    }

    // =====================================================
    // FULL CONSTRUCTOR
    // =====================================================

    public CulturePost(
            String id,
            String authorUid,
            String author,
            String category,
            String caption,
            String imageUrl,
            String visibility,
            long createdAt,
            int likeCount,
            int commentCount,
            int profileImageResId,
            int postImageResId
    ) {
        this.id = id;
        this.authorUid = authorUid;
        this.author = author;
        this.category = category;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.visibility = visibility;
        this.createdAt = createdAt;

        this.likeCount = Math.max(0, likeCount);
        this.commentCount = Math.max(0, commentCount);

        this.profileImageResId = profileImageResId;
        this.postImageResId = postImageResId;

        this.liked = false;
        this.saved = false;
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public String getId() {
        return id;
    }

    public String getAuthorUid() {
        return authorUid;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
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

    // =====================================================
    // LIKE
    // =====================================================

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

    // =====================================================
    // SAVE
    // =====================================================

    public void toggleSaved() {
        saved = !saved;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = Math.max(0, likeCount);
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = Math.max(0, commentCount);
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
