package com.example.myapplication;

public class Medication {

    private int medicationId;
    private String medicationName;

    public Medication(int id, String medicationName) {
        this.medicationId = id;
        this.medicationName = medicationName;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public int getMedicationId() {
        return medicationId;
    }
}
