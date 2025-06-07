package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling notifications in the MediTrack app.
 * This class provides methods to check if feedback is available for patients
 * and send notifications accordingly.
 */
public class NotificationUtils {
    private static final String TAG = "NotificationUtils";

    /**
     * Checks if a patient can submit a weekly report
     * @param context The context
     * @param userId The user ID
     * @param userProfile The user's profile
     * @return true if the patient can submit a weekly report, false otherwise
     */
    public static boolean isWeeklyFeedbackAvailable(Context context, String userId, Patient userProfile) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(userId);

        if (latestReport != null && latestReport.getReportDate() != null && !latestReport.getReportDate().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime reportDate = LocalDateTime.parse(latestReport.getReportDate(), formatter);
                LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

                if (reportDate.isAfter(oneWeekAgo)) {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing report date", e);
            }
        }

        String dateStr = userProfile.getLastMedicalReport();
        if (dateStr == null || dateStr.isEmpty()) {
            return true;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            return date.isBefore(oneWeekAgo);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing lastMedicalReport date", e);
            return true;
        }
    }

    /**
     * Checks if the specified patient is eligible for a weekly report notification and sends it if needed.
     * This method checks if the patient can submit a weekly report and saves a notification if they can.
     * The notification will be displayed as a popup when the patient opens the app.
     *
     * @param context The context
     * @param userId  The user ID
     */
    public static void checkAndSendNotificationForPatient(Context context, String userId) {
        if (userId.isEmpty()) {
            return;
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        User user = dbHelper.getUser(userId);

        if (user == null || user.getPatient() == null) {
            return;
        }

        boolean canSubmitReport = isWeeklyFeedbackAvailable(context, userId, user.getPatient());

        if (canSubmitReport) {
            String message = "Your weekly report is now available to submit. Please log your feedback.";
            dbHelper.saveNotification(userId, message, "timer_expired");
        }

    }

    /**
     * Schedules a notification for when the patient's next weekly feedback becomes available.
     * This method calculates when the patient will be eligible to submit their next report
     * (one week after their last report) and schedules a notification for that time.
     *
     * @param context The context
     * @param userId The user ID
     * @param lastReportDate The date of the patient's last report in format "yyyy-MM-dd'T'HH:mm:ss"
     */
    public static void scheduleNextFeedbackNotification(Context context, String userId, String lastReportDate) {
        if (userId.isEmpty() || lastReportDate == null || lastReportDate.isEmpty()) {
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime reportDate = LocalDateTime.parse(lastReportDate, formatter);
            LocalDateTime nextAvailableDate = reportDate.plusWeeks(1);

            if (nextAvailableDate.isBefore(LocalDateTime.now())) {
                checkAndSendNotificationForPatient(context, userId);
                return;
            }
            Log.d(TAG, "Next feedback will be available for user " + userId + " on " +
                  nextAvailableDate.format(formatter));

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling next feedback notification", e);
        }
    }
}
