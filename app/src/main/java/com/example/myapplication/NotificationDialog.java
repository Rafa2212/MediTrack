package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for displaying notifications to the user.
 * This dialog shows the most recent notification for a specific user,
 * retrieved from the database. It displays the notification in a RecyclerView
 * and provides a close button that marks the notification as shown.
 * If there are no notifications, it displays an empty state message.
 */
public class NotificationDialog extends Dialog {
    private final Context context;
    private final String userId;
    private final DatabaseHelper dbHelper;
    private List<Notification> notifications;
    private NotificationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private SharedPreferences notificationPrefs;

    /**
     * Constructs a new NotificationDialog with the specified context and user ID.
     * This constructor initializes the dialog with the necessary components to display
     * notifications for a specific user. It sets up the database helper, creates an empty
     * notifications list, and initializes the shared preferences for tracking notification state.
     *
     * @param context The context used to access resources and the database
     * @param userId The ID of the user whose notifications to display
     */
    public NotificationDialog(@NonNull Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.notifications = new ArrayList<>();
        this.notificationPrefs = context.getSharedPreferences("NOTIFICATION_PREFS", Context.MODE_PRIVATE);
    }

    /**
     * Initializes the dialog's UI components and sets up event listeners.
     * This method is called when the dialog is being shown. It performs several key operations:
     * - Removes the dialog title for a cleaner look
     * - Sets a transparent background for the dialog window
     * - Initializes the RecyclerView, empty state text view, and close button
     * - Sets up the RecyclerView with a LinearLayoutManager and NotificationAdapter
     * - Configures the close button to mark notifications as shown and dismiss the dialog
     * - Loads notifications from the database
     *
     * @param savedInstanceState If the dialog is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notifications);

        // Set dialog window properties for a more minimalist look
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        recyclerView = findViewById(R.id.notifications_recyclerview);
        emptyText = findViewById(R.id.empty_notifications_text);
        Button closeButton = findViewById(R.id.button_close);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new NotificationAdapter(context, notifications, new NotificationAdapter.NotificationActionListener() {});
        recyclerView.setAdapter(adapter);

        // Set up close button
        closeButton.setOnClickListener(v -> {
            // Mark that we've shown the notification
            SharedPreferences.Editor editor = notificationPrefs.edit();
            editor.putBoolean("notification_shown_" + userId, true);
            editor.apply();
            dismiss();
        });

        // Load notifications
        loadNotifications();
    }

    /**
     * Loads and displays notifications for the user from the database.
     * This method performs several operations:
     * 1. Retrieves all notifications for the user from the database
     * 2. If notifications exist, sorts them by date (newest first) and selects only the most recent one
     * 3. Updates the adapter with the filtered notifications
     * 4. Manages the visibility of the RecyclerView and empty state text view based on whether
     *    there are notifications to display
     * 
     * The method implements a design decision to show only the most recent notification
     * to the user, rather than a list of all notifications.
     */
    private void loadNotifications() {
        // Get notifications from the database
        List<Notification> userNotifications = dbHelper.getNotificationsForUser(userId);

        // Only show the most recent notification
        if (!userNotifications.isEmpty()) {
            // Sort by date (newest first) and take only the first one
            userNotifications.sort((n1, n2) -> n2.getDate().compareTo(n1.getDate()));
            userNotifications = userNotifications.subList(0, 1);
        }

        // Update the adapter
        notifications.clear();
        notifications.addAll(userNotifications);
        adapter.notifyDataSetChanged();

        // Show empty text if there are no notifications
        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}
