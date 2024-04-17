package com.example.myapplication;

public enum TokenData {
    OPEN_AI_SERVICE_KEY("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"),
    ASSISTANT_ID("asst_hsjN0HRXL6uziWRULhiOdx7B");

    private final String token;

    TokenData(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}