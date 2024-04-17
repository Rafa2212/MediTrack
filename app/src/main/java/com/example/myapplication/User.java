package com.example.myapplication;

public class User {
    private String userId;
    private String username;
    private String password;
    private boolean profileCompleted;
    private UserProfile userProfile;


    public User(String userId, String username, String password, UserProfile userProfile) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userProfile = userProfile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}