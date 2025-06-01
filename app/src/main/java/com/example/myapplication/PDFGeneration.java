package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for generating PDF reports for patients.
 * This class uses the iText PDF library to create professionally formatted medical reports
 * with consistent styling, proper section organization, and page numbering.
 * 
 * The generated PDFs include sections for:
 * - BMI (Body Mass Index) analysis
 * - Metabolic balance assessment
 * - Health diagnostics information
 * - Patient feedback and self-assessment
 * 
 * If any of these sections are not present in the provided report content,
 * default sections with general information will be added automatically.
 */
public class PDFGeneration {
    private final Context context;

    /**
     * Constructs a new PDFGeneration instance with the specified context.
     * The context is used to access application resources and file storage.
     *
     * @param context The application context used for file operations
     */
    public PDFGeneration(Context context) {
        this.context = context;
    }

    /**
     * Creates a PDF file containing formatted medical report information.
     * This method processes the provided weekly report response, formats it into a structured PDF,
     * and saves it to the application's files directory in a patient-specific folder.
     * 
     * The method performs the following operations:
     * 1. Creates a unique filename based on patient ID and current timestamp
     * 2. Creates necessary directories for storing the PDF file
     * 3. Processes the weekly report response into properly formatted sections
     * 4. Adds any missing standard sections (BMI, metabolic, diagnostics, feedback)
     * 5. Adds page numbers to all pages
     * 6. Saves the PDF file to storage
     *
     * @param weeklyReportResponse The content to include in the PDF, formatted with Markdown-style headings
     * @param patientId The ID of the patient, used for filename generation and directory structure
     * @param patientName The name of the patient, used for creating a patient-specific directory
     * @return The generated PDF file
     */
    public File createPDF(String weeklyReportResponse, String patientId, String patientName) {
        String filename = "WeeklyReport.pdf";
        if (patientId != null) {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            filename = "report_" + patientId + "_" + timestamp + ".pdf";
        }

        File reportsDir = new File(context.getFilesDir(), "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        File patientDir = reportsDir;
        if (patientName != null && !patientName.isEmpty()) {
            String sanitizedName = patientName.replaceAll("[^a-zA-Z0-9]", "_");
            patientDir = new File(reportsDir, sanitizedName);
            if (!patientDir.exists()) {
                patientDir.mkdirs();
            }
        }

        File pdfFile = new File(patientDir, filename);
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
        try {
            PdfWriter writer = new PdfWriter(Files.newOutputStream(pdfFile.toPath()));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(36, 36, 36, 36);

            if (weeklyReportResponse == null) {
                weeklyReportResponse = "Error: No content available.";
            }
            String[] sections = weeklyReportResponse.split("(?=# |## |### )");

            boolean hasBMISection = false;
            boolean hasMetabolicSection = false;
            boolean hasDiagnosticsSection = false;
            boolean hasFeedbackSection = false;

            for (String section : sections) {
                if (section == null || section.trim().isEmpty()) continue;

                if (section.contains("BMI") || section.contains("Body Mass Index")) {
                    hasBMISection = true;
                }
                if (section.contains("Metabolic") || section.contains("Metabolism")) {
                    hasMetabolicSection = true;
                }
                if (section.contains("Diagnosis") || section.contains("Diagnostic")) {
                    hasDiagnosticsSection = true;
                }
                if (section.contains("Feedback") || section.contains("Patient Report")) {
                    hasFeedbackSection = true;
                }

                String[] lines = section.split("\n");
                for (String line : lines) {
                    if (line == null) continue;

                    if (line.startsWith("# ")) {
                        // Main title - large, centered, with accent color
                        Text text = new Text(line.replace("# ", ""))
                                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                .setFontSize(18)
                                .setFontColor(new DeviceRgb(0, 51, 102)); // Dark blue
                        Paragraph para = new Paragraph().add(text)
                                .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                                .setMarginTop(20.0f)
                                .setMarginBottom(10.0f);
                        document.add(para);
                    } else if (line.startsWith("## ")) {
                        // Section title - medium, left-aligned, with accent color
                        Text text = new Text(line.replace("## ", ""))
                                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                .setFontSize(16)
                                .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue
                        Paragraph para = new Paragraph().add(text)
                                .setMarginTop(15.0f)
                                .setMarginBottom(8.0f);
                        document.add(para);
                    } else if (line.startsWith("### ")) {
                        // Subsection title - smaller, left-aligned, with accent color
                        Text text = new Text(line.replace("### ", ""))
                                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                .setFontSize(14)
                                .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue
                        Paragraph para = new Paragraph().add(text)
                                .setMarginTop(10.0f)
                                .setMarginBottom(6.0f);
                        document.add(para);
                    } else if (line.contains("**")) {
                        // Text with bold emphasis
                        Paragraph paragraph = new Paragraph().setFontSize(12);
                        String[] parts = line.split("\\*\\*");
                        for (int j = 0; j < parts.length; j++) {
                            if (parts[j] == null) continue;

                            if (j % 2 == 0) {
                                paragraph.add(new Text(parts[j]));
                            } else {
                                paragraph.add(new Text(parts[j])
                                        .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                        .setFontColor(new DeviceRgb(51, 51, 51))); // Dark gray for emphasis
                            }
                        }
                        document.add(paragraph);
                    } else if (line.startsWith("- ") || line.startsWith("* ")) {
                        // Bullet points
                        String bulletText = line.substring(2);
                        Paragraph para = new Paragraph()
                                .setFontSize(12)
                                .setMarginLeft(20f)
                                .setFirstLineIndent(-10f);

                        Text bullet = new Text("• ")
                                .setFont(PdfFontFactory.createFont("Helvetica-Bold"));
                        para.add(bullet).add(bulletText);

                        document.add(para);
                    } else if (!line.trim().isEmpty()) {
                        // Regular paragraph text
                        Paragraph para = new Paragraph(line)
                                .setFontSize(12)
                                .setMarginBottom(6.0f);
                        document.add(para);
                    }
                }
            }

            if (!hasBMISection) {
                addBMISection(document);
            }

            if (!hasMetabolicSection) {
                addMetabolicSection(document);
            }

            if (!hasDiagnosticsSection) {
                addDiagnosticsSection(document);
            }

            if (!hasFeedbackSection) {
                addFeedbackSection(document);
            }

            try {
                int numberOfPages = pdf.getNumberOfPages();
                for (int i = 1; i <= numberOfPages; i++) {
                    com.itextpdf.kernel.pdf.PdfPage page = pdf.getPage(i);
                    if (page == null) {
                        continue;
                    }

                    try {
                        com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(page);

                        com.itextpdf.kernel.geom.Rectangle pageSize = page.getPageSize();
                        if (pageSize == null) {
                            continue;
                        }

                        // Create a canvas for writing content
                        com.itextpdf.layout.Canvas layoutCanvas = new com.itextpdf.layout.Canvas(canvas, pdf, pageSize);

                        // Create footer text
                        Text footerText = new Text(String.format("Page %d of %d", i, numberOfPages))
                                .setFontSize(10)
                                .setFontColor(new DeviceRgb(128, 128, 128));

                        // Create footer paragraph
                        Paragraph footer = new Paragraph(footerText)
                                .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER);

                        // Add footer to the page using the canvas
                        layoutCanvas.showTextAligned(footer, pageSize.getWidth() / 2, 20, i, 
                                com.itextpdf.layout.property.TextAlignment.CENTER, 
                                com.itextpdf.layout.property.VerticalAlignment.BOTTOM, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfFile;
    }

    /**
     * Adds a BMI (Body Mass Index) analysis section to the document if it's not already present.
     * This method creates a formatted section with the following components:
     * - A section title with medium blue color and bold font
     * - An explanatory paragraph describing what BMI is and its purpose
     * - A list of BMI categories with bullet points (Underweight, Normal weight, etc.)
     * - A note about BMI limitations as a screening tool
     * 
     * This section provides educational information about BMI interpretation
     * and is added automatically if the weekly report doesn't include BMI information.
     *
     * @param document The PDF document to add the section to
     * @throws IOException If there is an error creating or adding content to the document
     */
    private void addBMISection(Document document) throws IOException {
        // Add section title
        Text titleText = new Text("Body Mass Index (BMI) Analysis")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(16)
                .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

        Paragraph titlePara = new Paragraph().add(titleText)
                .setMarginTop(15.0f)
                .setMarginBottom(8.0f);

        document.add(titlePara);

        // Add explanatory text
        Paragraph explanationPara = new Paragraph(
                "BMI is a measure of body fat based on height and weight. " +
                "It is used to screen for weight categories that may lead to health problems.")
                .setFontSize(12)
                .setMarginBottom(6.0f);

        document.add(explanationPara);

        // Add BMI categories
        Paragraph categoriesPara = new Paragraph("BMI Categories:")
                .setFontSize(12)
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setMarginTop(10.0f)
                .setMarginBottom(6.0f);

        document.add(categoriesPara);

        // Add bullet points for categories
        String[] categories = {
                "Underweight: BMI less than 18.5",
                "Normal weight: BMI 18.5-24.9",
                "Overweight: BMI 25-29.9",
                "Obesity (Class 1): BMI 30-34.9",
                "Obesity (Class 2): BMI 35-39.9",
                "Extreme Obesity (Class 3): BMI 40 or higher"
        };

        for (String category : categories) {
            Paragraph para = new Paragraph()
                    .setFontSize(12)
                    .setMarginLeft(20f)
                    .setFirstLineIndent(-10f);

            Text bullet = new Text("• ")
                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"));
            para.add(bullet).add(category);

            document.add(para);
        }

        // Add note about limitations
        Paragraph notePara = new Paragraph(
                "Note: BMI is a screening tool and does not directly measure body fat or account " +
                "for factors such as muscle mass, bone density, and overall body composition.")
                .setFontSize(12)
                .setMarginTop(10.0f)
                .setFontColor(new DeviceRgb(102, 102, 102)); // Gray text

        document.add(notePara);
    }

    /**
     * Adds a metabolic balance analysis section to the document if it's not already present.
     * This method creates a formatted section with the following components:
     * - A section title with medium blue color and bold font
     * - An explanatory paragraph describing metabolic balance and its influences
     * - A subsection for key metabolic indicators with bullet points
     *   (Resting Heart Rate, Active Zone Minutes, Steps per Day, etc.)
     * - A subsection with general recommendations for optimal metabolic health
     * 
     * This section provides educational information about metabolic health
     * and practical recommendations for maintaining metabolic balance.
     * It is added automatically if the weekly report doesn't include metabolic information.
     *
     * @param document The PDF document to add the section to
     * @throws IOException If there is an error creating or adding content to the document
     */
    private void addMetabolicSection(Document document) throws IOException {
        // Add section title
        Text titleText = new Text("Metabolic Balance Analysis")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(16)
                .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

        Paragraph titlePara = new Paragraph().add(titleText)
                .setMarginTop(15.0f)
                .setMarginBottom(8.0f);

        document.add(titlePara);

        // Add explanatory text
        Paragraph explanationPara = new Paragraph(
                "Metabolic balance refers to the equilibrium between energy intake and expenditure. " +
                "It is influenced by physical activity, resting metabolic rate, and dietary habits.")
                .setFontSize(12)
                .setMarginBottom(6.0f);

        document.add(explanationPara);

        // Add subsection for key metrics
        Text metricsText = new Text("Key Metabolic Indicators")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(14)
                .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

        Paragraph metricsPara = new Paragraph().add(metricsText)
                .setMarginTop(10.0f)
                .setMarginBottom(6.0f);

        document.add(metricsPara);

        // Add bullet points for metrics
        String[] metrics = {
                "Resting Heart Rate: Indicator of cardiovascular fitness",
                "Active Zone Minutes: Measure of moderate to intense physical activity",
                "Steps per Day: Indicator of overall activity level",
                "Sedentary Minutes: Time spent with minimal physical movement",
                "Sleep Quality: Important for metabolic regulation and recovery"
        };

        for (String metric : metrics) {
            Paragraph para = new Paragraph()
                    .setFontSize(12)
                    .setMarginLeft(20f)
                    .setFirstLineIndent(-10f);

            Text bullet = new Text("• ")
                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"));
            para.add(bullet).add(metric);

            document.add(para);
        }

        // Add recommendations
        Text recommendationsText = new Text("General Recommendations")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(14)
                .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

        Paragraph recommendationsPara = new Paragraph().add(recommendationsText)
                .setMarginTop(10.0f)
                .setMarginBottom(6.0f);

        document.add(recommendationsPara);

        Paragraph recommendationsContentPara = new Paragraph(
                "For optimal metabolic health, aim for at least 150 minutes of moderate-intensity " +
                "aerobic activity per week, maintain a balanced diet rich in whole foods, ensure " +
                "adequate hydration, and prioritize quality sleep of 7-9 hours per night.")
                .setFontSize(12)
                .setMarginBottom(6.0f);

        document.add(recommendationsContentPara);
    }

    /**
     * Adds a health diagnostics section to the document if it's not already present.
     * This method creates a formatted section with the following components:
     * - A section title with medium blue color and bold font
     * - An explanatory paragraph describing the purpose of health diagnostics
     * - Three subsections covering different diagnostic areas:
     *   1. Cardiovascular Health (HRV and VO2 Max information)
     *   2. Respiratory Function (breathing rate information)
     *   3. Sleep Quality (sleep stages and factors affecting sleep quality)
     * - A note about consulting healthcare providers for proper diagnosis
     * 
     * This section provides educational information about key health indicators
     * based on Fitbit data and their significance for overall health assessment.
     * It is added automatically if the weekly report doesn't include diagnostics information.
     *
     * @param document The PDF document to add the section to
     * @throws IOException If there is an error creating or adding content to the document
     */
    private void addDiagnosticsSection(Document document) throws IOException {
        // Add section title
        Text titleText = new Text("Health Diagnostics")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(16)
                .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

        Paragraph titlePara = new Paragraph().add(titleText)
                .setMarginTop(15.0f)
                .setMarginBottom(8.0f);

        document.add(titlePara);

        // Add explanatory text
        Paragraph explanationPara = new Paragraph(
                "This section provides an analysis of key health indicators based on your Fitbit data. " +
                "These metrics can help identify potential health concerns and track progress over time.")
                .setFontSize(12)
                .setMarginBottom(6.0f);

        document.add(explanationPara);

        // Add subsections for different diagnostic areas
        String[][] diagnosticAreas = {
                {"Cardiovascular Health", 
                 "Heart rate variability (HRV) is a measure of the variation in time between heartbeats. " +
                 "Higher HRV generally indicates better cardiovascular health and stress resilience. " +
                 "VO2 Max (cardio fitness score) represents your body's maximum oxygen utilization during exercise."},

                {"Respiratory Function", 
                 "Breathing rate at rest typically ranges from 12-20 breaths per minute. " +
                 "Consistently elevated rates may indicate respiratory or cardiovascular issues."},

                {"Sleep Quality", 
                 "Sleep is divided into stages: light, deep, and REM. Deep sleep is essential for physical recovery, " +
                 "while REM sleep supports cognitive function and emotional regulation. " +
                 "Factors like sleep duration, efficiency, and consistency all contribute to overall sleep quality."}
        };

        for (String[] area : diagnosticAreas) {
            // Add subsection title
            Text areaText = new Text(area[0])
                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                    .setFontSize(14)
                    .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

            Paragraph areaPara = new Paragraph().add(areaText)
                    .setMarginTop(10.0f)
                    .setMarginBottom(6.0f);

            document.add(areaPara);

            // Add subsection content
            Paragraph contentPara = new Paragraph(area[1])
                    .setFontSize(12)
                    .setMarginBottom(10.0f);

            document.add(contentPara);
        }

        // Add note about consulting healthcare providers
        Paragraph notePara = new Paragraph(
                "Note: This information is not intended to replace professional medical advice. " +
                "Always consult with your healthcare provider for proper diagnosis and treatment.")
                .setFontSize(12)
                .setMarginTop(10.0f)
                .setFontColor(new DeviceRgb(102, 102, 102)); // Gray text

        document.add(notePara);
    }

    /**
     * Adds a patient feedback and self-assessment section to the document if it's not already present.
     * This method creates a formatted section with the following components:
     * - A section title with medium blue color and bold font
     * - An explanatory paragraph describing the importance of patient feedback
     * - A subsection for general health assessment with placeholder content
     * - A subsection for condition-specific feedback with placeholder content
     * - A note about the importance of patient self-assessment and regular communication
     * 
     * This section serves as a placeholder for patient-reported information and emphasizes
     * the value of patient feedback in comprehensive healthcare assessment.
     * It is added automatically if the weekly report doesn't include patient feedback information.
     * The placeholder content directs readers to refer to weekly questionnaire responses for
     * more specific patient feedback.
     *
     * @param document The PDF document to add the section to
     * @throws IOException If there is an error creating or adding content to the document
     */
    private void addFeedbackSection(Document document) throws IOException {
        // Add section title
        Text titleText = new Text("Patient Feedback & Self-Assessment")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(16)
                .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

        Paragraph titlePara = new Paragraph().add(titleText)
                .setMarginTop(15.0f)
                .setMarginBottom(8.0f);

        document.add(titlePara);

        // Add explanatory text
        Paragraph explanationPara = new Paragraph(
                "This section summarizes the patient's self-reported health status and concerns. " +
                "Patient feedback is a valuable component of comprehensive healthcare assessment.")
                .setFontSize(12)
                .setMarginBottom(10.0f);

        document.add(explanationPara);

        // Add placeholder for patient's general health assessment
        Text generalHealthText = new Text("General Health Assessment")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(14)
                .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

        Paragraph generalHealthPara = new Paragraph().add(generalHealthText)
                .setMarginTop(10.0f)
                .setMarginBottom(6.0f);

        document.add(generalHealthPara);

        Paragraph generalHealthContentPara = new Paragraph(
                "The patient has not provided specific feedback on their general health status. " +
                "Please refer to the weekly questionnaire responses for more information.")
                .setFontSize(12)
                .setMarginBottom(10.0f);

        document.add(generalHealthContentPara);

        // Add placeholder for condition-specific feedback
        Text conditionsText = new Text("Condition-Specific Feedback")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(14)
                .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

        Paragraph conditionsPara = new Paragraph().add(conditionsText)
                .setMarginTop(10.0f)
                .setMarginBottom(6.0f);

        document.add(conditionsPara);

        Paragraph conditionsContentPara = new Paragraph(
                "The patient has not provided specific feedback on their diagnosed conditions. " +
                "Please refer to the weekly questionnaire responses for more information.")
                .setFontSize(12)
                .setMarginBottom(10.0f);

        document.add(conditionsContentPara);

        // Add note about the importance of patient feedback
        Paragraph notePara = new Paragraph(
                "Note: Patient self-assessment is an important complement to objective health metrics. " +
                "Regular communication between patient and healthcare provider is encouraged for optimal care.")
                .setFontSize(12)
                .setMarginTop(10.0f)
                .setFontColor(new DeviceRgb(102, 102, 102)); // Gray text

        document.add(notePara);
    }
}
