package com.example.myapplication;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private final List<User> patients;
    private final Context context;

    public PatientAdapter(Context context, List<User> patients) {
        this.context = context;
        this.patients = patients;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        User patient = patients.get(position);
        UserProfile profile = patient.getUserProfile();

        if (profile != null) {
            holder.patientName.setText(profile.getName());
            holder.patientAge.setText("Age: " + profile.getAge());

            // Get the latest medical report date when the patient actually logged data
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(patient.getUserId());
            if (latestReport != null && latestReport.getReportDate() != null && !latestReport.getReportDate().isEmpty()) {
                String lastReportDate = latestReport.getReportDate();
                String formattedDate = "";
                try {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(lastReportDate, inputFormatter);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
                    formattedDate = dateTime.format(outputFormatter);
                    holder.patientMedicalReport.setText("Last Medical Report: " + formattedDate);
                } catch (Exception e) {
                    holder.patientMedicalReport.setText("Last Medical Report: " + lastReportDate); // Fallback to original string if parsing fails
                }
            } else {
                holder.patientMedicalReport.setText("");
            }

            // Set transition names for shared elements
            holder.patientName.setTransitionName("patient_name_" + patient.getUserId());
            holder.patientAge.setTransitionName("patient_age_" + patient.getUserId());
            holder.patientMedicalReport.setTransitionName("patient_report_" + patient.getUserId());

            // Set up deassign button click listener
            holder.btnDeassign.setOnClickListener(v -> {
                // Get the current doctor ID from SharedPreferences
                SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
                String doctorId = preferences.getString("userId", "");

                if (!doctorId.isEmpty()) {
                    // Deassign the patient from the doctor
                    boolean success = dbHelper.deassignPatientFromDoctor(doctorId, patient.getUserId());

                    if (success) {
                        // Remove the patient from the list
                        patients.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, patients.size());

                        // Show success message
                        Snackbar.make(v,
                                "Patient deassigned successfully!",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        // Show error message
                        Snackbar.make(v,
                                "Failed to deassign!",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DoctorPatientsActivity.class);
                intent.putExtra("patient_id", patient.getUserId());

                // Create pairs of shared elements for the transition
                Pair<View, String> p1 = Pair.create((View) holder.patientName, holder.patientName.getTransitionName());
                Pair<View, String> p2 = Pair.create((View) holder.patientAge, holder.patientAge.getTransitionName());
                Pair<View, String> p3 = Pair.create((View) holder.patientMedicalReport, holder.patientMedicalReport.getTransitionName());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) context, p1, p2, p3);

                context.startActivity(intent, options.toBundle());
            });
        }
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientName;
        TextView patientAge;
        TextView patientMedicalReport;
        Button btnDeassign;

        PatientViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            patientAge = itemView.findViewById(R.id.patient_age);
            patientMedicalReport = itemView.findViewById(R.id.patient_medical_report);
            btnDeassign = itemView.findViewById(R.id.btn_deassign);
        }
    }
}
