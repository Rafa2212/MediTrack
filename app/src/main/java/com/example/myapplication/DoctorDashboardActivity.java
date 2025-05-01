package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        // Get current doctor ID from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = preferences.getString("userId", "");

        if (!doctorId.isEmpty()) {
            // Get doctor information
            User doctor = dbHelper.getUser(doctorId);
            if (doctor != null && doctor.getUserProfile() != null) {
                // Set welcome message with doctor's name
                String doctorName = doctor.getUserProfile().getName();
                welcomeText.setText("Welcome " + doctorName + ". You can manage your patients here.");

                // Get patients for this doctor
                List<User> patients = dbHelper.getPatientsForDoctor(doctorId);

                // Set up RecyclerView
                patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                // Set up the adapter with patients
                patientAdapter = new PatientAdapter(this, patients);
                patientsRecyclerView.setAdapter(patientAdapter);
            }
        }
    }
}
