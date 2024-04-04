// Disease.java
package com.example.myapplication;

public class Disease {

    private int diseaseId;
    private String name;

    public Disease(int id, String name) {
        this.diseaseId = id;
        this.name = name;
    }

    public int getDiseaseId() {
        return diseaseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}