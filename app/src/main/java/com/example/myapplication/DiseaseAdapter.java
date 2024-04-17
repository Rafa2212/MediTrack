package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
        holder.diseaseNameTextView.setText(disease.getName());
        holder.icd10TextView.setText(disease.getICD10());

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