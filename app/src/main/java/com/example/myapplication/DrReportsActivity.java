package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Activity that displays medical reports for a patient.
 * This activity retrieves a patient ID from the intent, loads the patient's medical reports,
 * and displays them in a RecyclerView. When a report is clicked, it opens the PDF file
 * using a FileProvider. The activity uses the doctor navigation bar for navigation.
 */
public class DrReportsActivity extends BaseActivity {

    /**
     * Initializes the activity, sets up UI components, and loads medical reports.
     * This method retrieves the patient ID from the intent, loads the patient's information
     * and medical reports, and displays them in a RecyclerView. It also sets up the
     * doctor navigation bar for navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reports);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        RecyclerView reportsRecyclerView = findViewById(R.id.reports_recyclerview);
        TextView patientNameText = findViewById(R.id.patient_name_text);

        reportsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        String patientId = getIntent().getStringExtra("patient_id");

        if (patientId != null && !patientId.isEmpty()) {
            User patient = dbHelper.getUser(patientId);

            if (patient != null && patient.getPatient() != null) {
                patientNameText.setText(patient.getPatient().getName() + "'s Medical Reports");

                try {
                    List<MedicalReport> reports = dbHelper.getMedicalReportsForPatient(patientId);

                    if (reports.isEmpty()) {
                        reportsRecyclerView.setVisibility(View.GONE);
                    } else {
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
            // Show reports for all patients assigned to the doctor
            SharedPreferences preferences = getSharedPreferences("PREFERENCE", android.content.Context.MODE_PRIVATE);
            String doctorId = preferences.getString("userId", "");

            if (!doctorId.isEmpty()) {
                patientNameText.setText("Recent Patient Reports");

                try {
                    SharedPreferences shownNotificationsPrefs = getSharedPreferences("SHOWN_NOTIFICATIONS_PREFS", MODE_PRIVATE);
                    Set<String> shownNotificationIds = shownNotificationsPrefs.getStringSet("shown_notification_ids_" + doctorId, new HashSet<>());

                    List<Notification> notifications = dbHelper.getNotificationsForUser(doctorId);

                    List<String> recentPatientNames = new ArrayList<>();
                    for (Notification notification : notifications) {
                        if ("feedback_submitted".equals(notification.getType()) && 
                            shownNotificationIds.contains(notification.getId())) {

                            String message = notification.getMessage();
                            int endIndex = message.indexOf(" has submitted");
                            if (endIndex > 0) {
                                String patientName = message.substring(0, endIndex);
                                recentPatientNames.add(patientName);
                            }
                        }
                    }

                    List<User> patients = dbHelper.getPatientsForDoctor(doctorId);
                    List<MedicalReport> allReports = new ArrayList<>();

                    for (User patient : patients) {
                        if (patient.getPatient() != null && 
                            patient.getPatient().getName() != null && 
                            recentPatientNames.contains(patient.getPatient().getName())) {

                            List<MedicalReport> patientReports = dbHelper.getMedicalReportsForPatient(patient.getUserId());
                            allReports.addAll(patientReports);
                        }
                    }

                    Collections.sort(allReports, (r1, r2) -> {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                            LocalDateTime date1 = LocalDateTime.parse(r1.getReportDate(), formatter);
                            LocalDateTime date2 = LocalDateTime.parse(r2.getReportDate(), formatter);
                            return date2.compareTo(date1); // Newest first
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    if (allReports.isEmpty()) {
                        reportsRecyclerView.setVisibility(View.GONE);
                        TextView noReportsText = new TextView(this);
                        noReportsText.setText("No reports available");
                        noReportsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        ((ViewGroup) reportsRecyclerView.getParent()).addView(noReportsText);
                    } else {
                        ReportAdapter adapter = new ReportAdapter(allReports);
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
                        "Doctor information not found!",
                        Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_patients);
    }

    /**
     * Adapter for displaying medical reports in a RecyclerView.
     * This adapter binds MedicalReport objects to views that display the report title,
     * date, and provide functionality to open the PDF file when clicked.
     */
    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
        private final List<MedicalReport> reports;

        /**
         * Constructs a new ReportAdapter with the specified list of medical reports.
         *
         * @param reports The list of MedicalReport objects to display in the RecyclerView
         */
        public ReportAdapter(List<MedicalReport> reports) {
            this.reports = reports;
        }

        /**
         * Creates a new ViewHolder by inflating the item layout.
         *
         * @param parent The ViewGroup into which the new View will be added
         * @param viewType The view type of the new View (not used in this implementation)
         * @return A new ViewHolder that holds a View of the given view type
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pdf_report, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        /**
         * Binds data from a MedicalReport to the views in the ViewHolder.
         * This method formats the report date, sets the title and date text,
         * and configures a click listener to open the PDF file when clicked.
         *
         * @param holder The ViewHolder to bind data to
         * @param position The position of the item in the data set
         */
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MedicalReport report = reports.get(position);

            String formattedDate = "";
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(report.getReportDate(), inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
                formattedDate = dateTime.format(outputFormatter);
            } catch (Exception e) {
                formattedDate = report.getReportDate();
            }

            holder.titleTextView.setText("Medical Report");
            holder.dateTextView.setText("Logged on: " + formattedDate);

            holder.itemView.setOnClickListener(v -> {
                try {
                    Log.d("MedicalReportsActivity", "Report path: " + report.getReportPath());

                    File file = new File(report.getReportPath());
                    if (file.exists()) {
                        Log.d("MedicalReportsActivity", "File exists at path: " + file.getAbsolutePath());

                        Uri uri = FileProvider.getUriForFile(DrReportsActivity.this,
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

        /**
         * Returns the total number of items in the data set.
         * Safely handles the case where the reports list might be null.
         *
         * @return The total number of items in this adapter
         */
        @Override
        public int getItemCount() {
            return reports != null ? reports.size() : 0;
        }

        /**
         * ViewHolder class for medical report items.
         * Holds references to the views within each item in the RecyclerView.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView titleTextView;
            public TextView dateTextView;
            public ImageView pdfIconView;

            /**
             * Constructs a new ViewHolder and finds all the required views from the item layout.
             *
             * @param itemView The view containing the medical report item layout
             */
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.report_title);
                dateTextView = itemView.findViewById(R.id.report_date);
                pdfIconView = itemView.findViewById(R.id.pdf_icon);
            }
        }
    }
}
