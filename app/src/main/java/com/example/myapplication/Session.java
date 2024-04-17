package com.example.myapplication;

public class Session {

    private String id;
    private final String keyString;
    private final String value;

    public Session(String id, String keyString, String value) {
        this.id = id;
        this.keyString = keyString;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyString() {
        return keyString;
    }

    public String getValue() {
        return value;
    }
}