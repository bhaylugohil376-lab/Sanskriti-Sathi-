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

    /*
     * Local drawable fallback.
     * Firebase post ke liye imageUrl ko priority milegi.
     */
    private int profileImageResId;
    private int postImageResId;

    private boolean liked;
    private boolean saved;

    public CulturePost() {
        // Required empty constructor
    }

    /*
     * Local/static post constructor
     */
    public CulturePost(
            String author,
            String category,
            String caption,
            int profileImageResId,
            int postImageResId
    ) {
        this.author = author;
        this.category = category;
        this.caption = caption;
        this.profileImageResId = profileImageResId;
        this.postImageResId = postImageResId;
        this.likeCount = 0;
        this.commentCount = 0;
        this.liked = false;
        this.saved = false;
    }

    /*
     * Firebase/basic constructor
     */
    public CulturePost(
            String id,
            String authorUid,
            String author,
            String category,
            String caption,
            String imageUrl,
            String visibility,
            long createdAt
    ) {
        this.id = id;
        this.authorUid = authorUid;
        this.author = author;
        this.category = category;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.likeCount = 0;
        this.commentCount = 0;
        this.profileImageResId = R.drawable.icon_foreground;
        this.postImageResId = R.drawable.icon_foreground;
        this.liked = false;
        this.saved = false;
    }

    /*
     * Full constructor
     */
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
            int postImageResId,
            boolean liked,
            boolean saved
    ) {
        this.id = id;
        this.authorUid = authorUid;
        this.author = author;
        this.category = category;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.profileImageResId = profileImageResId;
        this.postImageResId = postImageResId;
        this.liked = liked;
        this.saved = saved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public void setProfileImageResId(int profileImageResId) {
        this.profileImageResId = profileImageResId;
    }

    public int getPostImageResId() {
        return postImageResId;
    }

    public void setPostImageResId(int postImageResId) {
        this.postImageResId = postImageResId;
    }

    /*
     * Compatibility method for existing adapter/code.
     */
    public int getImageResId() {
        return postImageResId;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
