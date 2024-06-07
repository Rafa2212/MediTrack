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
        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "WeeklyReport.pdf");
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            String[] lines = weeklyReportResponse.split("\n");
            for (String line : lines) {
                if (line.startsWith("### ")) {
                    Text text = new Text(line.replace("### ", ""))
                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                            .setFontSize(12)
                            .setFontColor(new DeviceRgb(0, 0, 255));
                    document.add(new Paragraph().add(text).setMarginTop(10.0f).setMarginBottom(10.0f));
                } else if (line.startsWith("## ")) {
                    Text text = new Text(line.replace("## ", ""))
                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                            .setFontSize(14)
                            .setFontColor(new DeviceRgb(0, 0, 0));
                    document.add(new Paragraph().add(text).setMarginTop(15.0f).setMarginBottom(10.0f));
                } else if (line.startsWith("# ")) {
                    Text text = new Text(line.replace("# ", ""))
                            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                            .setFontSize(16)
                            .setFontColor(new DeviceRgb(0, 0, 0));
                    document.add(new Paragraph().add(text).setMarginTop(20.0f).setMarginBottom(10.0f));
                } else if (line.contains("**")) {
                    Paragraph paragraph = new Paragraph();
                    String[] parts = line.split("\\*\\*");
                    for (int i = 0; i < parts.length; i++) {
                        if (i % 2 == 0) {
                            paragraph.add(new Text(parts[i]));
                        } else {
                            paragraph.add(new Text(parts[i]).setFont(PdfFontFactory.createFont("Helvetica-Bold")));
                        }
                    }
                    document.add(paragraph);
                } else {
                    document.add(new Paragraph(line).setFontSize(12));
                }
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
}