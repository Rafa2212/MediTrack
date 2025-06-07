package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying patients in a RecyclerView for doctor users.
 * This adapter binds User objects with patient role to views that display
 * patient information including name, age, and last medical report date.
 * It provides functionality for deassigning patients and navigating to
 * patient details with shared element transitions.
 */
public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private final List<User> patients;
    private final Context context;

    /**
     * Constructs a new PatientAdapter with the specified context and list of patients.
     *
     * @param context The context used for accessing resources and services
     * @param patients The list of User objects with patient role to display
     */
    public PatientAdapter(Context context, List<User> patients) {
        this.context = context;
        this.patients = patients;
    }

    /**
     * Creates a new ViewHolder by inflating the patient item layout.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (not used in this implementation)
     * @return A new PatientViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    /**
     * Binds data from a patient User object to the views in the ViewHolder.
     * This method sets the patient's name, age, and last medical report date.
     * It also configures transition names for shared element transitions,
     * sets up a click listener for deassigning the patient, and a click listener
     * for navigating to the patient's details.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        User patient = patients.get(position);
        Patient profile = patient.getPatient();

        if (profile != null) {
            holder.patientName.setText(profile.getName());
            holder.patientAge.setText("Age: " + profile.getAge());

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
                    holder.patientMedicalReport.setText("Last Medical Report: " + lastReportDate);
                }
            } else {
                holder.patientMedicalReport.setText("");
            }

            holder.patientName.setTransitionName("patient_name_" + patient.getUserId());
            holder.patientAge.setTransitionName("patient_age_" + patient.getUserId());
            holder.patientMedicalReport.setTransitionName("patient_report_" + patient.getUserId());

            holder.btnDeassign.setOnClickListener(v -> {
                SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
                String doctorId = preferences.getString("userId", "");

                if (!doctorId.isEmpty()) {
                    boolean success = dbHelper.deassignPatientFromDoctor(doctorId, patient.getUserId());

                    if (success) {
                        patients.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, patients.size());

                        Snackbar.make(v,
                                "Patient deassigned successfully!",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(v,
                                "Failed to deassign!",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DrPatientsActivity.class);
                intent.putExtra("patient_id", patient.getUserId());

                Pair<View, String> p1 = Pair.create(holder.patientName, holder.patientName.getTransitionName());
                Pair<View, String> p2 = Pair.create(holder.patientAge, holder.patientAge.getTransitionName());
                Pair<View, String> p3 = Pair.create(holder.patientMedicalReport, holder.patientMedicalReport.getTransitionName());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) context, p1, p2, p3);

                context.startActivity(intent, options.toBundle());
            });
        }
    }

    /**
     * Returns the total number of patients in the data set.
     *
     * @return The total number of patients
     */
    @Override
    public int getItemCount() {
        return patients.size();
    }

    /**
     * ViewHolder class for patient items in the RecyclerView.
     * Holds references to the views within each patient item layout,
     * including the patient name, age, medical report, and deassign button.
     */
    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientName;
        TextView patientAge;
        TextView patientMedicalReport;
        Button btnDeassign;

        /**
         * Constructs a new PatientViewHolder with the specified item view.
         * Finds and stores references to the views within the patient item layout.
         *
         * @param itemView The view containing the patient item layout
         */
        PatientViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            patientAge = itemView.findViewById(R.id.patient_age);
            patientMedicalReport = itemView.findViewById(R.id.patient_medical_report);
            btnDeassign = itemView.findViewById(R.id.btn_deassign);
        }
    }
}
