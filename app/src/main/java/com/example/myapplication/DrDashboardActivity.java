package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * Activity that displays the doctor's dashboard with their information and list of patients.
 * This activity shows the doctor's name and specialty, provides a list of patients assigned
 * to the doctor, and includes a floating action button to add new patients.
 * The bottom navigation is configured specifically for doctor users.
 */
public class DrDashboardActivity extends BaseActivity {

    /**
     * Initializes the doctor dashboard activity, sets up UI components, and loads doctor data.
     * This method performs several key operations:
     * - Sets up the activity layout and window transitions
     * - Configures the bottom navigation bar for doctor users
     * - Sets up the floating action button to add new patients
     * - Retrieves and displays the doctor's name and specialty
     * - Loads and displays the list of patients assigned to the doctor
     * - Shows appropriate UI based on whether the doctor has patients or not
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_doctor_dashboard);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        TextView welcomeText = findViewById(R.id.doctor_welcome_text);
        RecyclerView patientsRecyclerView = findViewById(R.id.patients_recyclerview);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_doctor_dashboard);

        FloatingActionButton fabAddPatient = findViewById(R.id.fab_add_patient);
        fabAddPatient.setOnClickListener(v -> {
            Intent intent = new Intent(DrDashboardActivity.this, DrProfileActivity.class);
            intent.putExtra("create_new_patient", true);
            startActivity(intent);
        });

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = preferences.getString("userId", "");

        if (!doctorId.isEmpty()) {
            User doctor = dbHelper.getUser(doctorId);
            if (doctor != null && doctor.getUserProfile() != null) {
                String doctorName = doctor.getUserProfile().getName();
                String specialty = doctor.getUserProfile().getSpecialty();

                String welcomeMsg = doctorName;
                if (specialty != null && !specialty.isEmpty()) {
                    welcomeMsg += " | " + specialty;
                }
                welcomeText.setText(welcomeMsg);

                List<User> patients = dbHelper.getPatientsForDoctor(doctorId);

                patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                PatientAdapter patientAdapter = new PatientAdapter(this, patients);
                patientsRecyclerView.setAdapter(patientAdapter);

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
