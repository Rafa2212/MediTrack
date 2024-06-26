// Disease.java
package com.example.myapplication;

public class Disease {

    private String ICD10;
    private int diseaseId;
    private String name;

    public Disease(int id, String name, String ICD10) {
        this.diseaseId = id;
        this.name = name;
        this.ICD10 = ICD10;
    }

    public int getDiseaseId() {
        return diseaseId;
    }

    public String getName() {
        return name;
    }

    public String getICD10() {
        return ICD10;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setICD10(String ICD10) {
        this.ICD10 = ICD10;
    }
}