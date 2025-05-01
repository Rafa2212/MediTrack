package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class DashboardActivity extends BaseActivity {
    private String bInterpretation;
    private String metabolicInterpretation;

    @SuppressLint({"SetTextI18p", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        RecyclerView diseaseRecyclerView = findViewById(R.id.disease_recyclerview);
        RecyclerView healthWidgetsRecyclerView = findViewById(R.id.health_widgets_recyclerview);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupNavigation(bottomNav, R.id.menu_dashboard);

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();

        // Find most recent BMI interpretation
        LocalDateTime mostRecentBMI = LocalDateTime.MIN;
        String mostRecentBMIKey = "";
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                String key = entry.getKey();
                if (key == null) continue;

                String[] parts = key.split("#");
                if (parts.length >= 3 && parts[0].contains("BMI")) {
                    LocalDateTime currentDateTime = LocalDateTime.parse(parts[2]);
                    if (currentDateTime.isAfter(mostRecentBMI)) {
                        mostRecentBMI = currentDateTime;
                        mostRecentBMIKey = key;
                    }
                }
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error processing BMI entry", e);
            }
        }

        // Find most recent Metabolic interpretation
        LocalDateTime mostRecentMetabolic = LocalDateTime.MIN;
        String mostRecentMetabolicKey = "";
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                String key = entry.getKey();
                if (key == null) continue;

                String[] parts = key.split("#");
                if (parts.length >= 3 && parts[0].contains("Metabolic")) {
                    LocalDateTime currentDateTime = LocalDateTime.parse(parts[2]);
                    if (currentDateTime.isAfter(mostRecentMetabolic)) {
                        mostRecentMetabolic = currentDateTime;
                        mostRecentMetabolicKey = key;
                    }
                }
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error processing Metabolic entry", e);
            }
        }

        ArrayList<Disease> diseasesList = new ArrayList<>();
        ArrayList<HealthWidget> healthWidgetsList = new ArrayList<>();
        
        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            // Get BMI interpretation
            if (!mostRecentBMIKey.isEmpty()) {
                try {
                    long bmiId = Long.parseLong(preferences.getString(mostRecentBMIKey, ""));
                    Session curr_bmi = dbHelper.getSession(bmiId);
                    if (curr_bmi != null) {
                        bInterpretation = curr_bmi.getValue();
                    } else {
                        bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error getting BMI interpretation", e);
                    bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
                }
            } else {
                bInterpretation = "No BMI interpretation available yet. Please update your profile to generate one.";
            }

            // Get Metabolic interpretation
            if (!mostRecentMetabolicKey.isEmpty()) {
                try {
                    long metabolicId = Long.parseLong(preferences.getString(mostRecentMetabolicKey, ""));
                    Session curr_metabolic = dbHelper.getSession(metabolicId);
                    if (curr_metabolic != null) {
                        metabolicInterpretation = curr_metabolic.getValue();
                    } else {
                        metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error getting Metabolic interpretation", e);
                    metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
                }
            } else {
                metabolicInterpretation = "No Metabolic Balance interpretation available yet. Please update your profile with additional health metrics to generate one.";
            }

            // Add health widgets to the list
            healthWidgetsList.add(new HealthWidget(
                    "BMI",
                    getString(R.string.bmi_title),
                    getString(R.string.bmi_description),
                    R.drawable.background_bmi,
                    bInterpretation
            ));
            
            healthWidgetsList.add(new HealthWidget(
                    "Metabolic",
                    getString(R.string.metabolic_title),
                    getString(R.string.metabolic_description),
                    R.drawable.background_bmi,
                    metabolicInterpretation
            ));

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                try {
                    String key = entry.getKey();
                    if (key == null) continue;

                    String[] parts = key.split("#");
                    if (parts.length >= 3 && parts[0].equals("Disease")) {
                        long diseaseId = Long.parseLong(preferences.getString(key, ""));
                        Session curr_disease = dbHelper.getSession(diseaseId);
                        if (curr_disease != null) {
                            String diseaseInterpretation = curr_disease.getValue();
                            if (diseaseInterpretation != null) {
                                diseasesList.add(new Disease(parts[1], parts[2], diseaseInterpretation));
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error processing disease entry", e);
                }
            }
        } catch (Exception e) {
            Log.e("ErrorTag", "DatabaseHelper instantiation failed", e);
        }

        // Set up health widgets RecyclerView
        HealthWidgetAdapter healthWidgetAdapter = new HealthWidgetAdapter(healthWidgetsList);
        healthWidgetsRecyclerView.setAdapter(healthWidgetAdapter);
        LinearLayoutManager healthLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        healthWidgetsRecyclerView.setLayoutManager(healthLayoutManager);
        int healthSpacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        healthWidgetsRecyclerView.addItemDecoration(
                new HealthWidgetAdapter.HealthWidgetItemDecoration(healthSpacingInPixels));

        // Set up disease RecyclerView
        CarouselAdapter carouselAdapter = new CarouselAdapter(diseasesList);

        if (diseasesList.isEmpty()) {
            diseaseRecyclerView.setVisibility(View.GONE);
            TextView interpretationText = findViewById(R.id.disease_subtext);
            interpretationText.setVisibility(View.GONE);
            ImageView interpretationsTitle = findViewById(R.id.image_interpretation);
            interpretationsTitle.setVisibility(View.GONE);
        } else {
            diseaseRecyclerView.setAdapter(carouselAdapter);
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            diseaseRecyclerView.setLayoutManager(layoutManager);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
            diseaseRecyclerView.addItemDecoration(
                    new CarouselAdapter.CarouselItemDecoration(spacingInPixels));
            diseaseRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }
    }
}