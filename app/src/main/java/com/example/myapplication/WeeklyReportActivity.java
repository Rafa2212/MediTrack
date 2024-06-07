package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;
import com.theokanning.openai.threads.ThreadRequest;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeeklyReportActivity extends BaseActivity {
    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupNavigation(bottomNav, R.id.menu_wkly_report);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String curr_user = sharedPreferences.getString("userId", "");

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        User user = dbHelper.getUser(curr_user);

        AppCompatAutoCompleteTextView editTextFirstQ = findViewById(R.id.editTextFirstQ);
        AppCompatAutoCompleteTextView editTextBMIQ = findViewById(R.id.editTextBMIQ);

        String[] states = new String[]{"very poor", "poor", "ok", "good", "very good"};
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                states
        );
        editTextFirstQ.setAdapter(stateAdapter);
        editTextFirstQ.setThreshold(1);

        editTextBMIQ.setAdapter(stateAdapter);
        editTextBMIQ.setThreshold(1);

        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);

        Map<String, ?> allEntries = preferences.getAll();
        ArrayList<Disease> diseasesList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String[] parts = entry.getKey().split("#");
            if (parts[0].equals("Disease")) {
                long diseaseId = Long.parseLong(preferences.getString(entry.getKey(), ""));
                Session curr_disease = dbHelper.getSession(diseaseId);
                String diseaseInterpretation = curr_disease.getValue();
                diseasesList.add(new Disease(parts[1], parts[2], diseaseInterpretation));
            }
        }

        LinearLayout diseasesLinearLayout = findViewById(R.id.diseasesLinearLayout);
        for (Disease disease : diseasesList) {
            addDiseaseQuestion(diseasesLinearLayout, disease, states);
        }

        Button buttonSubmitReport = findViewById(R.id.buttonSubmitReport);

        buttonSubmitReport.setOnClickListener(v -> {
            String firstQ = editTextFirstQ.getText().toString();
            String BMIQ = editTextBMIQ.getText().toString();
            List<String> lstString = getDiseasesStringList(diseasesLinearLayout);
            boolean areAllFieldsCompleted = true;
            for (int i = 0; i < lstString.toArray().length; i++){
                    if (lstString.get(i).equals("")){
                        areAllFieldsCompleted = false;
                        break;
                    }
            }

            if (TextUtils.isEmpty(firstQ) || TextUtils.isEmpty(BMIQ) || !areAllFieldsCompleted) {
                Snackbar
                .make(findViewById(android.R.id.content), "Please fill out all fields",
                        Snackbar.LENGTH_SHORT)
                .show();
            } else {
                final Dialog dialog = new Dialog(WeeklyReportActivity.this);

                dialog.setContentView(R.layout.custom_dialog);

                dialog.findViewById(R.id.progress);
                TextView textView = dialog.findViewById(R.id.text);

                textView.setText("Connecting to Fitbit...");

                dialog.setCancelable(false);

                dialog.show();

                UserProfile userProfile = dbHelper.getUserProfile(curr_user);

                float heightInMeters = userProfile.getHeight() / 100;
                float BMI = userProfile.getWeight() / (heightInMeters * heightInMeters);

                boolean isOlder = isDateOlderThanAWeek(userProfile.getLastMedicalReport());

                if (true) {

                    FitbitAPI fb = new FitbitAPI(TokenData.FITBIT_TOKEN.getToken());
                    fb.updateUserProfile(userProfile);

                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    Handler handler = new Handler(Looper.getMainLooper());

                    OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                    executor.execute(() -> {
                        try {
                            Assistant assistant = service.retrieveAssistant(TokenData.ASSISTANT_ID.getToken());

                            Thread thread = service.createThread(new ThreadRequest());

                            String prepPrompt = "Consider that you will work at a medical report PDF document so please follow this structured format:";
                            prepPrompt += "Please take into consideration to include the patient's username, the current date for the report and the title: Weekly Report as headers/title.";
                            prepPrompt += "Now for the content please prepare yourself for some raw data got from a Fitbit API, prepare to interpret it the best for a user and doctor to understand his state";
                            prepPrompt += "For the footer I want the pages counted so please take that in mind, I will provide you in the next prompt the user profile details for the patient and also his data for the week";

                            MessageRequest messageRequest =
                                    MessageRequest.builder().role("user").content(prepPrompt).build();
                            service.createMessage(thread.getId(), messageRequest);
                            RunCreateRequest runCreateRequest =
                                    RunCreateRequest.builder().assistantId(assistant.getId()).build();
                            Run run = service.createRun(thread.getId(), runCreateRequest);
                            Run retrievedRun;
                            do {
                                retrievedRun = service.retrieveRun(thread.getId(), run.getId());
                            } while (!(retrievedRun.getStatus().equals("completed"))
                                    && !(retrievedRun.getStatus().equals("failed")));

                            String prompt = generatePrompt(userProfile, BMI, firstQ, BMIQ, lstString);

                            messageRequest =
                                    MessageRequest.builder().role("user").content(prompt).build();
                            service.createMessage(thread.getId(), messageRequest);
                            runCreateRequest =
                                    RunCreateRequest.builder().assistantId(assistant.getId()).build();
                            run = service.createRun(thread.getId(), runCreateRequest);
                            do {
                                retrievedRun = service.retrieveRun(thread.getId(), run.getId());
                            } while (!(retrievedRun.getStatus().equals("completed"))
                                    && !(retrievedRun.getStatus().equals("failed")));
                            OpenAiResponse<Message> response = service.listMessages(thread.getId());
                            Message respMsg = service.retrieveMessage(thread.getId(), response.getFirstId());
                            String weeklyReportResponse =
                                    respMsg.getContent().get(0).getText().getValue();

                            PDFGeneration pdfGeneration = new PDFGeneration(getApplicationContext());
                            File pdfFile = pdfGeneration.createPDF(weeklyReportResponse);
                            pdfGeneration.openPDF(pdfFile);

                            userProfile.setLastMedicalReport(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                            dbHelper.insertOrUpdateProfile(curr_user, userProfile);

                            dialog.dismiss();

                        } catch (Exception e) {
                            dialog.dismiss();
                            handler.post(()
                                    -> Snackbar
                                    .make(findViewById(android.R.id.content),
                                            "Saving failed! Try again later!", Snackbar.LENGTH_SHORT)
                                    .show());
                        }
                    });
                } else {
                    dialog.dismiss();
                    Snackbar
                    .make(findViewById(android.R.id.content), "This user already has a report for this week!",
                            Snackbar.LENGTH_SHORT)
                    .show();
                }
            }
        });

        if (user != null && user.getUserProfile() != null) {
            bottomNav.setVisibility(View.VISIBLE);
        } else {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private List<String> getDiseasesStringList(LinearLayout parentLayout) {

        List<String> lstString = new ArrayList<>();

        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);

            if (child instanceof TextInputLayout) {
                TextInputLayout textInputLayout = (TextInputLayout) child;
                AppCompatAutoCompleteTextView autoCompleteTextView = (AppCompatAutoCompleteTextView) textInputLayout.getEditText();

                if (autoCompleteTextView != null) {
                    String input = autoCompleteTextView.getText().toString();
                    lstString.add(input);
                }
            }
        }
        return lstString;
    }

    public String generatePrompt(UserProfile userProfile, double BMI, String firstQ, String BMIQ, List<String> diseasesStates) {
        String prompt = String.format(
                "Knowing that the current date is: %s, and the patient: %s, having %d year old having %.2f cm height and %.2f kg weight with a BMI of %.2f. " +
                        "Average steps per day: %d, sedentary minutes per day: %d, resting heart rate: %.2f bpm; " +
                        "Average breathing rate: %.2f breaths per minute. " +
                        "Sleep data: %d minutes after wakeup, %d minutes awake, %d minutes to fall asleep, %d restless events, %d minutes restless, %d minutes in bed. " +
                        "Sleep stages: %d minutes deep, %d minutes light, %d minutes REM, %d minutes wake. " +
                        "Active zone minutes per week: %d. " +
                        "Heart rate variability: average daily RMSSD: %.2f ms, average deep RMSSD: %.2f ms. " +
                        "Cardio fitness score (VO2 max): %s. " +
                        "Patient questions: First question response: %s, BMI question response: %s. " +
                        "Current conditions based on ICD10 codes: %s. " +
                        "Based on the above data, provide a medical report, including potential insights and recommendations for the next week, or possible doctor recommendations if bad states reoccur.",
                LocalDateTime.now().toString(),
                userProfile.getName(),
                userProfile.getAge(),
                userProfile.getHeight(),
                userProfile.getWeight(),
                BMI,
                userProfile.getAverageSteps(),
                userProfile.getAverageSedentaryMinutes(),
                userProfile.getAverageBreathingRate(),
                userProfile.getAverageBreathingRate(),
                userProfile.getMinutesAfterWakeup(),
                userProfile.getMinutesAwake(),
                userProfile.getMinutesToFallAsleep(),
                userProfile.getRestlessCount(),
                userProfile.getRestlessDuration(),
                userProfile.getTimeInBed(),
                userProfile.getDeepSleep(),
                userProfile.getLightSleep(),
                userProfile.getRemSleep(),
                userProfile.getWakeSleep(),
                userProfile.getAverageActiveZoneMinutes(),
                userProfile.getAverageDailyRmssd(),
                userProfile.getAverageDeepRmssd(),
                userProfile.getVo2Max(),
                firstQ,
                BMIQ,
                String.join(", ", diseasesStates)
        );

        return prompt;
    }

    private boolean isDateOlderThanAWeek(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return true;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // Adjust pattern as needed
        LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return date.isBefore(oneWeekAgo);
    }

    private void addDiseaseQuestion(LinearLayout parentLayout, Disease disease, String[] states) {
        TextInputLayout textInputLayout = new TextInputLayout(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(24, 24, 24, 0);
        textInputLayout.setLayoutParams(layoutParams);

        textInputLayout.setHint(" (" + disease.getICD10() + ") state?");

        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxStrokeWidth(1);
        textInputLayout.setBoxStrokeWidthFocused(2);

        textInputLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.logoColorRed)));

        androidx.appcompat.widget.AppCompatAutoCompleteTextView autoCompleteTextView = new androidx.appcompat.widget.AppCompatAutoCompleteTextView(this);
        autoCompleteTextView.setId(View.generateViewId());
        autoCompleteTextView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                states
        );
        autoCompleteTextView.setAdapter(stateAdapter);
        autoCompleteTextView.setThreshold(1);

        textInputLayout.addView(autoCompleteTextView);
        parentLayout.addView(textInputLayout);
    }
}