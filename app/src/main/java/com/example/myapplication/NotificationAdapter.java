package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying notifications in a RecyclerView
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final List<Notification> notifications;
    private final Context context;
    private final NotificationActionListener listener;

    /**
     * Interface for notification actions
     */
    public interface NotificationActionListener {
        void onMarkAsRead(Notification notification);
        void onDismiss(Notification notification);
    }

    /**
     * Constructor for the adapter
     * @param context The context
     * @param notifications The list of notifications
     * @param listener The listener for notification actions
     */
    public NotificationAdapter(Context context, List<Notification> notifications, NotificationActionListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        holder.messageTextView.setText(notification.getMessage());
        
        // Format the date for display
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(notification.getDate(), inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.getDefault());
            String formattedDate = dateTime.format(outputFormatter);
            holder.dateTextView.setText(formattedDate);
        } catch (Exception e) {
            holder.dateTextView.setText(notification.getDate());
        }
        
        // Set button visibility based on read status
        if (notification.isRead()) {
            holder.markReadButton.setVisibility(View.GONE);
        } else {
            holder.markReadButton.setVisibility(View.VISIBLE);
        }
        
        // Set up button click listeners
        holder.markReadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkAsRead(notification);
            }
        });
        
        holder.dismissButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDismiss(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Updates the notifications list
     * @param newNotifications The new list of notifications
     */
    public void updateNotifications(List<Notification> newNotifications) {
        notifications.clear();
        notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for notification items
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView dateTextView;
        Button markReadButton;
        Button dismissButton;

        NotificationViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.notification_message);
            dateTextView = itemView.findViewById(R.id.notification_date);
            markReadButton = itemView.findViewById(R.id.button_mark_read);
            dismissButton = itemView.findViewById(R.id.button_dismiss);
        }
    }
}