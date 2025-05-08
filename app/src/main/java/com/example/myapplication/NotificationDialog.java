package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
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
public class NotificationDialog extends Dialog implements NotificationAdapter.NotificationActionListener {
    private final Context context;
    private final String userId;
    private final DatabaseHelper dbHelper;
    private List<Notification> notifications;
    private NotificationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyText;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notifications);

        // Initialize views
        recyclerView = findViewById(R.id.notifications_recyclerview);
        emptyText = findViewById(R.id.empty_notifications_text);
        Button closeButton = findViewById(R.id.button_close);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new NotificationAdapter(context, notifications, this);
        recyclerView.setAdapter(adapter);

        // Set up close button
        closeButton.setOnClickListener(v -> dismiss());

        // Load notifications
        loadNotifications();
    }

    /**
     * Loads notifications for the user
     */
    private void loadNotifications() {
        // Get notifications from the database
        List<Notification> userNotifications = dbHelper.getNotificationsForUser(userId);
        
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

    @Override
    public void onMarkAsRead(Notification notification) {
        // Mark the notification as read in the database
        dbHelper.markNotificationAsRead(notification.getId());
        
        // Update the notification in the list
        notification.setRead(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDismiss(Notification notification) {
        // Delete the notification from the database
        dbHelper.deleteNotification(notification.getId());
        
        // Remove the notification from the list
        notifications.remove(notification);
        adapter.notifyDataSetChanged();
        
        // Show empty text if there are no notifications
        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        }
    }
}