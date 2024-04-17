package com.example.myapplication;

public class UserProfile {
    private String name;
    private final int age;
    private final float height;
    private final float weight;
    private final int bloodPressure;
    private final int heartRate;

    public UserProfile(String name, int age, float height, float weight, int bloodPressure, int heartrate) {
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartrate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public int getBloodPressure() {
        return bloodPressure;
    }

    public int getHeartrate() {
        return heartRate;
    }
}