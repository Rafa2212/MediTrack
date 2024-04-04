package com.example.myapplication;

public class Medication {

    private int medicationId;
    private String medicationName;
    private String dosage;
    private String frequency;

    public Medication(int id, String medicationName, String dosage, String frequency) {
        this.medicationId = id;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }
}
