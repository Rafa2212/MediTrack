package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
    private final List<Disease> diseases;

    public CarouselAdapter(List<Disease> diseases) {
        this.diseases = diseases;
    }

    @NotNull
    @Override
    public CarouselAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.disease_widget_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(CarouselAdapter.ViewHolder holder, int position) {
        Disease disease = diseases.get(position);
        holder.diseaseNameText.setText(disease.getName() + " (" + disease.getICD10() + ")");

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

            ScrollView scrollView = new ScrollView(v.getContext());
            TextView textView = new TextView(v.getContext());

            textView.setPadding(32, 32, 32, 32);
            textView.setText(disease.getInterpretation());

            scrollView.addView(textView);
            builder.setView(scrollView)
                    .setTitle("Disease ICD10 Code")
                    .setNegativeButton("CLOSE", (dialog, id) -> dialog.dismiss());
            builder.create().show();
        });
    }

    @Override
    public int getItemCount() {
        return diseases.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView diseaseNameText;

        public ViewHolder(View itemView) {
            super(itemView);
            diseaseNameText = itemView.findViewById(R.id.diseaseNameTextView);
        }
    }

    public static class CarouselItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;

        public CarouselItemDecoration(int space) {
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