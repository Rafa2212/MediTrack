package com.example.myapplication;

public class Disease {

    private final String ICD10;
    private int diseaseId;
    private String name;
    private String interpretation;
    private String doctorName;
    private String doctorId;
    private String diagnosisDate;

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

    public Disease(int id, String name, String ICD10, String doctorName, String diagnosisDate) {
        this.diseaseId = id;
        this.name = name;
        this.ICD10 = ICD10;
        this.doctorName = doctorName;
        this.diagnosisDate = diagnosisDate;
        this.doctorId = null;
    }

    public Disease(int id, String name, String ICD10, String doctorName, String doctorId, String diagnosisDate) {
        this.diseaseId = id;
        this.name = name;
        this.ICD10 = ICD10;
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.diagnosisDate = diagnosisDate;
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

    public String getDoctorName() {
        return doctorName;
    }

    public String getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(String diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoctorId() {
        return doctorId;
    }
}
