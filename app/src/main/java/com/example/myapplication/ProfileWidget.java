package com.example.myapplication;

/**
 * Represents a health widget displayed in the application.
 * This class encapsulates information about a health metric widget including
 * its type, title, description, associated image resource, and health interpretation.
 * Health widgets are used to display various health metrics like BMI, metabolic balance,
 * and other health indicators in a consistent format.
 */
public class ProfileWidget {
    private final String type;
    private final String title;
    private final String description;
    private final int imageResource;
    private final String interpretation;

    /**
     * Constructs a new HealthWidget with the specified properties.
     *
     * @param type The type of health metric (e.g., "BMI", "Metabolic")
     * @param title The display title of the widget
     * @param description A brief description of the health metric
     * @param imageResource The resource ID for the background image
     * @param interpretation The health interpretation or analysis text
     */
    public ProfileWidget(String type, String title, String description, int imageResource, String interpretation) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.interpretation = interpretation;
    }

    /**
     * Gets the type of health metric this widget represents.
     *
     * @return The health metric type (e.g., "BMI", "Metabolic")
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the display title of the widget.
     *
     * @return The widget's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the health metric.
     *
     * @return A brief description of the health metric
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the resource ID for the background image.
     *
     * @return The resource ID for the widget's background image
     */
    public int getImageResource() {
        return imageResource;
    }

    /**
     * Gets the health interpretation or analysis text.
     *
     * @return The detailed health interpretation or analysis
     */
    public String getInterpretation() {
        return interpretation;
    }
}
