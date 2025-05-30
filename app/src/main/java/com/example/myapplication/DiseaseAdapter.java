package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {
    private final Context context;
    private final List<Disease> diseaseList;
    OnDiseaseActionListener onDiseaseActionListener;

    public DiseaseAdapter(
            Context context, List<Disease> diseaseList, OnDiseaseActionListener onDiseaseActionListener) {
        this.context = context;
        this.diseaseList = diseaseList;
        this.onDiseaseActionListener = onDiseaseActionListener;
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.disease_item, parent, false);
        return new DiseaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        Disease disease = diseaseList.get(position);

        // Format the disease name to include doctor and date information
        String doctorInfo = "";
        if (disease.getDoctorName() != null && !disease.getDoctorName().isEmpty()) {
            doctorInfo = " (Dr. " + disease.getDoctorName();

            if (disease.getDiagnosisDate() != null && !disease.getDiagnosisDate().isEmpty()) {
                doctorInfo += ", " + disease.getDiagnosisDate();
            }

            doctorInfo += ")";
        }

        // Set the disease name with doctor info
        holder.diseaseNameTextView.setText(disease.getName() + doctorInfo);

        // Make the text smaller to fit on one line
        holder.diseaseNameTextView.setTextSize(14); // Smaller text size

        holder.icd10TextView.setText(disease.getICD10());

        // Get current doctor ID from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String currentDoctorId = preferences.getString("userId", "");

        // Check if the current doctor is the creator of this disease
        boolean isCreator = disease.getDoctorId() != null && disease.getDoctorId().equals(currentDoctorId);

        // Enable or disable the delete button based on whether the current doctor is the creator
        holder.deleteButton.setEnabled(isCreator);

        // Change the appearance of the delete button based on whether it's enabled
        if (isCreator) {
            // Button is enabled - use normal color
            holder.deleteButton.setAlpha(1.0f);
        } else {
            // Button is disabled - grey it out
            holder.deleteButton.setAlpha(0.3f);
        }

        holder.deleteButton.setOnClickListener(v -> onDiseaseActionListener.onDeleteDisease(disease));
    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    public interface OnDiseaseActionListener { void onDeleteDisease(Disease disease); }

    public static class DiseaseViewHolder extends RecyclerView.ViewHolder {
        TextView diseaseNameTextView;
        TextView icd10TextView;
        ImageButton deleteButton;

        public DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            diseaseNameTextView = itemView.findViewById(R.id.diseaseNameTextView);
            icd10TextView = itemView.findViewById(R.id.icd10TextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
