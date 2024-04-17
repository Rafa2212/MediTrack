package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class DashboardActivity extends BaseActivity {
    private String bInterpretation;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        RecyclerView diseaseRecyclerView = findViewById(R.id.disease_recyclerview);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupNavigation(bottomNav, R.id.menu_dashboard);

        MaterialCardView bmiCard = findViewById(R.id.BMI_widget);

        bmiCard.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                ScrollView scrollView = new ScrollView(v.getContext());
                TextView textView = new TextView(v.getContext());

                textView.setPadding(32, 32, 32, 32);
                textView.setText(bInterpretation);

                scrollView.addView(textView);
                builder.setView(scrollView)
                        .setTitle("BMI Interpretation")
                        .setNegativeButton("CLOSE", (dialog, id) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;
        });

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();
        LocalDateTime mostRecent = LocalDateTime.MIN;
        String mostRecentKey = "";
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String[] parts = entry.getKey().split("#");
            if (parts[0].contains("BMI")) {
                LocalDateTime currentDateTime = LocalDateTime.parse(parts[2]);
                if (currentDateTime.isAfter(mostRecent)) {
                    mostRecent = currentDateTime;
                    mostRecentKey = entry.getKey();
                }
            }
        }

        ArrayList<Disease> diseasesList = new ArrayList<>();
        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            if (!mostRecentKey.isEmpty()) {
                long bmiId = Long.parseLong(preferences.getString(mostRecentKey, ""));
                Session curr_bmi = dbHelper.getSession(bmiId);
                bInterpretation = curr_bmi.getValue();
            }

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String[] parts = entry.getKey().split("#");
                if (parts[0].equals("Disease")) {
                    long diseaseId = Long.parseLong(preferences.getString(entry.getKey(), ""));
                    Session curr_disease = dbHelper.getSession(diseaseId);
                    String diseaseInterpretation = curr_disease.getValue();
                    diseasesList.add(new Disease(parts[1], parts[2], diseaseInterpretation));
                }
            }
        } catch (Exception e) {
            Log.e("ErrorTag", "DatabaseHelper instantiation failed", e);
        }

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
