package com.example.myapplication;

import java.util.List;

public class UserProfile {
    private String name;
    private int age;
    private float height;
    private float weight;
    private int bloodPressure;
    private int heartRate;

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