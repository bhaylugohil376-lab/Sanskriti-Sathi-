package com.sanskritisathi.app;

public class UserProfile {

    private String uid;
    private String name;
    private String username;
    private String email;
    private String bio;
    private String profileImageUrl;

    public UserProfile() {
        // Firebase Firestore ke liye empty constructor
    }

    public UserProfile(
            String uid,
            String name,
            String username,
            String email,
            String bio,
            String profileImageUrl) {

        this.uid = uid;
        this.name = name;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
