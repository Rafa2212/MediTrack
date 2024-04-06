package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DashboardActivity extends AppCompatActivity {

    private TextView mBMIValueTextView;
    private TextView mBMIDescriptionTextView;
    private TextView mTreatmentDescriptionTextView;
    private TextView mMedicationDescriptionTextView;
    private RecyclerView mMedicationList;
    private String CURRENT_USER_ID;
    private DatabaseHelper dbHelper;
    private User currentUser;
    // Replace these with actual values
    private double userWeight = 60.0; // in kg
    private double userHeight = 1.75; // in m

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        CURRENT_USER_ID = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("userId", "default_value");
        dbHelper = DatabaseHelper.getInstance(this);

        currentUser = dbHelper.getUser(CURRENT_USER_ID);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_dashboard);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
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

        CardView bmiWidget = findViewById(R.id.imc_widget);
        mBMIValueTextView = bmiWidget.findViewById(R.id.bmi_value);
        mBMIDescriptionTextView = bmiWidget.findViewById(R.id.bmi_description);

        CardView treatmentWidget = findViewById(R.id.treatment_widget);
        mTreatmentDescriptionTextView = treatmentWidget.findViewById(R.id.treatment_description);

//        CardView medicationWidget = findViewById(R.id.medication_widget);
//        mMedicationDescriptionTextView = medicationWidget.findViewById(R.id.medication_description);
//        mMedicationList = medicationWidget.findViewById(R.id.medication_list);

        // Calculate BMI and display the value
        double bmi = calculateBMI(userWeight, userHeight);
        mBMIValueTextView.setText(String.valueOf(bmi));

        // Get AI prompts
        postToModel(String.valueOf(bmi), "bmi");
        postToModel(String.valueOf(bmi), "treatment");
        // postToModel(userDiseases, "medication"); // userDiseases should contain user's diseases data
    }

    public double calculateBMI(double weight, double height) {
        return weight / Math.pow(height, 2);
    }

    public void postToModel(String text, String widget) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String json = "{ \"text\": \"" + text + "\" }";
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("http://url-to-your-flask-app.com/classify") // replace with actual url
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String aiPrompt = jsonObject.getString("result"); // replace 'result' with actual key
                        runOnUiThread(() -> updateWidget(aiPrompt, widget));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateWidget(String aiPrompt, String widget) {
        switch (widget) {
            case "bmi":
                mBMIDescriptionTextView.setText(aiPrompt);
                break;
            case "treatment":
                mTreatmentDescriptionTextView.setText(aiPrompt);
                break;
            case "medication":
                // TODO
                break;
        }
    }
}