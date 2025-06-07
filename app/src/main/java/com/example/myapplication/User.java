package com.example.myapplication;

public class User {
    private final String userId;
    private final Patient patient;
    private final Doctor doctor;
    private final String role;

    public User(String userId, Patient patient, String role) {
        this.userId = userId;
        this.patient = patient;
        this.doctor = null;
        this.role = role;
    }

    public User(String userId, Doctor doctor, String role) {
        this.userId = userId;
        this.patient = null;
        this.doctor = doctor;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getRole() {
        return role;
    }

    // For backward compatibility
    public Object getUserProfile() {
        if ("doctor".equals(role)) {
            return doctor;
        } else {
            return patient;
        }
    }
}