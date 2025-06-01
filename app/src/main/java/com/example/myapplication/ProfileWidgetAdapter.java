package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for displaying health widgets in a RecyclerView.
 * This adapter binds HealthWidget objects to card views that display health metrics
 * such as BMI and metabolic balance. When a health widget is clicked, it shows a dialog
 * with a detailed interpretation of the health metric.
 */
public class ProfileWidgetAdapter extends RecyclerView.Adapter<ProfileWidgetAdapter.ViewHolder> {
    private final List<ProfileWidget> profileWidgets;

    /**
     * Constructs a new HealthWidgetAdapter with the specified list of health widgets.
     *
     * @param profileWidgets The list of HealthWidget objects to display in the RecyclerView
     */
    public ProfileWidgetAdapter(List<ProfileWidget> profileWidgets) {
        this.profileWidgets = profileWidgets;
    }

    /**
     * Creates a new ViewHolder by inflating the health widget item layout.
     *
     * @param parent The parent ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (not used in this implementation)
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NotNull
    @Override
    public ProfileWidgetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.health_widget_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data from a HealthWidget to the views in the ViewHolder.
     * Sets the title, description, and image for the health widget.
     * Also sets up a click listener that displays a dialog with the detailed
     * interpretation of the health metric when the item is clicked.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ProfileWidgetAdapter.ViewHolder holder, int position) {
        ProfileWidget profileWidget = profileWidgets.get(position);
        holder.titleTextView.setText(profileWidget.getTitle());
        holder.descriptionTextView.setText(profileWidget.getDescription());
        holder.imageView.setImageResource(profileWidget.getImageResource());

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

            ScrollView scrollView = new ScrollView(v.getContext());
            TextView textView = new TextView(v.getContext());

            textView.setPadding(32, 32, 32, 32);
            textView.setText(profileWidget.getInterpretation());

            scrollView.addView(textView);
            builder.setView(scrollView)
                    .setTitle(profileWidget.getTitle() + " Interpretation")
                    .setNegativeButton("CLOSE", (dialog, id) -> dialog.dismiss());
            builder.create().show();
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * Safely handles the case where the healthWidgets list might be null.
     *
     * @return The total number of items in this adapter
     */
    @Override
    public int getItemCount() {
        return profileWidgets != null ? profileWidgets.size() : 0;
    }

    /**
     * ViewHolder class for health widget items.
     * Holds references to the views within each item in the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public ImageView imageView;

        /**
         * Constructs a new ViewHolder and finds all the required views from the item layout.
         *
         * @param itemView The view containing the health widget item layout
         */
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.healthTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.healthDescriptionTextView);
            imageView = itemView.findViewById(R.id.healthImageView);
        }
    }

    /**
     * Item decoration class that adds horizontal spacing between health widget items.
     * This decoration applies left and right margins to items in the RecyclerView.
     */
    public static class HealthWidgetItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;

        /**
         * Constructs a new HealthWidgetItemDecoration with the specified spacing.
         *
         * @param space The size of the spacing to add between items, in pixels
         */
        public HealthWidgetItemDecoration(int space) {
            this.mSpace = space;
        }

        /**
         * Sets the offsets for the item views, effectively adding spacing between items.
         * This implementation adds horizontal spacing (left and right margins).
         *
         * @param outRect The Rect to receive the output offsets
         * @param view The child view to decorate
         * @param parent The RecyclerView this ItemDecoration is decorating
         * @param state The current RecyclerView.State
         */
        @Override
        public void getItemOffsets(Rect outRect, @NotNull View view, @NotNull RecyclerView parent,
                                   @NotNull RecyclerView.State state) {
            outRect.left = mSpace;
            outRect.right = mSpace;
        }
    }
}
