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

        // Get diseases for the current patient from the database
        // This will only include diseases that have not been deleted
        List<Disease> diseasesList = dbHelper.getDiseasesForPatient(curr_user);

        // Convert to ArrayList if needed
        if (!(diseasesList instanceof ArrayList)) {
            diseasesList = new ArrayList<>(diseasesList);
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

            // First check if there's a report in the database
            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(curr_user);

            if (latestReport != null && latestReport.getReportPath() != null && !latestReport.getReportPath().isEmpty()) {
                // Check if the PDF file exists
                File file = new File(latestReport.getReportPath());
                if (file.exists()) {
                    // Show the button to view the last report
                    buttonViewLastReport.setVisibility(View.VISIBLE);

                    // Set up the button click listener
                    buttonViewLastReport.setOnClickListener(viewReportBtn -> {
                        try {
                            // Log the report path for debugging
                            Log.d("WeeklyReportActivity", "Report path: " + latestReport.getReportPath());
                            Log.d("WeeklyReportActivity", "File exists at path: " + file.getAbsolutePath());

                            Uri uri = FileProvider.getUriForFile(WeeklyReportActivity.this,
                                    getPackageName() + ".provider", file);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("WeeklyReportActivity", "Error opening PDF file: " + e.getMessage(), e);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Error opening PDF file!",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("WeeklyReportActivity", "File does not exist at path: " + file.getAbsolutePath());
                    buttonViewLastReport.setVisibility(View.GONE);
                }
            } else {
                Log.d("WeeklyReportActivity", "No report available or empty report path");
                buttonViewLastReport.setVisibility(View.GONE);
            }

            // Calculate time remaining until next report
            String timeRemaining = calculateTimeRemaining(userProfile.getLastMedicalReport());
            textViewTimer.setText(getString(R.string.next_report_time, timeRemaining));
            textViewTimer.setVisibility(View.VISIBLE);

            // Set color based on availability
            if (timeRemaining.equals("now")) {
                textViewTimer.setTextColor(getResources().getColor(R.color.green));
                buttonSubmitReport.setEnabled(true);

                // Check if we need to create a notification for the timer expiration
                SharedPreferences notificationPrefs = getSharedPreferences("NOTIFICATION_PREFS", MODE_PRIVATE);
                boolean timerExpirationNotified = notificationPrefs.getBoolean("timer_expiration_notified_" + curr_user, false);

                if (!timerExpirationNotified) {
                    // Create a notification for the timer expiration
                    String message = "Your weekly report is now available to submit. Please log your feedback.";
                    dbHelper.saveNotification(curr_user, message, "timer_expired");

                    // Mark that we've notified the user about this timer expiration
                    SharedPreferences.Editor editor = notificationPrefs.edit();
                    editor.putBoolean("timer_expiration_notified_" + curr_user, true);
                    editor.apply();
                }
            } else {
                textViewTimer.setTextColor(getResources().getColor(R.color.logoColorRed));
                buttonSubmitReport.setEnabled(false);

                // Reset the notification flag when the timer is not expired
                SharedPreferences notificationPrefs = getSharedPreferences("NOTIFICATION_PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = notificationPrefs.edit();
                editor.putBoolean("timer_expiration_notified_" + curr_user, false);
                editor.apply();
            }
        }

        buttonSubmitReport.setOnClickListener(v -> {
            String firstQ = editTextFirstQ.getText().toString();
            String BMIQ = editTextBMIQ.getText().toString();
            List<String> lstString = getDiseasesStringList(diseasesLinearLayout);
            boolean areAllFieldsCompleted = true;
            if (lstString != null && !lstString.isEmpty()) {
                Object[] stringArray = lstString.toArray();
                for (int i = 0; i < stringArray.length; i++){
                    if (lstString.get(i).isEmpty()){
                        areAllFieldsCompleted = false;
                        break;
                    }
                }
            } else {
                areAllFieldsCompleted = false;
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

                            // Add null checks to prevent NullPointerException
                            final String weeklyReportResponse;
                            if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                                weeklyReportResponse = result.getChoices().get(0).getMessage().getContent();
                            } else {
                                // Handle the case where result or choices is null or empty
                                weeklyReportResponse = "Error generating report. Please try again later.";
                                Log.e("WeeklyReportActivity", "OpenAI API returned null or empty result");
                            }

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
                                    "",
                                    reportDate, 
                                    weeklyReportResponse, 
                                    pdfFile.getAbsolutePath()
                                );

                                if (reportId != -1) {
                                    // Update the lastMedicalReport timestamp only after all data is processed and saved
                                    userProfile.setLastMedicalReport(reportDate);
                                    dbHelper.insertOrUpdateProfile(curr_user, userProfile);

                                    // Create notifications for doctors assigned to this patient
                                    // Get all doctors assigned to this patient
                                    List<User> doctors = dbHelper.getDoctorsForPatient(curr_user);
                                    if (doctors != null && !doctors.isEmpty()) {
                                        // Create a notification for each doctor
                                        for (User doctor : doctors) {
                                            String patientName = userProfile.getName();
                                            String message = patientName + " has submitted their weekly feedback. A new medical report is available.";
                                            dbHelper.saveNotification(doctor.getUserId(), message, "feedback_submitted");
                                        }
                                    }

                                    // Disable the submit button after successful submission
                                    Button submitButton = findViewById(R.id.buttonSubmitReport);
                                    submitButton.setEnabled(false);

                                    // Show the timer for next available report
                                    TextView timerView = findViewById(R.id.textViewTimer);
                                    String timeRemaining = calculateTimeRemaining(userProfile.getLastMedicalReport());
                                    timerView.setText(getString(R.string.next_report_time, timeRemaining));
                                    timerView.setVisibility(View.VISIBLE);

                                    // Set color based on availability (always red after submitting a report)
                                    timerView.setTextColor(getResources().getColor(R.color.logoColorRed));

                                    // Make the View Last Report button visible
                                    Button viewLastReportButton = findViewById(R.id.buttonViewLastReport);
                                    viewLastReportButton.setVisibility(View.VISIBLE);

                                    // Set up the button click listener
                                    viewLastReportButton.setOnClickListener(viewReportBtn -> {
                                        try {
                                            // Get the latest report from the database
                                            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(curr_user);

                                            if (latestReport != null && latestReport.getReportPath() != null && !latestReport.getReportPath().isEmpty()) {
                                                // Log the report path for debugging
                                                Log.d("WeeklyReportActivity", "Report path: " + latestReport.getReportPath());

                                                // Open the PDF file using the path from the database
                                                File file = new File(latestReport.getReportPath());
                                                if (file.exists()) {
                                                    Log.d("WeeklyReportActivity", "File exists at path: " + file.getAbsolutePath());

                                                    Uri uri = FileProvider.getUriForFile(WeeklyReportActivity.this,
                                                            getPackageName() + ".provider", file);

                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setDataAndType(uri, "application/pdf");
                                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                    startActivity(intent);
                                                } else {
                                                    Log.e("WeeklyReportActivity", "File does not exist at path: " + file.getAbsolutePath());
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            "PDF file not found!",
                                                            Snackbar.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Log.e("WeeklyReportActivity", "No report available or empty report path");
                                                Snackbar.make(findViewById(android.R.id.content),
                                                        "No report available!",
                                                        Snackbar.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            Log.e("WeeklyReportActivity", "Error opening PDF file: " + e.getMessage(), e);
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    "Error opening PDF file!",
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    });

                                    Snackbar.make(findViewById(android.R.id.content), 
                                            "Weekly feedback logged successfully. Your doctor will be able to view your report.", 
                                            Snackbar.LENGTH_LONG).show();
                                } else {
                                    // Error saving report to database
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Error saving report to database. Please try again.",
                                            Snackbar.LENGTH_LONG).show();

                                    // Make sure the View Last Report button is not visible
                                    Button viewLastReportButton = findViewById(R.id.buttonViewLastReport);
                                    viewLastReportButton.setVisibility(View.GONE);

                                    // Re-enable the submit button
                                    Button submitButton = findViewById(R.id.buttonSubmitReport);
                                    submitButton.setEnabled(true);
                                }

                                dialog.dismiss();
                            });

                        } catch (Exception e) {
                            dialog.dismiss();
                            handler.post(() -> {
                                // Show error message
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Saving failed! Try again later!", Snackbar.LENGTH_SHORT)
                                        .show();

                                // Make sure the View Last Report button is not visible
                                Button viewLastReportButton = findViewById(R.id.buttonViewLastReport);
                                viewLastReportButton.setVisibility(View.GONE);

                                // Re-enable the submit button
                                Button submitButton = findViewById(R.id.buttonSubmitReport);
                                submitButton.setEnabled(true);
                            });
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
                "You are creating a comprehensive medical report PDF for a patient based on their Fitbit data and health metrics. " +
                "You must structure your response as a formal medical report with clear, well-delimited sections using markdown headings " +
                "(# for main title, ## for sections, ### for subsections).\n\n" +

                "# PATIENT INFORMATION AND DATA\n" +
                "Current date: %s\n" +
                "Patient name: %s\n" +
                "Age: %d years\n" +
                "Height: %.2f cm\n" +
                "Weight: %.2f kg\n" +
                "BMI: %.2f\n" +
                "Body Fat Percentage: %.1f%%\n" +
                "Blood Pressure: %d/%d mmHg\n" +
                "Resting Heart Rate: %d bpm\n" +
                "Blood Glucose: %.1f mg/dL\n" +
                "Cholesterol - Total: %.1f mg/dL\n" +
                "Cholesterol - HDL: %.1f mg/dL\n" +
                "Cholesterol - LDL: %.1f mg/dL\n" +
                "Health Score: %d/100\n\n" +

                "# FITBIT ACTIVITY DATA\n" +
                "Average steps per day: %d steps\n" +
                "Sedentary minutes per day: %d minutes\n" +
                "Resting heart rate: %d bpm\n" +
                "Average breathing rate: %.2f breaths per minute\n" +
                "Active zone minutes per week: %d minutes\n\n" +

                "# SLEEP DATA\n" +
                "Minutes after wakeup: %d\n" +
                "Minutes awake: %d\n" +
                "Minutes to fall asleep: %d\n" +
                "Restless events: %d\n" +
                "Restless duration: %d minutes\n" +
                "Total time in bed: %d minutes (%.1f hours)\n" +
                "Deep sleep: %d minutes (%.1f hours)\n" +
                "Light sleep: %d minutes (%.1f hours)\n" +
                "REM sleep: %d minutes (%.1f hours)\n" +
                "Wake during sleep: %d minutes\n" +
                "Sleep efficiency: %.1f%%\n\n" +

                "# CARDIOVASCULAR METRICS\n" +
                "Heart rate variability (daily RMSSD): %.2f ms\n" +
                "Heart rate variability (deep RMSSD): %.2f ms\n" +
                "Cardio fitness score (VO2 max): %s\n\n" +

                "# PATIENT FEEDBACK\n" +
                "General health self-assessment: %s\n" +
                "BMI self-assessment: %s\n" +
                "Current conditions (ICD10) self-assessment: %s\n\n" +

                "# REPORT STRUCTURE REQUIREMENTS\n" +
                "Create a formal medical report with the following sections. Each section must be clearly delimited with proper headings and spacing:\n\n" +

                "1. **MEDICAL REPORT HEADER**\n" +
                "   - Title: \"WEEKLY HEALTH ASSESSMENT REPORT\"\n" +
                "   - Patient: Full name\n" +
                "   - Date: Current date formatted as Month Day, Year\n" +
                "   - Report ID: Generate a unique report ID\n" +
                "   - Physician: Dr. [Use 'Medic' as the physician name]\n\n" +

                "2. **PATIENT PROFILE SUMMARY**\n" +
                "   - Brief demographic information\n" +
                "   - Key health indicators (BMI, blood pressure, etc.)\n" +
                "   - Current health status overview\n\n" +

                "3. **BMI AND BODY COMPOSITION ANALYSIS**\n" +
                "   - Current BMI: %.2f (calculate exact category)\n" +
                "   - Detailed interpretation of BMI category for this %d-year-old patient\n" +
                "   - Body composition assessment including body fat percentage\n" +
                "   - Health implications of current measurements\n" +
                "   - Comparison to ideal ranges for patient's demographic\n\n" +

                "4. **METABOLIC HEALTH ASSESSMENT**\n" +
                "   - Activity level analysis (steps, sedentary time, active minutes)\n" +
                "   - Cardiovascular indicators (resting heart rate, blood pressure)\n" +
                "   - Metabolic efficiency indicators\n" +
                "   - Specific insights on how these values compare to clinical guidelines\n" +
                "   - Metabolic health risk assessment\n\n" +

                "5. **CARDIOVASCULAR FUNCTION**\n" +
                "   - Heart rate variability analysis (detailed interpretation of RMSSD values)\n" +
                "   - VO2 max assessment and fitness level classification\n" +
                "   - Cardiovascular risk stratification\n" +
                "   - Specific interpretations of what these values indicate for long-term health\n\n" +

                "6. **SLEEP QUALITY ANALYSIS**\n" +
                "   - Sleep architecture breakdown (deep, light, REM percentages)\n" +
                "   - Sleep efficiency calculation and interpretation\n" +
                "   - Sleep quality assessment\n" +
                "   - Sleep hygiene evaluation\n" +
                "   - Impact of current sleep patterns on health\n\n" +

                "7. **PATIENT SELF-ASSESSMENT INTEGRATION**\n" +
                "   - Analysis of patient's subjective health reports\n" +
                "   - Correlation between objective metrics and subjective experience\n" +
                "   - Identification of perception-reality gaps\n" +
                "   - Psychological aspects of health management\n\n" +

                "8. **CLINICAL RECOMMENDATIONS**\n" +
                "   - Prioritized, actionable recommendations (minimum 5)\n" +
                "   - Lifestyle modifications with specific targets\n" +
                "   - Exercise prescription with frequency, intensity, time, and type\n" +
                "   - Nutritional guidance relevant to metabolic profile\n" +
                "   - Sleep optimization strategies\n" +
                "   - Stress management techniques if indicated\n" +
                "   - Follow-up testing recommendations\n\n" +

                "9. **SUMMARY AND PROGNOSIS**\n" +
                "   - Concise overview of key findings\n" +
                "   - Health trajectory assessment\n" +
                "   - Potential health outcomes with and without intervention\n" +
                "   - Timeline for expected improvements\n\n" +

                "Format this as a professional medical document with:\n" +
                "- Clear section demarcation\n" +
                "- Professional medical terminology with patient-friendly explanations\n" +
                "- Strategic use of bullet points for clarity\n" +
                "- Bold text for critical values and key recommendations\n" +
                "- Clinical interpretation alongside each major metric\n" +
                "- Reference ranges where appropriate\n" +
                "- Page numbers in format 'Page X of Y'\n\n" +

                "This report will be converted to PDF format, so maintain proper formatting with clear paragraph breaks and consistent spacing.",

                // Patient information
                LocalDateTime.now().toString(),
                userProfile.getName(),
                userProfile.getAge(),
                userProfile.getHeight(),
                userProfile.getWeight(),
                BMI,
                userProfile.getBodyFatPercentage(),
                userProfile.getBloodPressureSystolic(),
                userProfile.getBloodPressureDiastolic(),
                userProfile.getRestingHeartRate(),
                userProfile.getBloodGlucose(),
                userProfile.getCholesterolTotal(),
                userProfile.getCholesterolHDL(),
                userProfile.getCholesterolLDL(),
                userProfile.getHealthScore(),

                // Activity data
                userProfile.getAverageSteps(),
                userProfile.getAverageSedentaryMinutes(),
                userProfile.getRestingHeartRate(),
                userProfile.getAverageBreathingRate(),
                userProfile.getAverageActiveZoneMinutes(),

                // Sleep data
                userProfile.getMinutesAfterWakeup(),
                userProfile.getMinutesAwake(),
                userProfile.getMinutesToFallAsleep(),
                userProfile.getRestlessCount(),
                userProfile.getRestlessDuration(),
                userProfile.getTimeInBed(),
                userProfile.getTimeInBed() / 60.0f,
                userProfile.getDeepSleep(),
                userProfile.getDeepSleep() / 60.0f,
                userProfile.getLightSleep(),
                userProfile.getLightSleep() / 60.0f,
                userProfile.getRemSleep(),
                userProfile.getRemSleep() / 60.0f,
                userProfile.getWakeSleep(),
                // Calculate sleep efficiency (time asleep / time in bed * 100)
                userProfile.getTimeInBed() > 0 ? 
                    (float)(userProfile.getTimeInBed() - userProfile.getWakeSleep()) / userProfile.getTimeInBed() * 100 : 0,

                // Cardiovascular metrics
                userProfile.getAverageDailyRmssd(),
                userProfile.getAverageDeepRmssd(),
                userProfile.getVo2Max(),

                // Patient feedback
                firstQ,
                BMIQ,
                diseasesStates != null ? String.join(", ", diseasesStates) : "",

                // Additional parameters for specific sections
                BMI,
                userProfile.getAge()
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
                    LocalDateTime nextAvailableDate = reportDate.plusWeeks(1);
                    LocalDateTime now = LocalDateTime.now();

                    if (now.isBefore(nextAvailableDate)) {
                        long daysUntil = java.time.Duration.between(now, nextAvailableDate).toDays();
                        long hoursUntil = java.time.Duration.between(now, nextAvailableDate).toHours() % 24;

                        if (daysUntil > 0) {
                            return daysUntil + " days, " + hoursUntil + " hours";
                        } else {
                            return hoursUntil + " hours";
                        }
                    }
                } catch (Exception e) {
                    Log.e("WeeklyReportActivity", "Error parsing report date", e);
                }
            }
        }

        // If no recent report in the database, check the lastMedicalReport timestamp in the UserProfile
        if (dateStr == null || dateStr.isEmpty()) {
            return "now";
        }

        try {
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
        } catch (Exception e) {
            Log.e("WeeklyReportActivity", "Error parsing lastMedicalReport date", e);
            return "now";
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

        textInputLayout.setHint(disease.getName() +  "state?");

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
