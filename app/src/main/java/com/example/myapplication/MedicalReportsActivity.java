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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

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
    private TextView patientNameText;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reports);

        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        reportsRecyclerView = findViewById(R.id.reports_recyclerview);
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
                    Snackbar.make(findViewById(android.R.id.content),
                            "Error loading medical reports!",
                            Snackbar.LENGTH_SHORT).show();
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Patient information not found!",
                        Snackbar.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "No patient selected!",
                    Snackbar.LENGTH_SHORT).show();
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
                    .inflate(R.layout.item_pdf_report, parent, false);
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
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
                formattedDate = dateTime.format(outputFormatter);
            } catch (Exception e) {
                formattedDate = report.getReportDate(); // Fallback to original string if parsing fails
            }

            // Get doctor information
            String doctorName = "Unknown Doctor";
            String doctorSpecialty = "";
            String doctorId = report.getDoctorId();
            if (doctorId != null && !doctorId.isEmpty()) {
                User doctor = dbHelper.getUser(doctorId);
                if (doctor != null && doctor.getUserProfile() != null) {
                    doctorName = doctor.getUserProfile().getName();
                    doctorSpecialty = doctor.getUserProfile().getSpecialty();
                }
            }

            holder.titleTextView.setText("Medical Report");
            holder.dateTextView.setText("Logged on: " + formattedDate);

            holder.itemView.setOnClickListener(v -> {
                try {
                    // Log the report path for debugging
                    Log.d("MedicalReportsActivity", "Report path: " + report.getReportPath());

                    // Open the PDF file using the path from the database
                    File file = new File(report.getReportPath());
                    if (file.exists()) {
                        Log.d("MedicalReportsActivity", "File exists at path: " + file.getAbsolutePath());

                        Uri uri = FileProvider.getUriForFile(MedicalReportsActivity.this,
                                getPackageName() + ".provider", file);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(intent);
                    } else {
                        Log.e("MedicalReportsActivity", "File does not exist at path: " + file.getAbsolutePath());
                        Snackbar.make(findViewById(android.R.id.content),
                                "PDF file not found!",
                                Snackbar.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("MedicalReportsActivity", "Error opening PDF file: " + e.getMessage(), e);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Error opening PDF file!",
                            Snackbar.LENGTH_SHORT).show();
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
            public ImageView pdfIconView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.report_title);
                dateTextView = itemView.findViewById(R.id.report_date);
                pdfIconView = itemView.findViewById(R.id.pdf_icon);
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
