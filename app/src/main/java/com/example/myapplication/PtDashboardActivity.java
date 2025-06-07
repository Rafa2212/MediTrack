package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.example.myapplication.NotificationUtils;

/**
 * Activity that displays the user's dashboard with health information and disease interpretations.
 * This activity shows the user's assigned doctors, BMI and metabolic interpretations,
 * and disease-specific information in a carousel view.
 */
public class PtDashboardActivity extends BaseActivity {

    /**
     * Checks if a patient can submit a weekly report
     * @param userId The user ID
     * @param userProfile The user's profile
     * @return true if the patient can submit a weekly report, false otherwise
     * @deprecated Use {@link NotificationUtils} instead.
     */
    @Deprecated
    private boolean isWeeklyFeedbackAvailable(String userId, Patient userProfile) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(userId);

        if (latestReport != null && latestReport.getReportDate() != null && !latestReport.getReportDate().isEmpty()) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                java.time.LocalDateTime reportDate = java.time.LocalDateTime.parse(latestReport.getReportDate(), formatter);
                java.time.LocalDateTime oneWeekAgo = java.time.LocalDateTime.now().minusWeeks(1);

                if (reportDate.isAfter(oneWeekAgo)) {
                    return false;
                }
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error parsing report date", e);
            }
        }

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
     * Checks if the user is eligible for a weekly report notification and displays it if needed.
     * This method retrieves the user ID from shared preferences and uses NotificationUtils
     * to check if the user can submit a weekly report and show a notification if eligible.
     * The notification is shown as a popup every time the patient logs in if feedback is not registered for the week.
     */
    public void checkForNotifications() {
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        if (userId.isEmpty()) {
            return;
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        User user = dbHelper.getUser(userId);

        if (user == null || user.getPatient() == null) {
            return;
        }

        // Check if the patient can submit a weekly report
        boolean canSubmitReport = NotificationUtils.isWeeklyFeedbackAvailable(this, userId, user.getPatient());

        if (canSubmitReport) {
            // Save notification to database if needed
            NotificationUtils.checkAndSendNotificationForPatient(this, userId);

            // Show popup notification
            String message = "Your weekly report is now available to submit. Please log your feedback.";
            PopupNotificationHelper popupNotificationHelper = new PopupNotificationHelper(this);
            popupNotificationHelper.showPatientNotification(message, this);
        }

        // Also schedule the next notification based on the last report date
        String lastReportDate = user.getPatient().getLastMedicalReport();
        if (lastReportDate != null && !lastReportDate.isEmpty()) {
            NotificationUtils.scheduleNextFeedbackNotification(this, userId, lastReportDate);
        }
    }

    /**
     * Initializes the dashboard activity, sets up UI components, and loads user data.
     * This method performs several key operations:
     * - Sets up the bottom navigation bar
     * - Retrieves and displays assigned doctors for the current user
     * - Finds the most recent BMI and Metabolic entries
     * - Loads disease data and health widget information
     * - Configures RecyclerViews with appropriate adapters and layouts
     * - Checks for notifications that need to be displayed
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @SuppressLint({"SetTextI18p", "ClickableViewAccessibility", "SetTextI18n"})
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

        ArrayList<Disease> diseasesList = new ArrayList<>();
        ArrayList<ProfileWidget> profileWidgetsList = new ArrayList<>();

        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            // Get the user's profile from the database
            User user = dbHelper.getUser(userId);
            Patient userProfile = (user != null) ? user.getPatient() : null;

            // Get BMI interpretation from UserProfile
            String bInterpretation;
            if (userProfile != null && userProfile.getBmiInterpretation() != null && !userProfile.getBmiInterpretation().isEmpty()) {
                bInterpretation = userProfile.getBmiInterpretation();
            } else {
                bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
            }

            // Get Metabolic Balance interpretation from UserProfile
            String metabolicInterpretation;
            if (userProfile != null && userProfile.getMetabolicInterpretation() != null && !userProfile.getMetabolicInterpretation().isEmpty()) {
                metabolicInterpretation = userProfile.getMetabolicInterpretation();
            } else {
                metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
            }

            profileWidgetsList.add(new ProfileWidget(
                    "BMI",
                    getString(R.string.bmi_title),
                    getString(R.string.bmi_description),
                    R.drawable.background_bmi,
                    bInterpretation
            ));

            profileWidgetsList.add(new ProfileWidget(
                    "Metabolic",
                    getString(R.string.metabolic_title),
                    getString(R.string.metabolic_description),
                    R.drawable.background_metabolic,
                    metabolicInterpretation
            ));

            // Get diseases with interpretations directly from the database
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT " + 
                    DatabaseHelper.COLUMN_ICD10 + ", " + 
                    DatabaseHelper.COLUMN_DISEASE_DESCRIPTION + ", " + 
                    "interpretation " +
                    "FROM " + DatabaseHelper.TABLE_USER_DISEASES + 
                    " WHERE " + DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{userId});

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range")
                    String icd10 = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ICD10));
                    @SuppressLint("Range")
                    String diseaseName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION));

                    String interpretation = "";
                    try {
                        @SuppressLint("Range")
                        String interpretationFromDb = cursor.getString(cursor.getColumnIndex("interpretation"));
                        if (interpretationFromDb != null && !interpretationFromDb.isEmpty()) {
                            interpretation = interpretationFromDb;
                        }
                    } catch (Exception e) {
                        Log.e("DashboardActivity", "Error getting interpretation from database", e);
                    }

                    if (interpretation.isEmpty()) {
                        interpretation = "No detailed information available for this disease.";
                    }

                    // Check if this disease is already in the list
                    boolean alreadyExists = false;
                    for (Disease existingDisease : diseasesList) {
                        if (existingDisease.getICD10().equals(icd10)) {
                            alreadyExists = true;
                            break;
                        }
                    }

                    if (!alreadyExists) {
                        Disease diseaseWithInterpretation = new Disease(icd10, diseaseName, interpretation);
                        diseasesList.add(diseaseWithInterpretation);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("ErrorTag", "DatabaseHelper instantiation failed", e);
        }

        ProfileWidgetAdapter profileWidgetAdapter = new ProfileWidgetAdapter(profileWidgetsList);
        healthWidgetsRecyclerView.setAdapter(profileWidgetAdapter);
        LinearLayoutManager healthLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        healthWidgetsRecyclerView.setLayoutManager(healthLayoutManager);
        int healthSpacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        healthWidgetsRecyclerView.addItemDecoration(
                new ProfileWidgetAdapter.HealthWidgetItemDecoration(healthSpacingInPixels));

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
                /**
                 * Called when the RecyclerView is scrolled.
                 * This implementation simply calls the superclass method.
                 * 
                 * @param recyclerView The RecyclerView that was scrolled
                 * @param dx The amount of horizontal scroll
                 * @param dy The amount of vertical scroll
                 */
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        checkForNotifications();
    }
}
