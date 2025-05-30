package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import android.app.ActivityOptions;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable window content transitions
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        if (!userId.isEmpty()) {
            // User is already logged in, get user details
            dbHelper = DatabaseHelper.getInstance(this);
            User user = dbHelper.getUser(userId);

            if (user != null) {
                // Valid user found, redirect to appropriate dashboard
                Intent intent;

                if (user.getUserProfile() == null) {
                    // User needs to set up profile
                    intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else if ("doctor".equals(user.getRole())) {
                    // Doctor dashboard
                    intent = new Intent(this, DoctorDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    // Patient dashboard
                    intent = new Intent(this, DashboardActivity.class);

                    // Start the dashboard activity
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                    finish();
                }
                return;
            }
        }

        // If no valid session or user not found, show login screen
        setContentView(R.layout.activity_login);

        // Set default transition animations
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // Apply smooth window transitions
        setupWindowAnimations();

        dbHelper = DatabaseHelper.getInstance(this);

        // Add a doctor user for testing
        AddDoctorTest.addDoctorForTesting(this);

        // Ensure the 'medic' user exists
        AddMedicUser.addMedicUser(this);

        // Create a profile for rafael with realistic health data
        SimulateRafaelProfile.createRafaelProfile(this);

        // Simulate that patient8's weekly feedback is available to complete
        //SimulatePatient8Feedback.simulatePatient8Feedback(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // Retrieve and display the last username
        String lastUsername = preferences.getString("last_username", "");
        if (!lastUsername.isEmpty()) {
            editTextUsername.setText(lastUsername);
        }

        buttonLogin.setOnClickListener(v -> login());
    }

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

            if (user.getUserProfile() == null) {
                Intent intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Intent intent;
                if ("doctor".equals(user.getRole())) {
                    intent = new Intent(this, DoctorDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    // Patient dashboard
                    intent = new Intent(this, DashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }

            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            Snackbar
                    .make(findViewById(android.R.id.content), "Invalid username or password",
                            Snackbar.LENGTH_SHORT)
                    .show();
        }
    }


    public void onUserLoggedIn(String userId) {
        // Save the username to SharedPreferences
        String username = editTextUsername.getText().toString().trim();

        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
        editor.clear().apply();
        editor.putString("userId", userId);
        editor.putString("last_username", username);
        editor.apply();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SHAREDPREF,
                new String[] {DatabaseHelper.COLUMN_SHAREDPREF_ID},
                DatabaseHelper.COLUMN_SHAREDPREF_USER_ID + "=?", new String[] {userId}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                long idFromDB = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_SHAREDPREF_ID));

                Session session = dbHelper.getSession(idFromDB);
                if (session != null) {
                    String keyFromDB = session.getKeyString();

                    editor.putString(keyFromDB, String.valueOf(idFromDB));
                    editor.apply();
                }
            }
            cursor.close();
        }
    }


    /**
     * Set up window transition animations
     */
    private void setupWindowAnimations() {
        Transition enterTransition = TransitionInflater.from(this).inflateTransition(R.transition.move);
        enterTransition.setDuration(300);
        getWindow().setEnterTransition(enterTransition);

        Transition exitTransition = TransitionInflater.from(this).inflateTransition(R.transition.move);
        exitTransition.setDuration(300);
        getWindow().setExitTransition(exitTransition);

        Transition sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(R.transition.move);
        sharedElementEnterTransition.setDuration(300);
        getWindow().setSharedElementEnterTransition(sharedElementEnterTransition);

        Transition sharedElementExitTransition = TransitionInflater.from(this).inflateTransition(R.transition.move);
        sharedElementExitTransition.setDuration(300);
        getWindow().setSharedElementExitTransition(sharedElementExitTransition);
    }

    @Override
    public void finish() {
        super.finish();
        // Apply custom exit animation
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
