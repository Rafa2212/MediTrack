package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
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
            //TODO: format the headers, #, * and everything with AI and debug the API problems
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);


            Document document = new Document(pdf);
            document.add(new Paragraph(weeklyReportResponse));
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