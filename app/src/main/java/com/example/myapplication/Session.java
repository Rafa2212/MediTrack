package com.example.myapplication;

public class Session {

    private String id;
    private String userId;
    private String keyString;
    private String value;

    public Session (){}

    public Session(String id, String userId, String keyString, String value) {
        this.id = id;
        this.userId = userId;
        this.keyString = keyString;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyString() {
        return keyString;
    }

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}