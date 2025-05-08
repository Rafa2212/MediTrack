package com.example.myapplication;

/**
 * Represents a notification in the system.
 * Notifications can be for patients (when timer expires) or for doctors (when a patient submits feedback).
 */
public class Notification {
    private String id;
    private String userId;
    private String message;
    private String date;
    private boolean isRead;
    private String type;

    /**
     * Constructor for creating a new notification
     * @param id The notification ID
     * @param userId The user ID this notification is for
     * @param message The notification message
     * @param date The date the notification was created
     * @param isRead Whether the notification has been read
     * @param type The type of notification (e.g., "timer_expired", "feedback_submitted")
     */
    public Notification(String id, String userId, String message, String date, boolean isRead, String type) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
        this.type = type;
    }

    /**
     * Gets the notification ID
     * @return The notification ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the notification ID
     * @param id The notification ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user ID this notification is for
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID this notification is for
     * @param userId The user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the notification message
     * @return The notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message
     * @param message The notification message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the date the notification was created
     * @return The date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date the notification was created
     * @param date The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Checks if the notification has been read
     * @return true if the notification has been read, false otherwise
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Sets whether the notification has been read
     * @param read true if the notification has been read, false otherwise
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Gets the type of notification
     * @return The notification type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of notification
     * @param type The notification type
     */
    public void setType(String type) {
        this.type = type;
    }
}