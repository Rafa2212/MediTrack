package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private Context context;
    private List<Medication> medicationList;

    public MedicationAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.medication_item, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.medicationNameTextView.setText(medication.getMedicationName());
//        holder.dosageTextView.setText(medication.getDosage());
//        holder.frequencyTextView.setText(medication.getFrequency());
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medicationNameTextView;
//        TextView dosageTextView;
//        TextView frequencyTextView;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            medicationNameTextView = itemView.findViewById(R.id.medicationNameTextView);
//            dosageTextView = itemView.findViewById(R.id.dosageTextView);
//            frequencyTextView = itemView.findViewById(R.id.frequencyTextView);
        }
    }
}
