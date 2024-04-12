package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView bDescription;
    private String bInterpretation;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        //mBmiWidget = findViewById(R.id.bmi_widget);
        //bDescription = findViewById(R.id.bDescription);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_dashboard);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_dashboard) {
                Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
                getWindow().setSharedElementExitTransition(fade);

                Intent intent = new Intent(this, DashboardActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(this, bottomNav, "bottomNavTransition");
                startActivity(intent, options.toBundle());
            } else if (itemId == R.id.menu_diseases) {
                Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
                getWindow().setSharedElementExitTransition(fade);

                Intent intent = new Intent(this, AddDiseaseActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(this, bottomNav, "bottomNavTransition");
                startActivity(intent, options.toBundle());
            } else if (itemId == R.id.menu_profile) {
                Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
                getWindow().setSharedElementExitTransition(fade);

                Intent intent = new Intent(this, ProfileSetupActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(this, bottomNav, "bottomNavTransition");
                startActivity(intent, options.toBundle());
            } else if(itemId == R.id.menu_help) {
                // start Help activity
            } else if (itemId == R.id.menu_logout) {
                SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            return true;
        });

        MaterialCardView bmiCard = findViewById(R.id.BMI_widget);

        bmiCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                    ScrollView scrollView = new ScrollView(v.getContext());
                    TextView textView = new TextView(v.getContext());
                    //TextView bmiDescriptionTextView = v.findViewById(R.id.bDescription);

                    textView.setPadding(32, 32, 32, 32);
                    textView.setText(bInterpretation);

                    scrollView.addView(textView);
                    builder.setView(scrollView)
                            .setTitle("BMI Interpretation")
                            .setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
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

        if (!mostRecentKey.isEmpty()){
            long bmiId = Long.parseLong(preferences.getString(mostRecentKey, ""));
            Session curr_bmi = dbHelper.getSession(bmiId);
            String bmiInterpretation = curr_bmi.getValue();
            bInterpretation = bmiInterpretation;
            //bDescription.setText("Your most recent BMI is " + bmiInterpretation + " on " + mostRecentKey.split("#")[2]);
//            mBmiWidget.setCardTitle("BMI");
//            mBmiWidget.setCardContent(bmiInterpretation);
        }
    }
}