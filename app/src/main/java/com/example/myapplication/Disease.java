package com.example.myapplication;

public class Disease {

    private final String ICD10;
    private int diseaseId;
    private String name;
    private String interpretation;

    public Disease (String icd10, String name, String interpretation){
        this.ICD10 = icd10;
        this.name = name;
        this.interpretation = interpretation;
    }

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

    public String getInterpretation() {
        return interpretation;
    }

    public void setName(String name) {
        this.name = name;
    }
}