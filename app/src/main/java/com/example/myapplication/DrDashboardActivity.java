package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity that displays the doctor's dashboard with their information and list of patients.
 * This activity shows the doctor's name and specialty, provides a list of patients assigned
 * to the doctor, and includes a floating action button to add new patients.
 * The bottom navigation is configured specifically for doctor users.
 */
public class DrDashboardActivity extends BaseActivity {

    /**
     * Checks for notifications about patients who have completed their weekly feedback.
     * This method retrieves the doctor ID from shared preferences, checks if there are any
     * notifications of type "feedback_submitted" for the doctor, and shows a popup notification
     * if there are any new notifications from currently assigned patients.
     * The notification is shown only once when the doctor logs in and is reset when a new
     * patient logs their weekly feedback.
     */
    private void checkForNotifications() {
        Log.d("DrDashboardActivity", "Checking for notifications");
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = preferences.getString("userId", "");

        if (doctorId.isEmpty()) {
            Log.d("DrDashboardActivity", "Doctor ID is empty, returning");
            return;
        }

        // Check if notification has already been shown
        SharedPreferences notificationPrefs = getSharedPreferences("DOCTOR_NOTIFICATION_PREFS", MODE_PRIVATE);
        boolean notificationShown = notificationPrefs.getBoolean("notification_shown_" + doctorId, false);
        Log.d("DrDashboardActivity", "Notification already shown: " + notificationShown);

        // If notification has already been shown, don't show it again
        if (notificationShown) {
            Log.d("DrDashboardActivity", "Notification already shown, returning");
            return;
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        List<Notification> notifications = dbHelper.getNotificationsForUser(doctorId);
        Log.d("DrDashboardActivity", "Found " + notifications.size() + " notifications for doctor " + doctorId);

        List<User> assignedPatients = dbHelper.getPatientsForDoctor(doctorId);
        Log.d("DrDashboardActivity", "Found " + assignedPatients.size() + " assigned patients for doctor " + doctorId);

        // Create a list of patient IDs for quick lookup
        List<String> assignedPatientIds = new ArrayList<>();
        for (User patient : assignedPatients) {
            assignedPatientIds.add(patient.getUserId());
        }

        // Filter notifications to only include "feedback_submitted" type from currently assigned patients
        // and only include notifications that haven't been shown before
        List<Notification> feedbackNotifications = new ArrayList<>();
        int feedbackTypeCount = 0;

        // Get the list of notification IDs that have already been shown
        SharedPreferences shownNotificationsPrefs = getSharedPreferences("SHOWN_NOTIFICATIONS_PREFS", android.content.Context.MODE_PRIVATE);
        Set<String> shownNotificationIds = shownNotificationsPrefs.getStringSet("shown_notification_ids_" + doctorId, new HashSet<>());
        Log.d("DrDashboardActivity", "Found " + shownNotificationIds.size() + " previously shown notification IDs");

        for (Notification notification : notifications) {
            if ("feedback_submitted".equals(notification.getType())) {
                feedbackTypeCount++;
                Log.d("DrDashboardActivity", "Found feedback_submitted notification: " + notification.getMessage());

                // Skip notifications that have already been shown
                if (shownNotificationIds.contains(notification.getId())) {
                    Log.d("DrDashboardActivity", "Notification " + notification.getId() + " has already been shown, skipping");
                    continue;
                }

                String message = notification.getMessage();
                int endIndex = message.indexOf(" has submitted");
                if (endIndex > 0) {
                    String patientName = message.substring(0, endIndex);
                    Log.d("DrDashboardActivity", "Extracted patient name: " + patientName);

                    // Check if this patient is currently assigned to the doctor
                    boolean isCurrentlyAssigned = false;
                    for (User patient : assignedPatients) {
                        if (patient.getPatient() != null && 
                            patient.getPatient().getName() != null && 
                            patient.getPatient().getName().equals(patientName)) {
                            isCurrentlyAssigned = true;
                            Log.d("DrDashboardActivity", "Patient " + patientName + " is currently assigned to doctor");
                            break;
                        }
                    }

                    if (isCurrentlyAssigned) {
                        feedbackNotifications.add(notification);
                        Log.d("DrDashboardActivity", "Added notification for " + patientName + " to feedbackNotifications");
                    } else {
                        Log.d("DrDashboardActivity", "Patient " + patientName + " is not currently assigned to doctor, skipping notification");
                    }
                } else {
                    Log.d("DrDashboardActivity", "Could not extract patient name from message: " + message);
                }
            }
        }

        Log.d("DrDashboardActivity", "Found " + feedbackTypeCount + " feedback_submitted notifications, " + 
              feedbackNotifications.size() + " are from currently assigned patients");

        if (!feedbackNotifications.isEmpty()) {
            Log.d("DrDashboardActivity", "Processing " + feedbackNotifications.size() + " feedback notifications");

            // Extract patient names from notifications
            StringBuilder combinedMessage = new StringBuilder();
            for (Notification notification : feedbackNotifications) {
                String message = notification.getMessage();
                // Extract patient name (format: "[patientName] has submitted their weekly feedback...")
                int endIndex = message.indexOf(" has submitted");
                if (endIndex > 0) {
                    String patientName = message.substring(0, endIndex);
                    if (combinedMessage.length() > 0) {
                        combinedMessage.append("; ");
                    }
                    combinedMessage.append(patientName);
                    Log.d("DrDashboardActivity", "Added patient name to combined message: " + patientName);
                }
            }

            // Create a combined message
            if (combinedMessage.length() > 0) {
                combinedMessage.append(" has submitted their weekly feedback.");
                Log.d("DrDashboardActivity", "Final combined message: " + combinedMessage.toString());

                // Show popup notification
                Log.d("DrDashboardActivity", "Creating PopupNotificationHelper to show notification");
                PopupNotificationHelper popupNotificationHelper = new PopupNotificationHelper(this);
                Log.d("DrDashboardActivity", "Calling showDoctorNotification");
                popupNotificationHelper.showDoctorNotification(combinedMessage.toString(), this);
                Log.d("DrDashboardActivity", "showDoctorNotification called");

                // Mark notification as shown
                Log.d("DrDashboardActivity", "Marking notification as shown for doctor " + doctorId);
                SharedPreferences.Editor editor = notificationPrefs.edit();
                editor.putBoolean("notification_shown_" + doctorId, true);
                editor.apply();
                Log.d("DrDashboardActivity", "Notification marked as shown");

                // Store the notification IDs that have been shown
                Set<String> shownIds = new HashSet<>();
                for (Notification notification : feedbackNotifications) {
                    shownIds.add(notification.getId());
                }

                SharedPreferences.Editor shownIdsEditor = shownNotificationsPrefs.edit();
                shownIdsEditor.putStringSet("shown_notification_ids_" + doctorId, shownIds);
                shownIdsEditor.apply();
                Log.d("DrDashboardActivity", "Stored " + shownIds.size() + " notification IDs as shown");
            } else {
                Log.d("DrDashboardActivity", "Combined message is empty, not showing notification");
            }
        } else {
            Log.d("DrDashboardActivity", "No feedback notifications to show");
        }
    }

    /**
     * Initializes the doctor dashboard activity, sets up UI components, and loads doctor data.
     * This method performs several key operations:
     * - Sets up the activity layout and window transitions
     * - Configures the bottom navigation bar for doctor users
     * - Sets up the floating action button to add new patients
     * - Retrieves and displays the doctor's name and specialty
     * - Loads and displays the list of patients assigned to the doctor
     * - Shows appropriate UI based on whether the doctor has patients or not
     * - Checks for notifications about patients who have completed their weekly feedback
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_doctor_dashboard);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        TextView welcomeText = findViewById(R.id.doctor_welcome_text);
        RecyclerView patientsRecyclerView = findViewById(R.id.patients_recyclerview);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_doctor_dashboard);

        FloatingActionButton fabAddPatient = findViewById(R.id.fab_add_patient);
        fabAddPatient.setOnClickListener(v -> {
            Intent intent = new Intent(DrDashboardActivity.this, DrProfileActivity.class);
            intent.putExtra("create_new_patient", true);
            startActivity(intent);
        });

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = preferences.getString("userId", "");

        if (!doctorId.isEmpty()) {
            User doctor = dbHelper.getUser(doctorId);
            if (doctor != null && doctor.getDoctor() != null) {
                String doctorName = doctor.getDoctor().getName();
                String specialty = doctor.getDoctor().getSpecialty();

                String welcomeMsg = doctorName;
                if (specialty != null && !specialty.isEmpty()) {
                    welcomeMsg += " | " + specialty;
                }
                welcomeText.setText(welcomeMsg);

                List<User> patients = dbHelper.getPatientsForDoctor(doctorId);

                patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                PatientAdapter patientAdapter = new PatientAdapter(this, patients);
                patientsRecyclerView.setAdapter(patientAdapter);

                TextView noPatientsMsgView = findViewById(R.id.no_patients_message);
                if (patients.isEmpty()) {
                    noPatientsMsgView.setVisibility(View.VISIBLE);
                    patientsRecyclerView.setVisibility(View.GONE);
                } else {
                    noPatientsMsgView.setVisibility(View.GONE);
                    patientsRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }

        // Check for notifications
        checkForNotifications();
    }
}
