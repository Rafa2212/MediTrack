package com.example.myapplication;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String password;
    private boolean profileCompleted;
    private String name;
    private int age;
    private float height;
    private float weight;
    private List<Disease> knownDiseases;
    private List<Medication> knownMedications;

    public User(String userId, String username, String password, boolean profileCompleted, String name, int age, float height, float weight, List<Disease> knownDiseases, List<Medication> knownMedications) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.profileCompleted = profileCompleted;
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.knownDiseases = knownDiseases;
        this.knownMedications = knownMedications;
    }

    public User(String userId, String username, String password, boolean profileCompleted) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.profileCompleted = profileCompleted;
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

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
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

    public void setAge(int age) {
        this.age = age;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public List<Disease> getKnownDiseases() {
        return knownDiseases;
    }

    public void setKnownDiseases(List<Disease> knownDiseases) {
        this.knownDiseases = knownDiseases;
    }

    public List<Medication> getKnownMedications() {
        return knownMedications;
    }

    public void setKnownMedications(List<Medication> knownMedications) {
        this.knownMedications = knownMedications;
    }
}