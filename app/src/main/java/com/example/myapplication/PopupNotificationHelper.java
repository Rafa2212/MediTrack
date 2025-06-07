package com.example.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Helper class for creating and showing in-app popup notifications.
 * This class replaces the system notifications with in-app popups for both
 * doctors and patients.
 */
public class PopupNotificationHelper {
    private final Context context;

    /**
     * Constructs a new PopupNotificationHelper with the specified context.
     * 
     * @param context The context used to display popups
     */
    public PopupNotificationHelper(Context context) {
        this.context = context;
    }

    /**
     * Shows a popup notification to a doctor about patient feedback.
     * 
     * @param message The notification message
     * @param activity The current activity where the popup should be displayed
     */
    public void showDoctorNotification(String message, Activity activity) {
        Log.d("PopupNotificationHelper", "showDoctorNotification called with message: " + message);

        if (activity == null || activity.isFinishing()) {
            Log.e("PopupNotificationHelper", "Cannot show notification: activity is null or finishing");
            return;
        }

        try {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_notification);

            TextView titleTextView = dialog.findViewById(R.id.popup_title);
            TextView messageTextView = dialog.findViewById(R.id.popup_message);
            Button viewButton = dialog.findViewById(R.id.popup_view_button);
            Button dismissButton = dialog.findViewById(R.id.popup_dismiss_button);

            titleTextView.setText("Patient Feedback");
            messageTextView.setText(message);

            viewButton.setText("View Reports");
            viewButton.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(context, DrReportsActivity.class);
                context.startActivity(intent);
            });

            dismissButton.setOnClickListener(v -> dialog.dismiss());

            Log.d("PopupNotificationHelper", "Showing doctor notification dialog");
            dialog.show();
            Log.d("PopupNotificationHelper", "Doctor notification dialog shown");
        } catch (Exception e) {
            Log.e("PopupNotificationHelper", "Error showing doctor notification", e);
        }
    }

    /**
     * Shows a popup notification to a patient about weekly feedback.
     * 
     * @param message The notification message
     * @param activity The current activity where the popup should be displayed
     */
    public void showPatientNotification(String message, Activity activity) {
        Log.d("PopupNotificationHelper", "showPatientNotification called with message: " + message);

        if (activity == null || activity.isFinishing()) {
            Log.e("PopupNotificationHelper", "Cannot show notification: activity is null or finishing");
            return;
        }

        try {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_notification);

            TextView titleTextView = dialog.findViewById(R.id.popup_title);
            TextView messageTextView = dialog.findViewById(R.id.popup_message);
            Button viewButton = dialog.findViewById(R.id.popup_view_button);
            Button dismissButton = dialog.findViewById(R.id.popup_dismiss_button);

            titleTextView.setText("Weekly Feedback");
            messageTextView.setText(message);

            viewButton.setText("Submit Feedback");
            viewButton.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(context, PtFeedbackActivity.class);
                context.startActivity(intent);
            });

            dismissButton.setOnClickListener(v -> dialog.dismiss());

            Log.d("PopupNotificationHelper", "Showing patient notification dialog");
            dialog.show();
            Log.d("PopupNotificationHelper", "Patient notification dialog shown");
        } catch (Exception e) {
            Log.e("PopupNotificationHelper", "Error showing patient notification", e);
        }
    }
}
