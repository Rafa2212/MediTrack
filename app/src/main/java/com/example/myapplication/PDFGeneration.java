package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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

public class PDFGeneration {
    private Context context;

    public PDFGeneration(Context context) {
        this.context = context;
    }

    public File createPDF(String weeklyReportResponse) {
        return createPDF(weeklyReportResponse, null);
    }

    public File createPDF(String weeklyReportResponse, String patientId) {
        // Create a unique filename based on patient ID and current date if patientId is provided
        String filename = "WeeklyReport.pdf";
        if (patientId != null) {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            filename = "report_" + patientId + "_" + timestamp + ".pdf";
        }

        // Create reports directory if it doesn't exist
        File reportsDir = new File(context.getFilesDir(), "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        File pdfFile = new File(reportsDir, filename);
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Set document properties for better formatting
            document.setMargins(36, 36, 36, 36); // 0.5 inch margins

            // Parse the response to identify sections
            String[] sections = weeklyReportResponse.split("(?=# |## |### )");

            // Variables to track sections
            boolean hasBMISection = false;
            boolean hasMetabolicSection = false;
            boolean hasDiagnosticsSection = false;
            boolean hasFeedbackSection = false;

            // Process each section
            for (String section : sections) {
                if (section.trim().isEmpty()) continue;

                // Identify section type
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

                // Process the section line by line
                String[] lines = section.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];

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

            // Add missing sections if needed
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

            // Add footer with page numbers
            int numberOfPages = pdf.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                // Create footer text
                Text footerText = new Text(String.format("Page %d of %d", i, numberOfPages))
                        .setFontSize(10)
                        .setFontColor(new DeviceRgb(128, 128, 128));

                // Add footer to each page
                Paragraph footer = new Paragraph(footerText)
                        .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                        .setFixedPosition(i, 36, 20, pdf.getDefaultPageSize().getWidth() - 72);

                document.add(footer);
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfFile;
    }

    public void openPDF(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Adds a BMI analysis section to the document if it's not already present
     * @param document The PDF document to add the section to
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
     * Adds a metabolic balance section to the document if it's not already present
     * @param document The PDF document to add the section to
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
     * Adds a diagnostics section to the document if it's not already present
     * @param document The PDF document to add the section to
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
     * Adds a patient feedback section to the document if it's not already present
     * @param document The PDF document to add the section to
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
