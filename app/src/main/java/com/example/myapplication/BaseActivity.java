package com.example.myapplication;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.transition.Transition;
import android.transition.TransitionInflater;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {
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
            } else if (itemId == R.id.menu_diseases) {
                intent = new Intent(this, AddDiseaseActivity.class);
            } else if (itemId == R.id.menu_profile) {
                intent = new Intent(this, ProfileSetupActivity.class);
            } else if (itemId == R.id.menu_help) {
                intent = new Intent(this, HelpActivity.class);
            } else if (itemId == R.id.menu_logout) {
                SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            if (intent != null) {
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(this, bottomNav, "bottomNavTransition");
                startActivity(intent, options.toBundle());
            }

            return true;
        });
    }
}