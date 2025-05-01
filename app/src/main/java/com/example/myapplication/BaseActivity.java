package com.example.myapplication;

import android.app.ActivityOptions;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Enable window content transitions
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);

        // Set default transition animations
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // Apply smooth window transitions
        setupWindowAnimations();
    }

    /**
     * Set up window transition animations
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

    @Override
    public void finish() {
        super.finish();
        // Apply custom exit animation
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Start an activity with custom transitions
     * @param intent The intent to start the activity
     */
    public void startActivityWithTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Start an activity with custom transitions and finish the current activity
     * @param intent The intent to start the activity
     */
    public void startActivityWithTransitionAndFinish(Intent intent) {
        startActivity(intent);
        finish();
    }

    /**
     * Start an activity with fade transitions
     * @param intent The intent to start the activity
     */
    public void startActivityWithFadeTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Start an activity with fade transitions and finish the current activity
     * @param intent The intent to start the activity
     */
    public void startActivityWithFadeTransitionAndFinish(Intent intent) {
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Start an activity with zoom transitions
     * @param intent The intent to start the activity
     */
    public void startActivityWithZoomTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    /**
     * Start an activity with zoom transitions and finish the current activity
     * @param intent The intent to start the activity
     */
    public void startActivityWithZoomTransitionAndFinish(Intent intent) {
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }
    protected void setupNavigation(BottomNavigationView bottomNav, @IdRes int selectedItemId) {
        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
            getWindow().setSharedElementExitTransition(fade);

            if (itemId == R.id.menu_dashboard) {
                intent = new Intent(this, DashboardActivity.class);
            } else if (itemId == R.id.menu_profile) {
                intent = new Intent(this, ProfileSetupActivity.class);
            } else if (itemId == R.id.menu_wkly_report) {
                intent = new Intent(this, WeeklyReportActivity.class);
            }
            else if (itemId == R.id.menu_logout) {
                SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            if (intent != null) {
                startActivityWithTransition(intent);
            }

            return true;
        });
    }

    protected void setupDoctorNavigation(BottomNavigationView bottomNav, @IdRes int selectedItemId) {
        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
            getWindow().setSharedElementExitTransition(fade);

            if (itemId == R.id.menu_doctor_dashboard) {
                intent = new Intent(this, DoctorDashboardActivity.class);
            } else if (itemId == R.id.menu_patients) {
                intent = new Intent(this, DoctorPatientsActivity.class);
            } else if (itemId == R.id.menu_logout) {
                SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
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
