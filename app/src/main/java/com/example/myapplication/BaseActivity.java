package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Initializes the activity, sets up window transitions and animations.
     * This method is called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        setupWindowAnimations();
    }

    /**
     * Sets up window transition animations for the activity.
     * Configures enter, exit, shared element enter, and shared element exit transitions
     * with a duration of 300ms using the transition defined in R.transition.move.
     */
    protected void setupWindowAnimations() {
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

    /**
     * Called when the activity is finishing and should be closed.
     * Overrides the default transition animation with custom slide animations.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Starts an activity with custom slide transition animations.
     * This method provides a consistent transition experience across the app.
     * 
     * @param intent The intent containing the activity to be started and any additional data
     */
    public void startActivityWithTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Sets up the bottom navigation bar for the activity.
     * Configures the navigation menu based on user type (doctor or patient),
     * sets the selected item, and handles navigation item clicks.
     * 
     * @param bottomNav The BottomNavigationView to be configured
     * @param selectedItemId The resource ID of the menu item to be selected by default
     */
    protected void setupNavigation(BottomNavigationView bottomNav, @IdRes int selectedItemId) {
        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        boolean isDoctor = dbHelper.isDoctor(userId);

        if (!isDoctor) {
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.bottom_navigation_menu_patient);
        }

        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
            getWindow().setSharedElementExitTransition(fade);

            if (itemId == R.id.menu_dashboard) {
                intent = new Intent(this, PtDashboardActivity.class);
            } else if (itemId == R.id.menu_profile && isDoctor) {
                intent = new Intent(this, DrProfileActivity.class);
            } else if (itemId == R.id.menu_wkly_report) {
                intent = new Intent(this, PtFeedbackActivity.class);
            } else if (itemId == R.id.menu_logout) {
                SharedPreferences prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                String lastUsername = prefs.getString("last_username", "");
                prefs.edit().clear().apply();
                if (!lastUsername.isEmpty()) {
                    prefs.edit().putString("last_username", lastUsername).apply();
                }
                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            if (intent != null) {
                startActivityWithTransition(intent);
            }

            return true;
        });
    }

    /**
     * Sets up the bottom navigation bar specifically for doctor users.
     * Configures the navigation menu with doctor-specific options,
     * sets the selected item, and handles navigation item clicks.
     * 
     * @param bottomNav The BottomNavigationView to be configured for doctor users
     * @param selectedItemId The resource ID of the menu item to be selected by default
     */
    protected void setupDoctorNavigation(BottomNavigationView bottomNav, @IdRes int selectedItemId) {
        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
            getWindow().setSharedElementExitTransition(fade);

            if (itemId == R.id.menu_doctor_dashboard) {
                intent = new Intent(this, DrDashboardActivity.class);
            } else if (itemId == R.id.menu_patients) {
                intent = new Intent(this, DrPatientsActivity.class);
            } else if (itemId == R.id.menu_logout) {
                SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                String lastUsername = sharedPreferences.getString("last_username", "");
                sharedPreferences.edit().clear().apply();
                if (!lastUsername.isEmpty()) {
                    sharedPreferences.edit().putString("last_username", lastUsername).apply();
                }
                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            if (intent != null) {
                startActivityWithTransition(intent);
            }

            return true;
        });
    }
}
