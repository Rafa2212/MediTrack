package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class DoctorDashboardActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private TextView welcomeText;
    private RecyclerView patientsRecyclerView;
    private PatientAdapter patientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable window content transitions
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_doctor_dashboard);

        dbHelper = DatabaseHelper.getInstance(this);
        welcomeText = findViewById(R.id.doctor_welcome_text);
        patientsRecyclerView = findViewById(R.id.patients_recyclerview);

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_doctor_dashboard);

        // Set up add patient button
        FloatingActionButton fabAddPatient = findViewById(R.id.fab_add_patient);
        fabAddPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorDashboardActivity.this, ProfileSetupActivity.class);
                intent.putExtra("create_new_patient", true);
                startActivity(intent);
            }
        });

        // Get current doctor ID from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = preferences.getString("userId", "");

        if (!doctorId.isEmpty()) {
            // Get doctor information
            User doctor = dbHelper.getUser(doctorId);
            if (doctor != null && doctor.getUserProfile() != null) {
                // Set welcome message with doctor's name and specialty
                String doctorName = doctor.getUserProfile().getName();
                String specialty = doctor.getUserProfile().getSpecialty();

                // Create a more minimalist welcome message that includes specialty
                String welcomeMsg = doctorName;
                if (specialty != null && !specialty.isEmpty()) {
                    welcomeMsg += " | " + specialty;
                }
                welcomeText.setText(welcomeMsg);

                // Get patients for this doctor
                List<User> patients = dbHelper.getPatientsForDoctor(doctorId);

                // Set up RecyclerView
                patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                // Set up the adapter with patients
                patientAdapter = new PatientAdapter(this, patients);
                patientsRecyclerView.setAdapter(patientAdapter);

                // Show a message if there are no patients
                TextView noPatientsMsgView = findViewById(R.id.no_patients_message);
                if (patients.isEmpty()) {
                    noPatientsMsgView.setVisibility(View.VISIBLE);
                    patientsRecyclerView.setVisibility(View.GONE);
                } else {
                    noPatientsMsgView.setVisibility(View.GONE);
                    patientsRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
