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
 * Adapter for displaying notifications in a RecyclerView.
 * This adapter binds Notification objects to views that display notification messages.
 * It provides methods for creating view holders, binding data, and updating the notification list.
 * The adapter uses a NotificationActionListener interface for handling notification interactions,
 * though it's currently kept empty for compatibility.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final List<Notification> notifications;
    private final Context context;
    private final NotificationActionListener listener;

    /**
     * Interface for handling notification-related actions.
     * This interface is designed to allow activities or fragments to respond to
     * notification interactions such as clicks or dismissals. Currently, it's
     * kept empty for compatibility with future implementations, but could be
     * extended to include methods for handling specific notification events.
     */
    public interface NotificationActionListener {
        // Empty interface, kept for compatibility
    }

    /**
     * Constructs a new NotificationAdapter with the specified context, notifications, and listener.
     * This constructor initializes the adapter with a list of notifications to display and
     * a listener for handling notification interactions. The context is used for resource access
     * and inflating layouts.
     *
     * @param context The context used for accessing resources and inflating layouts
     * @param notifications The list of Notification objects to display in the RecyclerView
     * @param listener The listener for handling notification action events (currently unused)
     */
    public NotificationAdapter(Context context, List<Notification> notifications, NotificationActionListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the notification item layout.
     * This method is called when the RecyclerView needs a new ViewHolder to represent an item.
     * It inflates the notification_item layout and creates a NotificationViewHolder with the inflated view.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View (not used in this implementation as there's only one view type)
     * @return A new NotificationViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds data from a Notification to the views in the ViewHolder.
     * This method is called by the RecyclerView to display the data at the specified position.
     * It retrieves the appropriate Notification from the list and sets the message text
     * in the ViewHolder's TextView.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *               Notification at the given position
     * @param position The position of the Notification within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.messageTextView.setText(notification.getMessage());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * This method is used by the RecyclerView to determine how many items to display.
     *
     * @return The total number of notifications in the adapter's data set
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Updates the adapter's data set with a new list of notifications.
     * This method clears the current list of notifications, adds all items from the new list,
     * and notifies the adapter that the data set has changed to refresh the UI.
     * This is typically called when new notifications are received or when the notification
     * list needs to be refreshed.
     *
     * @param newNotifications The new list of Notification objects to display
     */
    public void updateNotifications(List<Notification> newNotifications) {
        notifications.clear();
        notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for notification items in the RecyclerView.
     * This class holds references to the views within each notification item layout
     * and provides access to these views for binding data. It contains a TextView
     * for displaying the notification message.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        /** TextView that displays the notification message */
        TextView messageTextView;

        /**
         * Constructs a new NotificationViewHolder with the specified item view.
         * This constructor initializes the ViewHolder by finding and storing a reference
         * to the message TextView within the notification item layout.
         *
         * @param itemView The view containing the notification item layout
         */
        NotificationViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.notification_message);
        }
    }
}
