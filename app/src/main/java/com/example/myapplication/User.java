package com.example.myapplication;

public class User {
    private final String userId;
    private final UserProfile userProfile;
    private final String role;

    public User(String userId, UserProfile userProfile, String role) {
        this.userId = userId;
        this.userProfile = userProfile;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public String getRole() {
        return role;
    }
}
