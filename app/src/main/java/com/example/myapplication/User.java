package com.example.myapplication;

public class User {
    private final String userId;
    private final UserProfile userProfile;


    public User(String userId, UserProfile userProfile) {
        this.userId = userId;
        this.userProfile = userProfile;
    }

    public String getUserId() {
        return userId;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }
}