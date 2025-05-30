package com.example.myapplication;

/**
 * Model class to represent an ICD-10 search result
 */
public class Icd10SearchResult {
    private String code;
    private String name;

    public Icd10SearchResult(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}