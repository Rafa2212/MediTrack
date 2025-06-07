package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Utility class for generating PDF reports for patients.
 * This class uses the iText PDF library to create professionally formatted medical reports
 * with consistent styling, proper section organization, and page numbering.
 * The generated PDFs include sections for:
 * - BMI (Body Mass Index) analysis
 * - Metabolic balance assessment
 * - Health diagnostics information
 * - Patient feedback and self-assessment
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
     * Creates a PDF file by combining a template first page with patient-specific data pages.
     * This method loads the first page from a template PDF, then adds additional pages with
     * minimalist content focused on patient-specific data and interpretations.
     * The method performs the following operations:
     * 1. Creates a unique filename based on patient ID and current timestamp
     * 2. Creates necessary directories for storing the PDF file
     * 3. Loads the template PDF from resources and uses its first page
     * 4. Adds additional pages with patient-specific data (BMI, Metabolic Balance, etc.)
     * 5. Implements comparison with the last report if available
     * 6. Adds page numbers to all pages
     * 7. Saves the PDF file to storage
     *
     * @param weeklyReportResponse The content to include in the PDF, formatted with Markdown-style headings
     * @param patientId The ID of the patient, used for filename generation
     * @param patient The Patient object containing patient data for personalized insights
     * @return The generated PDF file
     */
    public File createPDF(String weeklyReportResponse, String patientId, Patient patient) {
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
        if (patient != null && patient.getName() != null && !patient.getName().isEmpty()) {
            patientDir = new File(reportsDir, patient.getName().replace(" ", "_"));
            if (!patientDir.exists()) {
                patientDir.mkdirs();
            }
        }

        File pdfFile = new File(patientDir, filename);
        if (pdfFile.exists()) {
            pdfFile.delete();
        }

        // Create a temporary file to store the template
        File tempFile = null;
        try {
            int resourceId = context.getResources().getIdentifier(
                    "meditrack_template_report", "raw", context.getPackageName());

            if (resourceId == 0) {
                return createFallbackPDF(weeklyReportResponse, patientId, patient);
            }

            tempFile = File.createTempFile("template", ".pdf", context.getCacheDir());
            try (InputStream inputStream = context.getResources().openRawResource(resourceId);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            // Copy the first page from the template
            PdfReader templateReader = new PdfReader(tempFile);
            PdfWriter writer = new PdfWriter(Files.newOutputStream(pdfFile.toPath()));
            PdfDocument pdfDoc = new PdfDocument(writer);

            Document document = new Document(pdfDoc);
            document.setMargins(36, 36, 36, 36);

            PdfDocument templatePdfDoc = new PdfDocument(templateReader);
            if (templatePdfDoc.getNumberOfPages() > 0) {
                templatePdfDoc.copyPagesTo(1, 1, pdfDoc);
            }
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            MedicalReport previousReport = null;
            if (patientId != null) {
                previousReport = dbHelper.getLatestMedicalReportForPatient(patientId);
            }

            addPatientDataPages(document, weeklyReportResponse, patient, previousReport);
            document.close();
            templatePdfDoc.close();
            templateReader.close();

            return pdfFile;
        } catch (IOException e) {
            e.printStackTrace();
            return createFallbackPDF(weeklyReportResponse, patientId, patient);
        } finally {
            // Delete the temporary file in the finally block to ensure it's always deleted after used
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Adds patient-specific data pages to the PDF document.
     * This method creates minimalist pages focused on patient-specific data and interpretations.
     * 
     * @param document The PDF document to add pages to
     * @param weeklyReportResponse The content to include in the PDF
     * @param patient The Patient object containing patient data
     * @param previousReport The previous medical report for comparison, or null if not available
     * @throws IOException If there is an error adding content to the document
     */
    private void addPatientDataPages(Document document, String weeklyReportResponse, Patient patient, MedicalReport previousReport) throws IOException {
        document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4));
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        Text titleText = new Text("Weekly Report " + formattedDate)
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(12)
                .setFontColor(new DeviceRgb(0, 51, 102)); // Dark blue

        Paragraph titlePara = new Paragraph().add(titleText)
                .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                .setMarginTop(15.0f)
                .setMarginBottom(8.0f);

        document.add(titlePara);

        String[] sections = weeklyReportResponse.split("(?=# |## |### )");

        for (String section : sections) {
            if (section == null || section.trim().isEmpty()) continue;

            if (section.contains("Clinical Recommendations") ||
                section.contains("CLINICAL RECOMMENDATIONS") ||
                section.contains("Recommendations") && section.startsWith("##")) {
                continue;
            }

            String[] lines = section.split("\n");
            for (String line : lines) {
                if (line == null) continue;

                if (line.startsWith("# ")) {
                    continue;
                } else if (line.startsWith("## ")) {
                    Text text = new Text(line.replace("## ", ""))
                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                            .setFontSize(10)
                            .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

                    float yPosition = document.getRenderer().getCurrentArea().getBBox().getTop();
                    if (yPosition < 100) { // If less than 100 points from bottom, add page break
                        document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4));
                    }

                    Paragraph para = new Paragraph().add(text)
                            .setMarginTop(10.0f)
                            .setMarginBottom(5.0f);

                    document.add(para);
                } else if (line.startsWith("### ")) {
                    Text text = new Text(line.replace("### ", ""))
                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                            .setFontSize(9)
                            .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

                    float yPosition = document.getRenderer().getCurrentArea().getBBox().getTop();
                    if (yPosition < 80) { // If less than 80 points from bottom, add page break
                        document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4));
                    }

                    Paragraph para = new Paragraph().add(text)
                            .setMarginTop(8.0f)
                            .setMarginBottom(4.0f);

                    document.add(para);
                } else if (line.contains("**")) {
                    Paragraph paragraph = new Paragraph().setFontSize(9);
                    String[] parts = line.split("\\*\\*");
                    for (int j = 0; j < parts.length; j++) {
                        if (parts[j] == null) continue;

                        if (j % 2 == 0) {
                            paragraph.add(new Text(parts[j]));
                        } else {
                            if (patient != null && parts[j].equals(patient.getName())) {
                                paragraph.add(new Text(parts[j])
                                        .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                        .setFontColor(new DeviceRgb(0, 102, 153))); // Medium blue for patient name
                            } else {
                                paragraph.add(new Text(parts[j])
                                        .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                        .setFontColor(new DeviceRgb(51, 51, 51))); // Dark gray for emphasis
                            }
                        }
                    }

                    document.add(paragraph);
                } else if (line.startsWith("- ") || line.startsWith("* ")) {
                    String bulletText = line.substring(2);

                    if (bulletText.contains("data from Fitbit") || bulletText.contains("according to Fitbit")) {
                    } else if (bulletText.contains("steps") || bulletText.contains("activity") ||
                              bulletText.contains("heart rate") || bulletText.contains("sleep")) {
                        bulletText += " (data from Fitbit)";
                    }

                    Paragraph para = new Paragraph()
                            .setFontSize(9)
                            .setMarginLeft(15f)
                            .setFirstLineIndent(-8f);

                    Text bullet = new Text("• ")
                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"));
                    para.add(bullet).add(bulletText);

                    document.add(para);
                } else if (!line.trim().isEmpty()) {
                    String processedLine = line;

                    if (processedLine.endsWith("#")) {
                        processedLine = processedLine.replaceAll("\\s*#\\s*$", "");
                    }

                    if (!processedLine.contains("data from Fitbit") && !processedLine.contains("according to Fitbit")) {
                        if (processedLine.contains("steps") || processedLine.contains("activity") ||
                            processedLine.contains("heart rate") || processedLine.contains("sleep")) {
                            processedLine += " (data from Fitbit)";
                        }
                    }

                    if (processedLine.contains("{") && processedLine.contains("}")) {
                        StringBuilder currentText = new StringBuilder();
                        boolean inCenteredSection = false;

                        for (int i = 0; i < processedLine.length(); i++) {
                            char c = processedLine.charAt(i);

                            if (c == '{' && !inCenteredSection) {
                                if (currentText.length() > 0) {
                                    Paragraph normalPara = new Paragraph(currentText.toString())
                                            .setFontSize(9)
                                            .setMarginBottom(4.0f);
                                    document.add(normalPara);
                                    currentText = new StringBuilder();
                                }
                                inCenteredSection = true;
                            } else if (c == '}' && inCenteredSection) {
                                Paragraph centeredPara = new Paragraph(currentText.toString())
                                        .setFontSize(9)
                                        .setMarginBottom(4.0f)
                                        .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER);
                                document.add(centeredPara);
                                currentText = new StringBuilder();
                                inCenteredSection = false;
                            } else {
                                currentText.append(c);
                            }
                        }

                        if (currentText.length() > 0) {
                            Paragraph finalPara = new Paragraph(currentText.toString())
                                    .setFontSize(9)
                                    .setMarginBottom(4.0f);
                            document.add(finalPara);
                        }
                    } else {
                        Paragraph para = new Paragraph(processedLine)
                                .setFontSize(9)
                                .setMarginBottom(4.0f);
                        document.add(para);
                    }
                }
            }
        }
    }

    /**
     * Creates a fallback PDF using the original method if there's an error with the template approach.
     *
     * @param weeklyReportResponse The content to include in the PDF
     * @param patientId The ID of the patient
     * @param patient The Patient object containing patient data
     * @return The generated PDF file
     */
    private File createFallbackPDF(String weeklyReportResponse, String patientId, Patient patient) {
        String filename = "WeeklyReport.pdf";
        if (patientId != null) {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            filename = "report_" + patientId + "_" + timestamp + ".pdf";
        }

        File reportsDir = new File(context.getFilesDir(), "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        // Create user-specific directory
        File patientDir = reportsDir;
        if (patient != null && patient.getName() != null && !patient.getName().isEmpty()) {
            patientDir = new File(reportsDir, patient.getName().replace(" ", "_"));
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

            Text coverTitleText = new Text("MediTrack Weekly Report")
                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                    .setFontSize(16)
                    .setFontColor(new DeviceRgb(0, 51, 102)); // Dark blue

            Paragraph coverTitlePara = new Paragraph().add(coverTitleText)
                    .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                    .setMarginTop(250.0f); // Position in the middle of the page

            document.add(coverTitlePara);

            if (patient != null && patient.getName() != null && !patient.getName().isEmpty()) {
                Text patientNameText = new Text(patient.getName())
                        .setFont(PdfFontFactory.createFont("Helvetica"))
                        .setFontSize(12)
                        .setFontColor(new DeviceRgb(0, 51, 102)); // Dark blue

                Paragraph patientNamePara = new Paragraph().add(patientNameText)
                        .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                        .setMarginTop(20.0f);

                document.add(patientNamePara);
            }

            String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            Text dateText = new Text(formattedDate)
                    .setFont(PdfFontFactory.createFont("Helvetica"))
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(0, 51, 102)); // Dark blue

            Paragraph datePara = new Paragraph().add(dateText)
                    .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                    .setMarginTop(20.0f);

            document.add(datePara);

            document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4));

            if (weeklyReportResponse == null) {
                weeklyReportResponse = "Error: No content available.";
            }
            String[] sections = weeklyReportResponse.split("(?=# |## |### )");

            for (String section : sections) {
                String[] lines = section.split("\n");
                for (String line : lines) {
                    if (line == null) continue;

                    if (line.startsWith("# ")) {
                        String titleText = line.replace("# ", "");
                        if (titleText.contains("WEEKLY") && titleText.contains("REPORT")) {
                            String titleFormattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                            titleText = "Weekly Report " + titleFormattedDate;
                        }

                        Text text = new Text(titleText)
                                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                .setFontSize(12)
                                .setFontColor(new DeviceRgb(0, 51, 102)); // Dark blue
                        Paragraph para = new Paragraph().add(text)
                                .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                                .setMarginTop(15.0f)
                                .setMarginBottom(8.0f);
                        document.add(para);

                    } else if (line.startsWith("## ")) {
                        String titleText = line.replace("## ", "");
                        Text text;
                        Paragraph para;

                        if (titleText.equals("I. Introduction") ||
                            titleText.equals("II. Summary of the Feedback") ||
                            titleText.equals("III. Observations and Trends") ||
                            titleText.equals("IV. Conclusion")) {

                            text = new Text(titleText)
                                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                    .setFontSize(14)
                                    .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

                            para = new Paragraph().add(text)
                                    .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                                    .setMarginTop(15.0f)
                                    .setMarginBottom(8.0f);
                        } else {
                            text = new Text(titleText)
                                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                    .setFontSize(10)
                                    .setFontColor(new DeviceRgb(0, 102, 153)); // Medium blue

                            para = new Paragraph().add(text)
                                    .setMarginTop(10.0f)
                                    .setMarginBottom(5.0f);
                        }

                        float yPosition = document.getRenderer().getCurrentArea().getBBox().getTop();
                        if (yPosition < 100) { // If less than 100 points from bottom, add page break
                            document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4));
                        }

                        document.add(para);
                    } else if (line.startsWith("### ")) {
                        Text text = new Text(line.replace("### ", ""))
                                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                .setFontSize(9)
                                .setFontColor(new DeviceRgb(51, 153, 204)); // Light blue

                        float yPosition = document.getRenderer().getCurrentArea().getBBox().getTop();
                        if (yPosition < 80) { // If less than 80 points from bottom, add page break
                            document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4));
                        }

                        Paragraph para = new Paragraph().add(text)
                                .setMarginTop(8.0f)
                                .setMarginBottom(4.0f);
                        document.add(para);
                    } else if (line.contains("**")) {
                        Paragraph paragraph = new Paragraph().setFontSize(9);
                        String[] parts = line.split("\\*\\*");

                        for (int j = 0; j < parts.length; j++) {
                            if (parts[j] == null) continue;

                            if (j % 2 == 0) {
                                paragraph.add(new Text(parts[j]));
                            } else {
                                if (patient != null && parts[j].equals(patient.getName())) {
                                    paragraph.add(new Text(parts[j])
                                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                            .setFontColor(new DeviceRgb(0, 102, 153))); // Medium blue for patient name
                                } else {
                                    paragraph.add(new Text(parts[j])
                                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                                            .setFontColor(new DeviceRgb(51, 51, 51))); // Dark gray for emphasis
                                }
                            }
                        }
                        document.add(paragraph);
                    }
                }
            }


            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfFile;
    }

}
