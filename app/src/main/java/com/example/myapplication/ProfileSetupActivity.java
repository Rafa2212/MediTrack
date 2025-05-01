package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileSetupActivity extends BaseActivity {
    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupNavigation(bottomNav, R.id.menu_profile);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String curr_user = sharedPreferences.getString("userId", "");

        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextAge = findViewById(R.id.editTextAge);
        EditText editTextHeight = findViewById(R.id.editTextHeight);
        EditText editTextWeight = findViewById(R.id.editTextWeight);
        EditText editTextBodyFat = findViewById(R.id.editTextBodyFat);
        EditText editTextBPSystolic = findViewById(R.id.editTextBPSystolic);
        EditText editTextBPDiastolic = findViewById(R.id.editTextBPDiastolic);
        EditText editTextHR = findViewById(R.id.editTextHR);
        EditText editTextBloodGlucose = findViewById(R.id.editTextBloodGlucose);
        EditText editTextCholesterolTotal = findViewById(R.id.editTextCholesterolTotal);
        EditText editTextCholesterolHDL = findViewById(R.id.editTextCholesterolHDL);
        EditText editTextCholesterolLDL = findViewById(R.id.editTextCholesterolLDL);
        Button buttonSubmitProfile = findViewById(R.id.buttonSubmitProfile);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        User user = dbHelper.getUser(curr_user);

        if (user != null && user.getUserProfile() != null) {
            UserProfile userProfile = user.getUserProfile();
            editTextName.setText(userProfile.getName());
            editTextAge.setText(String.valueOf(userProfile.getAge()));
            editTextHeight.setText(String.valueOf(userProfile.getHeight()));
            editTextWeight.setText(String.valueOf(userProfile.getWeight()));

            // Set values for additional health metrics if they exist
            if (userProfile.getBodyFatPercentage() > 0) {
                editTextBodyFat.setText(String.valueOf(userProfile.getBodyFatPercentage()));
            }
            if (userProfile.getBloodPressureSystolic() > 0) {
                editTextBPSystolic.setText(String.valueOf(userProfile.getBloodPressureSystolic()));
            }
            if (userProfile.getBloodPressureDiastolic() > 0) {
                editTextBPDiastolic.setText(String.valueOf(userProfile.getBloodPressureDiastolic()));
            }
            if (userProfile.getRestingHeartRate() > 0) {
                editTextHR.setText(String.valueOf(userProfile.getRestingHeartRate()));
            }
            if (userProfile.getBloodGlucose() > 0) {
                editTextBloodGlucose.setText(String.valueOf(userProfile.getBloodGlucose()));
            }
            if (userProfile.getCholesterolTotal() > 0) {
                editTextCholesterolTotal.setText(String.valueOf(userProfile.getCholesterolTotal()));
            }
            if (userProfile.getCholesterolHDL() > 0) {
                editTextCholesterolHDL.setText(String.valueOf(userProfile.getCholesterolHDL()));
            }
            if (userProfile.getCholesterolLDL() > 0) {
                editTextCholesterolLDL.setText(String.valueOf(userProfile.getCholesterolLDL()));
            }
        }

        buttonSubmitProfile.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String ageTxt = editTextAge.getText().toString();
            String heightTxt = editTextHeight.getText().toString();
            String weightTxt = editTextWeight.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageTxt) || TextUtils.isEmpty(heightTxt)
                    || TextUtils.isEmpty(weightTxt)) {
                Snackbar
                        .make(findViewById(android.R.id.content), "Please fill out all required fields!",
                                Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                final Dialog dialog = new Dialog(ProfileSetupActivity.this);

                dialog.setContentView(R.layout.custom_dialog);

                dialog.findViewById(R.id.progress);
                TextView textView = dialog.findViewById(R.id.text);

                textView.setText("Saving profile...");

                dialog.setCancelable(false);

                dialog.show();

                int age = Integer.parseInt(ageTxt);
                float height = Float.parseFloat(heightTxt);
                float weight = Float.parseFloat(weightTxt);

                UserProfile userProfile = new UserProfile(name, age, height, weight, "");

                // Get values from additional health metrics fields
                String bodyFatTxt = editTextBodyFat.getText().toString();
                String bpSystolicTxt = editTextBPSystolic.getText().toString();
                String bpDiastolicTxt = editTextBPDiastolic.getText().toString();
                String hrTxt = editTextHR.getText().toString();
                String bloodGlucoseTxt = editTextBloodGlucose.getText().toString();
                String cholesterolTotalTxt = editTextCholesterolTotal.getText().toString();
                String cholesterolHDLTxt = editTextCholesterolHDL.getText().toString();
                String cholesterolLDLTxt = editTextCholesterolLDL.getText().toString();

                // Set additional health metrics if provided
                if (!bodyFatTxt.isEmpty()) {
                    userProfile.setBodyFatPercentage(Float.parseFloat(bodyFatTxt));
                }
                if (!bpSystolicTxt.isEmpty()) {
                    userProfile.setBloodPressureSystolic(Integer.parseInt(bpSystolicTxt));
                }
                if (!bpDiastolicTxt.isEmpty()) {
                    userProfile.setBloodPressureDiastolic(Integer.parseInt(bpDiastolicTxt));
                }
                if (!hrTxt.isEmpty()) {
                    userProfile.setRestingHeartRate(Integer.parseInt(hrTxt));
                }
                if (!bloodGlucoseTxt.isEmpty()) {
                    userProfile.setBloodGlucose(Float.parseFloat(bloodGlucoseTxt));
                }
                if (!cholesterolTotalTxt.isEmpty()) {
                    userProfile.setCholesterolTotal(Float.parseFloat(cholesterolTotalTxt));
                }
                if (!cholesterolHDLTxt.isEmpty()) {
                    userProfile.setCholesterolHDL(Float.parseFloat(cholesterolHDLTxt));
                }
                if (!cholesterolLDLTxt.isEmpty()) {
                    userProfile.setCholesterolLDL(Float.parseFloat(cholesterolLDLTxt));
                }

                dbHelper.insertOrUpdateProfile(curr_user, userProfile);

                float heightInMeters = height / 100;
                float BMI = weight / (heightInMeters * heightInMeters);

                // Always query OpenAI for updated interpretations
                SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);

                ExecutorService executor = Executors.newSingleThreadExecutor();

                Handler handler = new Handler(Looper.getMainLooper());

                OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                executor.execute(() -> {
                    try {

                        // Generate BMI interpretation
                        String bmiPrompt = userProfile.getAge() + " year old having " + userProfile.getHeight()
                                + " cm "
                                + " and " + userProfile.getWeight() + " kg and " + BMI
                                + "BMI. Provide a detailed analysis of this BMI value, including health implications, potential risks, and personalized recommendations based on the person's age and measurements. Include specific advice for diet and exercise if appropriate.";

                        ChatCompletionRequest bmiRequest = ChatCompletionRequest.builder()
                                .model("gpt-3.5-turbo")
                                .messages(Arrays.asList(
                                        new ChatMessage("user", bmiPrompt)
                                ))
                                .build();

                        ChatCompletionResult bmiResult = service.createChatCompletion(bmiRequest);

                        String bmiResponse = bmiResult.getChoices().get(0).getMessage().getContent()
                                .replace('*', ' ')
                                .replace('#', ' ');
                        String bmiKey = "BMI#" + BMI + "#" + LocalDateTime.now();
                        long bmiId = dbHelper.insertOnSession(curr_user, bmiKey, bmiResponse);

                        SharedPreferences.Editor editor =
                                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                        editor.putString(bmiKey, String.valueOf(bmiId));

                        StringBuilder metabolicPrompt = new StringBuilder();
                        metabolicPrompt.append(userProfile.getAge() + " year old having " + userProfile.getHeight() + " cm and " + userProfile.getWeight() + " kg");

                        if (userProfile.getBodyFatPercentage() > 0) {
                            metabolicPrompt.append(" with body fat percentage of " + userProfile.getBodyFatPercentage() + "%");
                        }

                        if (userProfile.getBloodPressureSystolic() > 0 && userProfile.getBloodPressureDiastolic() > 0) {
                            metabolicPrompt.append(", blood pressure " + userProfile.getBloodPressureSystolic() + "/" + userProfile.getBloodPressureDiastolic() + " mmHg");
                        }

                        if (userProfile.getRestingHeartRate() > 0) {
                            metabolicPrompt.append(", resting heart rate " + userProfile.getRestingHeartRate() + " bpm");
                        }

                        if (userProfile.getBloodGlucose() > 0) {
                            metabolicPrompt.append(", blood glucose " + userProfile.getBloodGlucose() + " mg/dL");
                        }

                        if (userProfile.getCholesterolTotal() > 0) {
                            metabolicPrompt.append(", total cholesterol " + userProfile.getCholesterolTotal() + " mg/dL");

                            if (userProfile.getCholesterolHDL() > 0) {
                                metabolicPrompt.append(", HDL cholesterol " + userProfile.getCholesterolHDL() + " mg/dL");
                            }

                            if (userProfile.getCholesterolLDL() > 0) {
                                metabolicPrompt.append(", LDL cholesterol " + userProfile.getCholesterolLDL() + " mg/dL");
                            }
                        }

                        metabolicPrompt.append("\nPlease provide a detailed analysis of metabolic health based on these values, including:");
                        metabolicPrompt.append("\n1. Overall metabolic health assessment");
                        metabolicPrompt.append("\n2. Potential metabolic risks or concerns");
                        metabolicPrompt.append("\n3. Specific recommendations for improving metabolic health");
                        metabolicPrompt.append("\n4. Lifestyle changes that would benefit this individual");
                        metabolicPrompt.append("\n5. A health score out of 100 that represents how well this patient compares to the average patient of similar age, height, and weight. Format this as 'HEALTH_SCORE: X' where X is a number between 0 and 100.");

                        ChatCompletionRequest metabolicRequest = ChatCompletionRequest.builder()
                                .model("gpt-3.5-turbo")
                                .messages(Arrays.asList(
                                        new ChatMessage("user", metabolicPrompt.toString())
                                ))
                                .build();

                        ChatCompletionResult metabolicResult = service.createChatCompletion(metabolicRequest);

                        String metabolicResponse = metabolicResult.getChoices().get(0).getMessage().getContent()
                                .replace('*', ' ')
                                .replace('#', ' ');
                        String metabolicKey = "Metabolic#Balance#" + LocalDateTime.now();
                        long metabolicId = dbHelper.insertOnSession(curr_user, metabolicKey, metabolicResponse);

                        editor.putString(metabolicKey, String.valueOf(metabolicId));

                        // Extract health score from the response
                        int healthScore = 0;
                        String[] lines = metabolicResponse.split("\n");
                        for (String line : lines) {
                            if (line.contains("HEALTH_SCORE")) {
                                try {
                                    String scoreStr = line.substring(line.indexOf(":") + 1).trim();
                                    // Extract just the number
                                    scoreStr = scoreStr.replaceAll("[^0-9]", "");
                                    healthScore = Integer.parseInt(scoreStr);
                                    // Ensure score is between 0 and 100
                                    healthScore = Math.max(0, Math.min(100, healthScore));
                                    break;
                                } catch (Exception e) {
                                    Log.e("ProfileSetupActivity", "Error parsing health score: " + e.getMessage());
                                }
                            }
                        }

                        // Set the health score in the user profile
                        userProfile.setHealthScore(healthScore);
                        // Save the updated profile with the health score
                        dbHelper.insertOrUpdateProfile(curr_user, userProfile);
                        editor.apply();

                        dialog.dismiss();
                        Intent intent = new Intent(ProfileSetupActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } catch (Exception e) {
                        handler.post(()
                                -> Snackbar
                                .make(findViewById(android.R.id.content),
                                        "Saving failed! Try again later!", Snackbar.LENGTH_SHORT)
                                .show());
                    }
                });
            }
        });

        if (user != null && user.getUserProfile() != null) {
            bottomNav.setVisibility(View.VISIBLE);
        } else {
            bottomNav.setVisibility(View.GONE);
        }
    }
}
