package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Activity that handles user authentication and login.
 * This activity verifies user credentials, manages user sessions, and navigates
 * to the appropriate activity based on the user's role (patient or doctor).
 * It also handles automatic login for users with existing sessions.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private DatabaseHelper dbHelper;

    /**
     * Initializes the activity, sets up UI components, and checks for existing user sessions.
     * If a user is already logged in (has a valid userId in SharedPreferences), this method
     * automatically navigates to the appropriate activity based on the user's role:
     * - ProfileSetupActivity if the user profile is not set up
     * - DoctorDashboardActivity if the user is a doctor
     * - DashboardActivity if the user is a patient
     * If no user is logged in, it displays the login form and sets up the login button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        if (!userId.isEmpty()) {
            dbHelper = DatabaseHelper.getInstance(this);
            User user = dbHelper.getUser(userId);

            if (user != null) {
                Intent intent;

                if (user.getUserProfile() == null) {
                    // User needs to set up profile
                    intent = new Intent(LoginActivity.this, DrProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else if ("doctor".equals(user.getRole())) {
                    // Doctor dashboard
                    intent = new Intent(this, DrDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    intent = new Intent(this, PtDashboardActivity.class);

                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                    finish();
                }
                return;
            }
        }

        setContentView(R.layout.activity_login);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        dbHelper = DatabaseHelper.getInstance(this);

        UserTest.addDoctorForTesting(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        String lastUsername = preferences.getString("last_username", "");
        if (!lastUsername.isEmpty()) {
            editTextUsername.setText(lastUsername);
        }

        buttonLogin.setOnClickListener(v -> login());
    }

    /**
     * Validates user credentials and performs the login process.
     * This method retrieves the username and password from the input fields,
     * validates that they are not empty, and checks them against the database.
     * If authentication is successful, it saves the user session, shows a success message,
     * and navigates to the appropriate activity based on the user's role:
     * - ProfileSetupActivity if the user profile is not set up
     * - DoctorDashboardActivity if the user is a doctor
     * - DashboardActivity if the user is a patient
     * If authentication fails, it displays an error message.
     */
    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Snackbar
                    .make(findViewById(android.R.id.content), "Please enter both username and password",
                            Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        User user = dbHelper.checkUser(username, password);

        if (user != null) {
            Snackbar.make(findViewById(android.R.id.content), "Login successful", Snackbar.LENGTH_SHORT)
                    .show();
            onUserLoggedIn(user.getUserId());

            Intent intent;
            if (user.getUserProfile() == null) {
                intent = new Intent(LoginActivity.this, DrProfileActivity.class);
                startActivity(intent);
            } else {
                if ("doctor".equals(user.getRole())) {
                    intent = new Intent(this, DrDashboardActivity.class);
                } else {
                    intent = new Intent(this, PtDashboardActivity.class);
                }
                startActivity(intent);
            }
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            Snackbar
                    .make(findViewById(android.R.id.content), "Invalid username or password",
                            Snackbar.LENGTH_SHORT)
                    .show();
        }
    }


    /**
     * Saves the user's session data after successful login.
     * This method:
     * 1. Clears any existing session data in SharedPreferences
     * 2. Saves the user ID and username for the current session
     * 3. Retrieves and restores any previously saved preferences for this user
     * from the database and adds them to SharedPreferences
     * 
     * @param userId The ID of the authenticated user to save in the session
     */
    public void onUserLoggedIn(String userId) {
        String username = editTextUsername.getText().toString().trim();

        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
        editor.clear().apply();
        editor.putString("userId", userId);
        editor.putString("last_username", username);
        editor.putBoolean("just_logged_in", true);
        editor.apply();

        // Query the user_diseases table directly to get the diseases for this user
        List<Disease> diseases = dbHelper.getDiseasesForPatient(userId);

        // For each disease, create a key in the format "Disease#icd10Code#diseaseName"
        // and store the disease ID in SharedPreferences
        for (Disease disease : diseases) {
            String key = "Disease#" + disease.getICD10() + "#" + disease.getName();
            editor.putString(key, String.valueOf(disease.getDiseaseId()));
        }

        editor.apply();
    }

    /**
     * Overrides the default finish method to add custom transition animations.
     * This method applies slide-in-left and slide-out-right animations when
     * the activity is closed, providing a consistent user experience with
     * the rest of the application.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
