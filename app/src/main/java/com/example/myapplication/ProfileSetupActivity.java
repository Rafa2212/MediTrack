package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileSetupActivity extends AppCompatActivity {

    private Button buttonSubmitProfile;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_profile);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_dashboard) {
                // start Dashboard activity
            } else if (itemId == R.id.menu_diseases) {
                startActivity(new Intent(this, AddDiseaseActivity.class));
            } else if (itemId == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileSetupActivity.class));
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

        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextAge = findViewById(R.id.editTextAge);
        EditText editTextHeight = findViewById(R.id.editTextHeight);
        EditText editTextWeight = findViewById(R.id.editTextWeight);
        buttonSubmitProfile = findViewById(R.id.buttonSubmitProfile);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        User user = dbHelper.getUser(userId);

        if (user != null && user.getUserProfile() != null) {
            UserProfile userProfile = user.getUserProfile();
            editTextName.setText(userProfile.getName());
            editTextAge.setText(String.valueOf(userProfile.getAge()));
            editTextHeight.setText(String.valueOf(userProfile.getHeight()));
            editTextWeight.setText(String.valueOf(userProfile.getWeight()));
        }

        buttonSubmitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = editTextName.getText().toString();
                String ageTxt = editTextAge.getText().toString();
                String heightTxt = editTextHeight.getText().toString();
                String weightTxt = editTextWeight.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageTxt)
                        || TextUtils.isEmpty(heightTxt) || TextUtils.isEmpty(weightTxt)) {
                    Toast.makeText(ProfileSetupActivity.this, "Please fill out all fields", Toast.LENGTH_LONG).show();
                } else {
                    int age = Integer.parseInt(ageTxt);
                    float height = Float.parseFloat(heightTxt);
                    float weight = Float.parseFloat(weightTxt);

                    UserProfile userProfile = new UserProfile(name, age, height, weight);
                    dbHelper.insertOrUpdateProfile(userId, userProfile);

                    Intent intent = new Intent(ProfileSetupActivity.this, AddDiseaseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

        if (user != null && user.getUserProfile() != null) {
            bottomNav.setVisibility(View.VISIBLE); // make bottom navigation visible if profile is complete
        } else {
            bottomNav.setVisibility(View.GONE);  // hide it if profile is not complete
        }
    }
}