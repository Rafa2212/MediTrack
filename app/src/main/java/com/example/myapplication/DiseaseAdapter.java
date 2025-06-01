package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        Disease disease = diseaseList.get(position);

        String doctorInfo = "";
        if (disease.getDoctorName() != null && !disease.getDoctorName().isEmpty()) {
            doctorInfo = " (Dr. " + disease.getDoctorName();

            if (disease.getDiagnosisDate() != null && !disease.getDiagnosisDate().isEmpty()) {
                doctorInfo += ", " + disease.getDiagnosisDate();
            }

            doctorInfo += ")";
        }

        holder.diseaseNameTextView.setText(disease.getName() + doctorInfo);

        holder.diseaseNameTextView.setTextSize(14);

        holder.icd10TextView.setText(disease.getICD10());

        SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String currentDoctorId = preferences.getString("userId", "");

        boolean isCreator = disease.getDoctorId() != null && disease.getDoctorId().equals(currentDoctorId);

        holder.deleteButton.setEnabled(isCreator);

        if (isCreator) {
            holder.deleteButton.setAlpha(1.0f);
        } else {
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
