package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.stream.Collectors;

public class ProfileSetupActivity extends AppCompatActivity {

    private List<Disease> pubDiseases;
    private List<Medication> pubMedications;
    private Button buttonSubmitProfile;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextAge = findViewById(R.id.editTextAge);
        EditText editTextHeight = findViewById(R.id.editTextHeight);
        EditText editTextWeight = findViewById(R.id.editTextWeight);
        EditText editTextKnownDisease = findViewById(R.id.editTextKnownDisease);
        EditText editTextKnownMedication = findViewById(R.id.editTextKnownMedication);
        buttonSubmitProfile = findViewById(R.id.buttonSubmitProfile);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        // Get the user id from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        User user = dbHelper.getUser(userId); // assuming getUser() returns a User object

        // If user object is null or profile not completed, fetch user profile from database
        if (user != null && user.isProfileCompleted()) {
            editTextName.setText(user.getName());
            editTextAge.setText(String.valueOf(user.getAge()));
            editTextHeight.setText(String.valueOf(user.getHeight()));
            editTextWeight.setText(String.valueOf(user.getWeight()));
            List<Disease> diseases = user.getKnownDiseases(); pubDiseases = diseases;
            List<String> diseaseNames = diseases.stream()
                    .map(Disease::getName)
                    .collect(Collectors.toList());
            List<Medication> medications = user.getKnownMedications(); pubMedications = medications;
            List<String> medicationNames = medications.stream()
                    .map(Medication::getMedicationName)
                    .collect(Collectors.toList());
            editTextKnownDisease.setText(String.join(",", diseaseNames));
            editTextKnownMedication.setText(String.join(" ", medicationNames));
        }

        buttonSubmitProfile.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            int age = Integer.parseInt(editTextAge.getText().toString());
            float height = Float.parseFloat(editTextHeight.getText().toString());
            float weight = Float.parseFloat(editTextWeight.getText().toString());
            String knownDisease = editTextKnownDisease.getText().toString();
            String knownMedication = editTextKnownMedication.getText().toString();

            dbHelper.insertOrUpdateProfile(userId, name, age, height, weight, pubDiseases, pubMedications, 1);

            Intent intent = new Intent(ProfileSetupActivity.this, AddMedicationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.page_1) {
                // Action when first item is selected
            } else if (itemId == R.id.page_2) {
                // Action when second item is selected, e.g. start Profile activity
                startActivity(new Intent(ProfileSetupActivity.this, ProfileSetupActivity.class));
            } else if (itemId == R.id.page_3) {
                // Action when third item is selected, e.g. logout
                startActivity(new Intent(ProfileSetupActivity.this, LoginActivity.class));
            }
            return true;
        });

        if (user != null && user.isProfileCompleted()) {
            bottomNav.setVisibility(View.VISIBLE); // make bottom navigation visible if profile is complete
        } else {
            bottomNav.setVisibility(View.GONE);  // hide it if profile is not complete
        }

    }
}