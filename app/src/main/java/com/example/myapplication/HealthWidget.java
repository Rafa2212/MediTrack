package com.example.myapplication;

public class HealthWidget {
    private final String type;
    private final String title;
    private final String description;
    private final int imageResource;
    private final String interpretation;

    public HealthWidget(String type, String title, String description, int imageResource, String interpretation) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.interpretation = interpretation;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getInterpretation() {
        return interpretation;
    }
}