package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.FileProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        Button buttonViewLastReport = findViewById(R.id.buttonViewLastReport);
        TextView textViewTimer = findViewById(R.id.textViewTimer);

        // Check if user can submit a report
        if (user != null && user.getUserProfile() != null) {
            UserProfile userProfile = user.getUserProfile();
            boolean canSubmitReport = isDateOlderThanAWeek(userProfile.getLastMedicalReport());

            // Check if there's a last report available
            boolean hasLastReport = userProfile.getLastMedicalReport() != null && !userProfile.getLastMedicalReport().isEmpty();

            if (hasLastReport) {
                // Check if there's a PDF file for this user
                File reportsDir = new File(getFilesDir(), "reports");
                if (reportsDir.exists() && reportsDir.isDirectory()) {
                    File[] files = reportsDir.listFiles((dir, name) -> name.startsWith("report_" + curr_user) && name.endsWith(".pdf"));
                    if (files != null && files.length > 0) {
                        // Sort files by last modified date to get the most recent one
                        java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                        // Show the button to view the last report
                        buttonViewLastReport.setVisibility(View.VISIBLE);

                        // Set up the button click listener
                        buttonViewLastReport.setOnClickListener(viewReportBtn -> {
                            try {
                                // Get the latest report from the database
                                MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(curr_user);

                                if (latestReport != null && !latestReport.getReportPath().isEmpty()) {
                                    // Open the PDF file using the path from the database
                                    File file = new File(latestReport.getReportPath());
                                    if (file.exists()) {
                                        Uri uri = FileProvider.getUriForFile(WeeklyReportActivity.this,
                                                getPackageName() + ".provider", file);

                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setDataAndType(uri, "application/pdf");
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(WeeklyReportActivity.this, "PDF file not found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(WeeklyReportActivity.this, "No report available", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("WeeklyReportActivity", "Error opening PDF file", e);
                                Toast.makeText(WeeklyReportActivity.this, "Error opening PDF file", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        buttonViewLastReport.setVisibility(View.GONE);
                    }
                } else {
                    buttonViewLastReport.setVisibility(View.GONE);
                }
            } else {
                buttonViewLastReport.setVisibility(View.GONE);
            }

            if (!canSubmitReport) {
                // Calculate time remaining until next report
                String timeRemaining = calculateTimeRemaining(userProfile.getLastMedicalReport());
                textViewTimer.setText(getString(R.string.next_report_time, timeRemaining));
                textViewTimer.setVisibility(View.VISIBLE);
                buttonSubmitReport.setEnabled(false);
            } else {
                textViewTimer.setVisibility(View.GONE);
                buttonSubmitReport.setEnabled(true);
            }
        }

        buttonSubmitReport.setOnClickListener(v -> {
            String firstQ = editTextFirstQ.getText().toString();
            String BMIQ = editTextBMIQ.getText().toString();
            List<String> lstString = getDiseasesStringList(diseasesLinearLayout);
            boolean areAllFieldsCompleted = true;
            for (int i = 0; i < lstString.toArray().length; i++){
                    if (lstString.get(i).isEmpty()){
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

                textView.setText("Logging feedback...");

                dialog.setCancelable(false);

                dialog.show();

                UserProfile userProfile = dbHelper.getUserProfile(curr_user);

                float heightInMeters = userProfile.getHeight() / 100;
                float BMI = userProfile.getWeight() / (heightInMeters * heightInMeters);

                boolean isOlder = isDateOlderThanAWeek(userProfile.getLastMedicalReport());

                if (isOlder) {

                    FitbitAPI fb = new FitbitAPI(TokenData.FITBIT_TOKEN.getToken());
                    fb.updateUserProfile(userProfile);

                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    Handler handler = new Handler(Looper.getMainLooper());

                    OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                    executor.execute(() -> {
                        try {
                            String prepPrompt = "Consider that you will work at a medical report PDF document so please follow this structured format:";
                            prepPrompt += "Please take into consideration to include the patient's username, the current date for the report and the title: Weekly Report as headers/title.";
                            prepPrompt += "Now for the content please prepare yourself for some raw data got from a Fitbit API, prepare to interpret it the best for a user and doctor to understand his state";
                            prepPrompt += "For the footer I want the pages counted so please take that in mind, I will provide you in the next prompt the user profile details for the patient and also his data for the week";

                            String userDataPrompt = generatePrompt(userProfile, BMI, firstQ, BMIQ, lstString);

                            String fullPrompt = prepPrompt + "\n\n" + userDataPrompt;

                            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                                    .model("gpt-3.5-turbo")
                                    .messages(Arrays.asList(
                                            new ChatMessage("user", fullPrompt)
                                    ))
                                    .maxTokens(2000)
                                    .build();

                            ChatCompletionResult result = service.createChatCompletion(completionRequest);
                            String weeklyReportResponse = result.getChoices().get(0).getMessage().getContent();

                            // Get current timestamp for the report
                            String reportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

                            // Create PDF with patient ID for unique filename
                            PDFGeneration pdfGeneration = new PDFGeneration(getApplicationContext());
                            File pdfFile = pdfGeneration.createPDF(weeklyReportResponse, curr_user);

                            // Don't open PDF for patients, just save it
                            // The PDF will be available for doctors to view in MedicalReportsActivity

                            handler.post(() -> {
                                // Save the report in the database
                                long reportId = dbHelper.saveMedicalReport(
                                    curr_user, 
                                    reportDate, 
                                    weeklyReportResponse, 
                                    pdfFile.getAbsolutePath()
                                );

                                if (reportId != -1) {
                                    // Update the lastMedicalReport timestamp only after all data is processed and saved
                                    userProfile.setLastMedicalReport(reportDate);
                                    dbHelper.insertOrUpdateProfile(curr_user, userProfile);

                                    // Disable the submit button after successful submission
                                    Button submitButton = findViewById(R.id.buttonSubmitReport);
                                    submitButton.setEnabled(false);

                                    // Show the timer for next available report
                                    TextView timerView = findViewById(R.id.textViewTimer);
                                    String timeRemaining = calculateTimeRemaining(userProfile.getLastMedicalReport());
                                    timerView.setText(getString(R.string.next_report_time, timeRemaining));
                                    timerView.setVisibility(View.VISIBLE);

                                    Snackbar.make(findViewById(android.R.id.content), 
                                            "Weekly feedback logged successfully. Your doctor will be able to view your report.", 
                                            Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Error saving report to database. Please try again.",
                                            Snackbar.LENGTH_LONG).show();
                                }

                                dialog.dismiss();
                            });

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
        @SuppressLint("DefaultLocale") String prompt = String.format(
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
        // First, check if there's a report in the database from the last week
        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        if (!userId.isEmpty()) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(userId);

            if (latestReport != null && latestReport.getReportDate() != null && !latestReport.getReportDate().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    LocalDateTime reportDate = LocalDateTime.parse(latestReport.getReportDate(), formatter);
                    LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

                    // If the latest report is less than a week old, the user can't submit a new report
                    if (reportDate.isAfter(oneWeekAgo)) {
                        return false;
                    }
                } catch (Exception e) {
                    Log.e("WeeklyReportActivity", "Error parsing report date", e);
                }
            }
        }

        // If no recent report in the database, check the lastMedicalReport timestamp in the UserProfile
        if (dateStr == null || dateStr.isEmpty()) {
            return true;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            return date.isBefore(oneWeekAgo);
        } catch (Exception e) {
            Log.e("WeeklyReportActivity", "Error parsing lastMedicalReport date", e);
            return true;
        }
    }

    private String calculateTimeRemaining(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "now";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime lastReportDate = LocalDateTime.parse(dateStr, formatter);
        LocalDateTime nextAvailableDate = lastReportDate.plusWeeks(1);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(nextAvailableDate)) {
            return "now";
        }

        long daysUntil = java.time.Duration.between(now, nextAvailableDate).toDays();
        long hoursUntil = java.time.Duration.between(now, nextAvailableDate).toHours() % 24;

        if (daysUntil > 0) {
            return daysUntil + " days, " + hoursUntil + " hours";
        } else {
            return hoursUntil + " hours";
        }
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
