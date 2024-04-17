package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView bDescription;// Acquire ImageView from the layout
    private String bInterpretation;
    private RecyclerView diseaseRecyclerView;
    private CarouselAdapter carouselAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        diseaseRecyclerView = findViewById(R.id.disease_recyclerview);
        dbHelper = new DatabaseHelper(this);
        //mBmiWidget = findViewById(R.id.bmi_widget);
        //bDescription = findViewById(R.id.bDescription);

        SharedPreferences sharedPreference = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String curr_user = sharedPreference.getString("userId", "");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_dashboard);
        bottomNav.setBackgroundColor(Color.WHITE);
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
        }


        ArrayList<Disease> diseasesList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String[] parts = entry.getKey().split("#");
            if (parts[0].equals("Disease")) {
                long diseaseId = Long.parseLong(preferences.getString(entry.getKey(), ""));
                Session curr_disease = dbHelper.getSession(diseaseId);
                String diseaseInterpretation = curr_disease.getValue();
                diseasesList.add(new Disease(parts[1], diseaseInterpretation));
            }
        }

        carouselAdapter = new CarouselAdapter(this, diseasesList);
        diseaseRecyclerView.setAdapter(carouselAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        diseaseRecyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        diseaseRecyclerView.addItemDecoration(new CarouselItemDecoration(spacingInPixels));

        ImageView leftArrow = findViewById(R.id.left_arrow);
        ImageView rightArrow = findViewById(R.id.right_arrow);

        diseaseRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition =
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                                .findFirstVisibleItemPosition();
                int lastVisiblePosition =
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                                .findLastVisibleItemPosition();

                leftArrow.setEnabled(firstVisiblePosition > 0);
                rightArrow.setEnabled(lastVisiblePosition < diseasesList.size() - 1);
            }
        });
    }
}

