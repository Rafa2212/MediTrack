package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalReportsActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView reportsRecyclerView;
    private Button addReportButton;
    private TextView patientNameText;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reports);

        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        reportsRecyclerView = findViewById(R.id.reports_recyclerview);
        addReportButton = findViewById(R.id.add_report_button);
        patientNameText = findViewById(R.id.patient_name_text);

        // Set up RecyclerView
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get patient ID from intent
        patientId = getIntent().getStringExtra("patient_id");

        if (patientId != null && !patientId.isEmpty()) {
            // Get patient information
            User patient = dbHelper.getUser(patientId);

            if (patient != null && patient.getUserProfile() != null) {
                // Display patient name
                patientNameText.setText(patient.getUserProfile().getName() + "'s Medical Reports");

                // Load medical reports for this patient
                try {
                    // Get medical reports from the database
                    List<MedicalReport> reports = dbHelper.getMedicalReportsForPatient(patientId);

                    // If no reports found, hide the recycler view
                    if (reports.isEmpty()) {
                        reportsRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Set up the adapter
                        ReportAdapter adapter = new ReportAdapter(reports);
                        reportsRecyclerView.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    Log.e("MedicalReportsActivity", "Error loading medical reports", e);
                    Toast.makeText(this, "Error loading medical reports", Toast.LENGTH_SHORT).show();
                }

                // Check if patient has submitted weekly feedback
                boolean hasSubmittedFeedback = checkIfPatientHasSubmittedFeedback(patientId);

                // Enable/disable add report button based on feedback submission
                addReportButton.setEnabled(hasSubmittedFeedback);

                if (!hasSubmittedFeedback) {
                    Toast.makeText(this, "Add report button is disabled until patient submits weekly feedback", Toast.LENGTH_LONG).show();
                }

                // Set up add report button click listener
                addReportButton.setOnClickListener(v -> {
                    try {
                        // Check if patient has submitted weekly feedback
                        if (hasSubmittedFeedback) {
                            // Show a loading dialog
                            final Dialog dialog = new Dialog(MedicalReportsActivity.this);
                            dialog.setContentView(R.layout.custom_dialog);
                            dialog.findViewById(R.id.progress);
                            TextView textView = dialog.findViewById(R.id.text);
                            textView.setText("Generating medical report...");
                            dialog.setCancelable(false);
                            dialog.show();

                            // Get the patient's profile
                            User patientUser = dbHelper.getUser(patientId);
                            UserProfile patientProfile = patientUser.getUserProfile();

                            // Generate a unique filename for the PDF
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            String pdfFilename = "report_" + patientId + "_" + timestamp + ".pdf";

                            // Create the reports directory if it doesn't exist
                            File reportsDir = new File(getFilesDir(), "reports");
                            if (!reportsDir.exists()) {
                                reportsDir.mkdirs();
                            }

                            // Create a StringBuilder to build the report content
                            StringBuilder reportContent = new StringBuilder();

                            // Add a title
                            reportContent.append("# Weekly Medical Report\n\n");

                            // Add patient information
                            reportContent.append("## Patient Information\n");
                            reportContent.append("Name: ").append(patientProfile.getName()).append("\n");
                            reportContent.append("Age: ").append(patientProfile.getAge()).append("\n");
                            reportContent.append("Height: ").append(patientProfile.getHeight()).append(" cm\n");
                            reportContent.append("Weight: ").append(patientProfile.getWeight()).append(" kg\n\n");

                            // Calculate and add BMI
                            float heightInMeters = patientProfile.getHeight() / 100;
                            float bmi = patientProfile.getWeight() / (heightInMeters * heightInMeters);
                            reportContent.append("## BMI and Metabolic Balance\n");
                            reportContent.append("BMI: ").append(String.format("%.2f", bmi)).append("\n");

                            // Add BMI category
                            String bmiCategory;
                            if (bmi < 18.5) {
                                bmiCategory = "Underweight";
                            } else if (bmi < 25) {
                                bmiCategory = "Normal weight";
                            } else if (bmi < 30) {
                                bmiCategory = "Overweight";
                            } else {
                                bmiCategory = "Obese";
                            }
                            reportContent.append("BMI Category: ").append(bmiCategory).append("\n");

                            // Add metabolic balance indicators
                            reportContent.append("Metabolic Balance Indicators:\n");
                            reportContent.append("- Body Fat Percentage: ").append(patientProfile.getBodyFatPercentage()).append("%\n");
                            reportContent.append("- Resting Heart Rate: ").append(patientProfile.getRestingHeartRate()).append(" bpm\n");
                            reportContent.append("- Blood Pressure: ").append(patientProfile.getBloodPressureSystolic()).append("/").append(patientProfile.getBloodPressureDiastolic()).append(" mmHg\n");
                            reportContent.append("- Blood Glucose: ").append(patientProfile.getBloodGlucose()).append(" mg/dL\n");
                            reportContent.append("- Total Cholesterol: ").append(patientProfile.getCholesterolTotal()).append(" mg/dL\n");
                            reportContent.append("- HDL Cholesterol: ").append(patientProfile.getCholesterolHDL()).append(" mg/dL\n");
                            reportContent.append("- LDL Cholesterol: ").append(patientProfile.getCholesterolLDL()).append(" mg/dL\n\n");

                            // Add diseases
                            reportContent.append("## Patient Diseases\n");
                            List<Disease> diseases = loadDiseasesForPatient(patientId);
                            if (diseases.isEmpty()) {
                                reportContent.append("No diseases recorded for this patient.\n\n");
                            } else {
                                for (Disease disease : diseases) {
                                    reportContent.append("- ").append(disease.getName()).append(" (").append(disease.getICD10()).append(")\n");
                                }
                                reportContent.append("\n");
                            }

                            // Add health score
                            reportContent.append("## Health Score\n");
                            reportContent.append("Current Health Score: ").append(patientProfile.getHealthScore()).append("/100\n\n");

                            // Get the latest weekly feedback from the patient
                            List<MedicalReport> patientReports = dbHelper.getMedicalReportsForPatient(patientId);
                            if (patientReports != null && !patientReports.isEmpty()) {
                                // Get the most recent report (which should be the first one since they're ordered by date DESC)
                                MedicalReport latestReport = patientReports.get(0);
                                if (latestReport != null && latestReport.getReportContent() != null && !latestReport.getReportContent().isEmpty()) {
                                    reportContent.append("## Weekly Feedback from Patient\n");
                                    reportContent.append(latestReport.getReportContent()).append("\n\n");
                                }
                            }

                            // Add recommendations
                            reportContent.append("## Doctor's Recommendations\n");
                            reportContent.append("Based on the patient's health metrics and weekly feedback, the following recommendations are provided:\n\n");

                            // Add some recommendations based on health metrics
                            if (bmi < 18.5) {
                                reportContent.append("- Consider a nutrition plan to increase weight in a healthy manner\n");
                            } else if (bmi >= 25) {
                                reportContent.append("- Consider a weight management program to achieve a healthier BMI\n");
                            }

                            if (patientProfile.getBodyFatPercentage() > 25) {
                                reportContent.append("- Consider a weight management program to reduce body fat percentage\n");
                            }

                            if (patientProfile.getRestingHeartRate() > 80) {
                                reportContent.append("- Increase cardiovascular exercise to lower resting heart rate\n");
                            }

                            if (patientProfile.getBloodPressureSystolic() > 130 || patientProfile.getBloodPressureDiastolic() > 80) {
                                reportContent.append("- Monitor blood pressure regularly and consider lifestyle changes to reduce it\n");
                            }

                            if (patientProfile.getBloodGlucose() > 100) {
                                reportContent.append("- Monitor blood glucose levels and consider dietary changes\n");
                            }

                            if (patientProfile.getCholesterolTotal() > 200) {
                                reportContent.append("- Consider dietary changes to reduce total cholesterol\n");
                            }

                            if (patientProfile.getCholesterolLDL() > 130) {
                                reportContent.append("- Consider dietary changes to reduce LDL cholesterol\n");
                            }

                            // Add a conclusion
                            reportContent.append("\n## Conclusion\n");
                            reportContent.append("This report was generated on ").append(new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(new Date())).append(".\n");
                            reportContent.append("Please consult with your healthcare provider for personalized advice.\n");

                            // Generate the PDF
                            PDFGeneration pdfGeneration = new PDFGeneration(getApplicationContext());
                            File generatedPdfFile = pdfGeneration.createPDF(reportContent.toString());

                            // Rename the generated PDF to our desired filename
                            if (generatedPdfFile.exists()) {
                                File renamedFile = new File(reportsDir, pdfFilename);
                                generatedPdfFile.renameTo(renamedFile);

                                // Save the report to the database
                                String reportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                                long reportId = dbHelper.saveMedicalReport(
                                    patientId,
                                    reportDate,
                                    reportContent.toString(),
                                    renamedFile.getAbsolutePath()
                                );

                                if (reportId != -1) {
                                    // Update the UI to show the new report
                                    List<MedicalReport> reports = dbHelper.getMedicalReportsForPatient(patientId);

                                    if (!reports.isEmpty()) {
                                        reportsRecyclerView.setVisibility(View.VISIBLE);
                                        ReportAdapter adapter = new ReportAdapter(reports);
                                        reportsRecyclerView.setAdapter(adapter);
                                    }
                                } else {
                                    Toast.makeText(MedicalReportsActivity.this, "Error saving report to database", Toast.LENGTH_SHORT).show();
                                }

                                // Dismiss the dialog
                                dialog.dismiss();

                                // Show a success message
                                Toast.makeText(MedicalReportsActivity.this, "Medical report generated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                // Dismiss the dialog
                                dialog.dismiss();

                                // Show an error message
                                Toast.makeText(MedicalReportsActivity.this, "Error generating medical report", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MedicalReportsActivity.this, "Patient has not submitted weekly feedback yet", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("MedicalReportsActivity", "Error generating medical report", e);
                        Toast.makeText(MedicalReportsActivity.this, "Error generating medical report", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Patient information not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No patient selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_patients);
    }

    private boolean checkIfPatientHasSubmittedFeedback(String patientId) {
        try {
            if (patientId == null || patientId.isEmpty()) {
                return false;
            }

            User patient = dbHelper.getUser(patientId);
            if (patient == null) {
                return false;
            }

            UserProfile profile = patient.getUserProfile();
            if (profile == null) {
                return false;
            }

            String lastMedicalReport = profile.getLastMedicalReport();
            return lastMedicalReport != null && !lastMedicalReport.isEmpty();
        } catch (Exception e) {
            Log.e("MedicalReportsActivity", "Error checking if patient has submitted feedback", e);
            return false;
        }
    }

    // Inner class for the RecyclerView adapter
    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
        private final List<MedicalReport> reports;

        public ReportAdapter(List<MedicalReport> reports) {
            this.reports = reports;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MedicalReport report = reports.get(position);

            // Format the date for display
            String formattedDate = "";
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(report.getReportDate(), inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
                formattedDate = dateTime.format(outputFormatter);
            } catch (Exception e) {
                formattedDate = report.getReportDate(); // Fallback to original string if parsing fails
            }

            holder.titleTextView.setText("Medical Report");
            holder.dateTextView.setText(formattedDate);

            holder.itemView.setOnClickListener(v -> {
                try {
                    // Open the PDF file using the path from the database
                    File file = new File(report.getReportPath());
                    if (file.exists()) {
                        Uri uri = FileProvider.getUriForFile(MedicalReportsActivity.this,
                                getPackageName() + ".provider", file);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(intent);
                    } else {
                        Toast.makeText(MedicalReportsActivity.this, "PDF file not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("MedicalReportsActivity", "Error opening PDF file", e);
                    Toast.makeText(MedicalReportsActivity.this, "Error opening PDF file", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return reports != null ? reports.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView titleTextView;
            public TextView dateTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(android.R.id.text1);
                dateTextView = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    /**
     * Loads diseases for a patient
     * @param patientId The ID of the patient
     * @return A list of diseases for the patient
     */
    private List<Disease> loadDiseasesForPatient(String patientId) {
        List<Disease> diseases = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=?";
        String[] selectionArgs = {patientId};
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_DISEASES, 
                null, 
                selection, 
                selectionArgs, 
                null, 
                null, 
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int diseaseId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_ID_FK));

                String diseaseSelection = DatabaseHelper.COLUMN_DISEASE_ID + "=?";
                String[] diseaseSelectionArgs = {String.valueOf(diseaseId)};
                Cursor diseaseCursor = db.query(
                        DatabaseHelper.TABLE_DISEASES, 
                        null, 
                        diseaseSelection,
                        diseaseSelectionArgs, 
                        null, 
                        null, 
                        null);

                if (diseaseCursor != null && diseaseCursor.moveToFirst()) {
                    @SuppressLint("Range")
                    String description = diseaseCursor.getString(
                            diseaseCursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION));
                    @SuppressLint("Range")
                    String icd10 =
                            diseaseCursor.getString(diseaseCursor.getColumnIndex(DatabaseHelper.COLUMN_ICD10));

                    Disease disease = new Disease(diseaseId, description, icd10);
                    diseases.add(disease);
                }
                if (diseaseCursor != null) {
                    diseaseCursor.close();
                }
            }
            cursor.close();
        }

        return diseases;
    }
}
