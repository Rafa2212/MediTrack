package com.example.myapplication;

/**
 * Represents a medical report for a patient
 */
public class MedicalReport {
    private final String reportId;
    private final String patientId;
    private final String doctorId;
    private final String reportDate;
    private final String reportContent;
    private final String reportPath;

    /**
     * Constructor for MedicalReport
     * @param reportId The ID of the report
     * @param patientId The ID of the patient
     * @param doctorId The ID of the doctor who created the report
     * @param reportDate The date of the report
     * @param reportContent The content of the report
     * @param reportPath The path to the PDF file
     */
    public MedicalReport(String reportId, String patientId, String doctorId, String reportDate, String reportContent, String reportPath) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.reportDate = reportDate;
        this.reportContent = reportContent;
        this.reportPath = reportPath;
    }

    /**
     * Gets the report ID
     * @return The report ID
     */
    public String getReportId() {
        return reportId;
    }

    /**
     * Gets the patient ID
     * @return The patient ID
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Gets the doctor ID
     * @return The doctor ID
     */
    public String getDoctorId() {
        return doctorId;
    }

    /**
     * Gets the report date
     * @return The report date
     */
    public String getReportDate() {
        return reportDate;
    }

    /**
     * Gets the report content
     * @return The report content
     */
    public String getReportContent() {
        return reportContent;
    }

    /**
     * Gets the report path
     * @return The report path
     */
    public String getReportPath() {
        return reportPath;
    }
}
