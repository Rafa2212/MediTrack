package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends BaseActivity {
    private String bInterpretation;
    private String metabolicInterpretation;

    /**
     * Checks if a patient can submit a weekly report
     * @param userId The user ID
     * @param userProfile The user's profile
     * @return true if the patient can submit a weekly report, false otherwise
     */
    private boolean isWeeklyFeedbackAvailable(String userId, UserProfile userProfile) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        // First, check if there's a report in the database from the last week
        MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(userId);

        if (latestReport != null && latestReport.getReportDate() != null && !latestReport.getReportDate().isEmpty()) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                java.time.LocalDateTime reportDate = java.time.LocalDateTime.parse(latestReport.getReportDate(), formatter);
                java.time.LocalDateTime oneWeekAgo = java.time.LocalDateTime.now().minusWeeks(1);

                // If the latest report is less than a week old, the user can't submit a new report
                if (reportDate.isAfter(oneWeekAgo)) {
                    return false;
                }
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error parsing report date", e);
            }
        }

        // If no recent report in the database, check the lastMedicalReport timestamp in the UserProfile
        String dateStr = userProfile.getLastMedicalReport();
        if (dateStr == null || dateStr.isEmpty()) {
            return true;
        }

        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            java.time.LocalDateTime date = java.time.LocalDateTime.parse(dateStr, formatter);
            java.time.LocalDateTime oneWeekAgo = java.time.LocalDateTime.now().minusWeeks(1);
            return date.isBefore(oneWeekAgo);
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error parsing lastMedicalReport date", e);
            return true;
        }
    }

    /**
     * Checks for notifications and shows them if needed
     */
    public void checkForNotifications() {
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        if (userId.isEmpty()) {
            return;
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        User user = dbHelper.getUser(userId);

        if (user == null || user.getUserProfile() == null) {
            return;
        }

        // Check if the patient has weekly feedback available
        boolean canSubmitReport = isWeeklyFeedbackAvailable(userId, user.getUserProfile());

        // Check if notification has been shown already
        SharedPreferences notificationPrefs = getSharedPreferences("NOTIFICATION_PREFS", MODE_PRIVATE);
        boolean notificationShown = notificationPrefs.getBoolean("notification_shown_" + userId, false);

        // Show notification if weekly feedback is available and notification hasn't been shown
        if (canSubmitReport && !notificationShown) {
            // Create a notification for the patient
            String message = "Your weekly report is now available to submit. Please log your feedback.";
            dbHelper.saveNotification(userId, message, "timer_expired");

            // Show notification
            NotificationDialog dialog = new NotificationDialog(this, userId);
            dialog.show();
        }
    }

    @SuppressLint({"SetTextI18p", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        RecyclerView diseaseRecyclerView = findViewById(R.id.disease_recyclerview);
        RecyclerView healthWidgetsRecyclerView = findViewById(R.id.health_widgets_recyclerview);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupNavigation(bottomNav, R.id.menu_dashboard);

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        // Display assigned doctors
        if (!userId.isEmpty()) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            List<User> assignedDoctors = dbHelper.getDoctorsForPatient(userId);

            // Find the TextView to display assigned doctors
            TextView assignedDoctorsTextView = findViewById(R.id.assigned_doctors_text);

            if (assignedDoctors != null && !assignedDoctors.isEmpty()) {
                StringBuilder doctorsText = new StringBuilder("Your assigned doctors: ");
                for (int i = 0; i < assignedDoctors.size(); i++) {
                    User doctor = assignedDoctors.get(i);
                    if (doctor.getUserProfile() != null) {
                        doctorsText.append(doctor.getUserProfile().getName());
                        if (i < assignedDoctors.size() - 1) {
                            doctorsText.append(", ");
                        }
                    }
                }
                assignedDoctorsTextView.setText(doctorsText.toString());
                assignedDoctorsTextView.setVisibility(View.VISIBLE);
            } else {
                assignedDoctorsTextView.setText("You don't have any assigned doctors yet.");
                assignedDoctorsTextView.setVisibility(View.VISIBLE);
            }
        }

        Map<String, ?> allEntries = preferences.getAll();

        // Find most recent BMI interpretation
        LocalDateTime mostRecentBMI = LocalDateTime.MIN;
        String mostRecentBMIKey = "";
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                String key = entry.getKey();
                if (key == null) continue;

                String[] parts = key.split("#");
                if (parts.length >= 3 && parts[0].contains("BMI")) {
                    LocalDateTime currentDateTime = LocalDateTime.parse(parts[2]);
                    if (currentDateTime.isAfter(mostRecentBMI)) {
                        mostRecentBMI = currentDateTime;
                        mostRecentBMIKey = key;
                    }
                }
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error processing BMI entry", e);
            }
        }

        // Find most recent Metabolic interpretation
        LocalDateTime mostRecentMetabolic = LocalDateTime.MIN;
        String mostRecentMetabolicKey = "";
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                String key = entry.getKey();
                if (key == null) continue;

                String[] parts = key.split("#");
                if (parts.length >= 3 && parts[0].contains("Metabolic")) {
                    LocalDateTime currentDateTime = LocalDateTime.parse(parts[2]);
                    if (currentDateTime.isAfter(mostRecentMetabolic)) {
                        mostRecentMetabolic = currentDateTime;
                        mostRecentMetabolicKey = key;
                    }
                }
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error processing Metabolic entry", e);
            }
        }

        ArrayList<Disease> diseasesList = new ArrayList<>();
        ArrayList<HealthWidget> healthWidgetsList = new ArrayList<>();

        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            // Get BMI interpretation
            if (!mostRecentBMIKey.isEmpty()) {
                try {
                    long bmiId = Long.parseLong(preferences.getString(mostRecentBMIKey, ""));
                    Session curr_bmi = dbHelper.getSession(bmiId);
                    if (curr_bmi != null) {
                        bInterpretation = curr_bmi.getValue();
                    } else {
                        bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error getting BMI interpretation", e);
                    bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
                }
            } else {
                bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
            }

            // Get Metabolic interpretation
            if (!mostRecentMetabolicKey.isEmpty()) {
                try {
                    long metabolicId = Long.parseLong(preferences.getString(mostRecentMetabolicKey, ""));
                    Session curr_metabolic = dbHelper.getSession(metabolicId);
                    if (curr_metabolic != null) {
                        metabolicInterpretation = curr_metabolic.getValue();
                    } else {
                        metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error getting Metabolic interpretation", e);
                    metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
                }
            } else {
                metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
            }

            // Add health widgets to the list
            healthWidgetsList.add(new HealthWidget(
                    "BMI",
                    getString(R.string.bmi_title),
                    getString(R.string.bmi_description),
                    R.drawable.background_bmi,
                    bInterpretation
            ));

            healthWidgetsList.add(new HealthWidget(
                    "Metabolic",
                    getString(R.string.metabolic_title),
                    getString(R.string.metabolic_description),
                    R.drawable.background_bmi,
                    metabolicInterpretation
            ));

            // Get all diseases from the database for this patient
            // This will include diseases added by all doctors assigned to this patient
            List<Disease> diseases = dbHelper.getDiseasesForPatient(userId);

            for (Disease disease : diseases) {
                // Try to get interpretation from SharedPreferences
                String diseaseKey = "Disease#" + disease.getICD10() + "#" + disease.getName();
                String interpretationId = preferences.getString(diseaseKey, "");
                String interpretation = "";

                if (!interpretationId.isEmpty()) {
                    try {
                        Session diseaseSession = dbHelper.getSession(Long.parseLong(interpretationId));
                        if (diseaseSession != null) {
                            interpretation = diseaseSession.getValue();
                        }
                    } catch (Exception e) {
                        Log.e("DashboardActivity", "Error getting disease interpretation", e);
                    }
                }

                // If no interpretation found, use a default message
                if (interpretation.isEmpty()) {
                    interpretation = "No detailed information available for this disease.";
                }

                Disease diseaseWithInterpretation = new Disease(disease.getICD10(), disease.getName(), interpretation);
                diseasesList.add(diseaseWithInterpretation);
            }

            // Also include diseases from SharedPreferences for backward compatibility
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                try {
                    String key = entry.getKey();
                    if (key == null) continue;

                    String[] parts = key.split("#");
                    if (parts.length >= 3 && parts[0].equals("Disease")) {
                        // Check if this disease is already in the list
                        boolean alreadyExists = false;
                        for (Disease disease : diseasesList) {
                            if (disease.getICD10().equals(parts[1])) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (!alreadyExists) {
                            // Verify that this disease still exists in the database (not deleted by doctor)
                            SQLiteDatabase verifyDb = dbHelper.getReadableDatabase();
                            String verifyQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USER_DISEASES + " ud " +
                                    "INNER JOIN " + DatabaseHelper.TABLE_DISEASES + " d ON ud." + 
                                    DatabaseHelper.COLUMN_DISEASE_ID_FK + " = d." + DatabaseHelper.COLUMN_DISEASE_ID + 
                                    " WHERE d." + DatabaseHelper.COLUMN_ICD10 + " = ? AND ud." + 
                                    DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + " = ?";

                            Cursor verifyCursor = verifyDb.rawQuery(verifyQuery, new String[]{parts[1], userId});
                            boolean diseaseExists = verifyCursor != null && verifyCursor.getCount() > 0;

                            if (verifyCursor != null) {
                                verifyCursor.close();
                            }

                            if (diseaseExists) {
                                long diseaseId = Long.parseLong(preferences.getString(key, ""));
                                Session curr_disease = dbHelper.getSession(diseaseId);
                                if (curr_disease != null) {
                                    String diseaseInterpretation = curr_disease.getValue();
                                    if (diseaseInterpretation != null) {
                                        diseasesList.add(new Disease(parts[1], parts[2], diseaseInterpretation));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error processing disease entry", e);
                }
            }
        } catch (Exception e) {
            Log.e("ErrorTag", "DatabaseHelper instantiation failed", e);
        }

        // Set up health widgets RecyclerView
        HealthWidgetAdapter healthWidgetAdapter = new HealthWidgetAdapter(healthWidgetsList);
        healthWidgetsRecyclerView.setAdapter(healthWidgetAdapter);
        LinearLayoutManager healthLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        healthWidgetsRecyclerView.setLayoutManager(healthLayoutManager);
        int healthSpacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        healthWidgetsRecyclerView.addItemDecoration(
                new HealthWidgetAdapter.HealthWidgetItemDecoration(healthSpacingInPixels));

        // Set up disease RecyclerView
        CarouselAdapter carouselAdapter = new CarouselAdapter(diseasesList);

        if (diseasesList.isEmpty()) {
            diseaseRecyclerView.setVisibility(View.GONE);
            TextView interpretationText = findViewById(R.id.disease_subtext);
            interpretationText.setVisibility(View.GONE);
            ImageView interpretationsTitle = findViewById(R.id.image_interpretation);
            interpretationsTitle.setVisibility(View.GONE);
        } else {
            diseaseRecyclerView.setAdapter(carouselAdapter);
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            diseaseRecyclerView.setLayoutManager(layoutManager);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
            diseaseRecyclerView.addItemDecoration(
                    new CarouselAdapter.CarouselItemDecoration(spacingInPixels));
            diseaseRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        // Check for notifications
        checkForNotifications();
    }
}
