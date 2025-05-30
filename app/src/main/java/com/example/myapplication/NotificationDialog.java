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
 * Dialog for displaying notifications
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
     * Constructor for the dialog
     * @param context The context
     * @param userId The user ID to get notifications for
     */
    public NotificationDialog(@NonNull Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.notifications = new ArrayList<>();
        this.notificationPrefs = context.getSharedPreferences("NOTIFICATION_PREFS", Context.MODE_PRIVATE);
    }

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
     * Loads notifications for the user
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
