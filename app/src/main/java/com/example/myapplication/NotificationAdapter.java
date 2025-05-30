package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
        // Empty interface, kept for compatibility
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

        NotificationViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.notification_message);
        }
    }
}
