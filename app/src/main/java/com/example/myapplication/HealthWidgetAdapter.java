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

public class HealthWidgetAdapter extends RecyclerView.Adapter<HealthWidgetAdapter.ViewHolder> {
    private final List<HealthWidget> healthWidgets;

    public HealthWidgetAdapter(List<HealthWidget> healthWidgets) {
        this.healthWidgets = healthWidgets;
    }

    @NotNull
    @Override
    public HealthWidgetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.health_widget_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(HealthWidgetAdapter.ViewHolder holder, int position) {
        HealthWidget healthWidget = healthWidgets.get(position);
        holder.titleTextView.setText(healthWidget.getTitle());
        holder.descriptionTextView.setText(healthWidget.getDescription());
        holder.imageView.setImageResource(healthWidget.getImageResource());

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

            ScrollView scrollView = new ScrollView(v.getContext());
            TextView textView = new TextView(v.getContext());

            textView.setPadding(32, 32, 32, 32);
            textView.setText(healthWidget.getInterpretation());

            scrollView.addView(textView);
            builder.setView(scrollView)
                    .setTitle(healthWidget.getTitle() + " Interpretation")
                    .setNegativeButton("CLOSE", (dialog, id) -> dialog.dismiss());
            builder.create().show();
        });
    }

    @Override
    public int getItemCount() {
        return healthWidgets != null ? healthWidgets.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.healthTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.healthDescriptionTextView);
            imageView = itemView.findViewById(R.id.healthImageView);
        }
    }

    public static class HealthWidgetItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;

        public HealthWidgetItemDecoration(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NotNull View view, @NotNull RecyclerView parent,
                                   @NotNull RecyclerView.State state) {
            outRect.left = mSpace;
            outRect.right = mSpace;
        }
    }
}