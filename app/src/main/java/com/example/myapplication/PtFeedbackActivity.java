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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for generating and submitting weekly health reports.
 * This activity allows patients to:
 * 1. Submit weekly health feedback about their general health and specific conditions
 * 2. View their previously generated health reports
 * 3. Track when they can submit their next report
 * The activity integrates with Fitbit API to collect health metrics and uses OpenAI
 * to generate comprehensive health reports based on the collected data and patient feedback.
 * These reports are saved as PDF files and can be viewed by both the patient and their doctors.
 */
public class PtFeedbackActivity extends BaseActivity {
    /**
     * Initializes the activity, sets up UI components, and configures the weekly report submission functionality.
     * This method:
     * 1. Sets up the bottom navigation
     * 2. Initializes input fields for general health and BMI assessment
     * 3. Dynamically creates input fields for each of the patient's diseases
     * 4. Configures the submit report button with validation and submission logic
     * 5. Sets up the view last report button if a report exists
     * 6. Displays and configures the timer showing when the next report can be submitted
     * 7. Handles notification logic for report availability
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                          Otherwise it is null.
     */
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

        List<Disease> diseasesList = dbHelper.getDiseasesForPatient(curr_user);

        LinearLayout diseasesLinearLayout = findViewById(R.id.diseasesLinearLayout);
        for (Disease disease : diseasesList) {
            addDiseaseQuestion(diseasesLinearLayout, disease, states);
        }

        Button buttonSubmitReport = findViewById(R.id.buttonSubmitReport);
        Button buttonViewLastReport = findViewById(R.id.buttonViewLastReport);
        TextView textViewTimer = findViewById(R.id.textViewTimer);

        if (user != null && user.getUserProfile() != null) {
            Patient userProfile = user.getPatient();

            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(curr_user);

            if (latestReport != null && latestReport.getReportPath() != null && !latestReport.getReportPath().isEmpty()) {
                File file = new File(latestReport.getReportPath());
                if (file.exists()) {
                    buttonViewLastReport.setVisibility(View.VISIBLE);
                    buttonViewLastReport.setOnClickListener(viewReportBtn -> {
                        try {
                            // Log the report path for debugging
                            Log.d("WeeklyReportActivity", "Report path: " + latestReport.getReportPath());
                            Log.d("WeeklyReportActivity", "File exists at path: " + file.getAbsolutePath());

                            Uri uri = FileProvider.getUriForFile(PtFeedbackActivity.this,
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

            String timeRemaining = calculateTimeRemaining(userProfile.getLastMedicalReport());
            textViewTimer.setText(getString(R.string.next_report_time, timeRemaining));
            textViewTimer.setVisibility(View.VISIBLE);

            if (timeRemaining.equals("now")) {
                textViewTimer.setTextColor(getResources().getColor(R.color.green));
                buttonSubmitReport.setEnabled(true);

                SharedPreferences notificationPrefs = getSharedPreferences("NOTIFICATION_PREFS", MODE_PRIVATE);
                boolean timerExpirationNotified = notificationPrefs.getBoolean("timer_expiration_notified_" + curr_user, false);

                if (!timerExpirationNotified) {
                    String message = "Your weekly report is now available to submit. Please log your feedback.";
                    dbHelper.saveNotification(curr_user, message, "timer_expired");

                    SharedPreferences.Editor editor = notificationPrefs.edit();
                    editor.putBoolean("timer_expiration_notified_" + curr_user, true);
                    editor.apply();
                }
            } else {
                textViewTimer.setTextColor(getResources().getColor(R.color.logoColorRed));
                buttonSubmitReport.setEnabled(false);

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
            if (!lstString.isEmpty()) {
                Object[] stringArray = lstString.toArray();
                for (int i = 0; i < stringArray.length; i++){
                    if (lstString.get(i).isEmpty()){
                        areAllFieldsCompleted = false;
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(firstQ) || TextUtils.isEmpty(BMIQ) || !areAllFieldsCompleted) {
                Snackbar
                .make(findViewById(android.R.id.content), "Please fill out all fields",
                        Snackbar.LENGTH_SHORT)
                .show();
            } else {
                final Dialog dialog = new Dialog(PtFeedbackActivity.this);

                dialog.setContentView(R.layout.custom_dialog);

                dialog.findViewById(R.id.progress);
                TextView textView = dialog.findViewById(R.id.text);

                textView.setText("Connecting to FitBit...");

                dialog.setCancelable(false);

                dialog.show();

                Patient userProfile = (Patient) dbHelper.getUserProfile(curr_user);

                float heightInMeters = userProfile.getHeight() / 100;
                float BMI = userProfile.getWeight() / (heightInMeters * heightInMeters);

                boolean isOlder = isDateOlderThanAWeek(userProfile.getLastMedicalReport());

                if (isOlder) {
                    // Create a flag to track whether Fitbit data has been processed
                    final boolean[] fitbitDataProcessed = {false};

                    FitbitAPI fb = new FitbitAPI(TokenData.FITBIT_TOKEN.getToken());
                    // Use the callback to ensure Fitbit data is processed before generating the report
                    fb.updateUserProfile(userProfile, () -> {
                        Log.d("WeeklyReportActivity", "Fitbit data processing completed");
                        fitbitDataProcessed[0] = true;
                    });

                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    Handler handler = new Handler(Looper.getMainLooper());

                    String token = TokenData.OPEN_AI_SERVICE_KEY.getToken();
                    OpenAiService service = new OpenAiService(token, Duration.ofSeconds(120));

                    executor.execute(() -> {
                        try {
                            // Wait for Fitbit data to be processed with a timeout
                            int maxWaitTimeMs = 30000; // 30 seconds timeout
                            int waitedMs = 0;
                            while (!fitbitDataProcessed[0] && waitedMs < maxWaitTimeMs) {
                                try {
                                    Thread.sleep(100); // Wait 100ms before checking again
                                    waitedMs += 100;
                                } catch (InterruptedException e) {
                                    Log.e("WeeklyReportActivity", "Interrupted while waiting for Fitbit data", e);
                                    break;
                                }
                            }

                            if (!fitbitDataProcessed[0]) {
                                Log.w("WeeklyReportActivity", "Timed out waiting for Fitbit data, proceeding with report generation anyway");
                            }

                            // Now that Fitbit data is processed, generate the report
                            String prepPrompt = "Consider that you will work at a medical report PDF document so please replace the data in the parentheses () with the values and replace the square brackets [] with your actual analysis based on the patient data provided in parentheses within each section. You should not have in the PDF [ ] or ( ) and neither text between them, all of them should be replaced with values or with ' ' if there is no value for that. For the { }, align the values before that like it is mentioned between { } and then remove the { } and the text in between. Use the data to provide meaningful insights and recommendations in a minimalist and useful way.";

                            String lstRep = userProfile.getLastMedicalReport();

                            String userDataPrompt = generatePrompt(userProfile, BMI, firstQ, BMIQ, lstString, lstRep);

                            String fullPrompt = prepPrompt + "\n\n" + userDataPrompt;

                            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                                    .model("o4-mini")
                                    .messages(Collections.singletonList(
                                            new ChatMessage("user", fullPrompt)
                                    ))
                                    .build();

                            ChatCompletionResult result = service.createChatCompletion(completionRequest);

                            final String weeklyReportResponse;
                            if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                                weeklyReportResponse = result.getChoices().get(0).getMessage().getContent();
                            } else {
                                weeklyReportResponse = "Error generating report. Please try again later.";
                                Log.e("WeeklyReportActivity", "OpenAI API returned null or empty result");
                            }

                            String reportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

                            PDFGeneration pdfGeneration = new PDFGeneration(getApplicationContext());
                            File pdfFile = pdfGeneration.createPDF(weeklyReportResponse, curr_user, userProfile);

                            handler.post(() -> {
                                long reportId = dbHelper.saveMedicalReport(
                                    curr_user,
                                    "",
                                    reportDate, 
                                    weeklyReportResponse, 
                                    pdfFile.getAbsolutePath()
                                );

                                if (reportId != -1) {
                                    userProfile.setLastMedicalReport(reportDate);
                                    dbHelper.insertOrUpdateProfile(curr_user, userProfile);

                                    NotificationUtils.scheduleNextFeedbackNotification(getApplicationContext(), curr_user, reportDate);

                                    List<User> doctors = dbHelper.getDoctorsForPatient(curr_user);
                                    if (doctors != null && !doctors.isEmpty()) {
                                        for (User doctor : doctors) {
                                            String patientName = userProfile.getName();
                                            String message = patientName + " has submitted their weekly feedback. A new medical report is available.";
                                            dbHelper.saveNotification(doctor.getUserId(), message, "feedback_submitted");

                                            // Reset the notification flag for this doctor so they'll see the notification next time they log in
                                            SharedPreferences notificationPrefs = getSharedPreferences("DOCTOR_NOTIFICATION_PREFS", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = notificationPrefs.edit();
                                            editor.putBoolean("notification_shown_" + doctor.getUserId(), false);
                                            editor.apply();

                                            // No system notification is shown - will be displayed as popup when doctor opens the app
                                        }
                                    }

                                    Button submitButton = findViewById(R.id.buttonSubmitReport);
                                    submitButton.setEnabled(false);

                                    TextView timerView = findViewById(R.id.textViewTimer);
                                    String timeRemaining = calculateTimeRemaining(userProfile.getLastMedicalReport());
                                    timerView.setText(getString(R.string.next_report_time, timeRemaining));
                                    timerView.setVisibility(View.VISIBLE);

                                    timerView.setTextColor(getResources().getColor(R.color.logoColorRed));

                                    Button viewLastReportButton = findViewById(R.id.buttonViewLastReport);
                                    viewLastReportButton.setVisibility(View.VISIBLE);

                                    viewLastReportButton.setOnClickListener(viewReportBtn -> {
                                        try {
                                            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(curr_user);

                                            if (latestReport != null && latestReport.getReportPath() != null && !latestReport.getReportPath().isEmpty()) {
                                                Log.d("WeeklyReportActivity", "Report path: " + latestReport.getReportPath());

                                                File file = new File(latestReport.getReportPath());
                                                if (file.exists()) {
                                                    Log.d("WeeklyReportActivity", "File exists at path: " + file.getAbsolutePath());

                                                    Uri uri = FileProvider.getUriForFile(PtFeedbackActivity.this,
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
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Error saving report to database. Please try again.",
                                            Snackbar.LENGTH_LONG).show();

                                    Button viewLastReportButton = findViewById(R.id.buttonViewLastReport);
                                    viewLastReportButton.setVisibility(View.GONE);

                                    Button submitButton = findViewById(R.id.buttonSubmitReport);
                                    submitButton.setEnabled(true);
                                }

                                dialog.dismiss();
                            });

                        } catch (Exception e) {
                            dialog.dismiss();
                            handler.post(() -> {
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Saving failed! Try again later!", Snackbar.LENGTH_SHORT)
                                        .show();

                                Button viewLastReportButton = findViewById(R.id.buttonViewLastReport);
                                viewLastReportButton.setVisibility(View.GONE);

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

    /**
     * Extracts the user's input about their diseases from the UI components.
     * This method iterates through all child views of the provided LinearLayout,
     * finds TextInputLayout components containing AutoCompleteTextView elements,
     * and collects the text input from each one into a list.
     *
     * @param parentLayout The LinearLayout containing disease assessment input fields
     * @return A List of Strings containing the user's assessment of each disease state
     */
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

    /**
     * Generates a prompt for the OpenAI API to create a medical report following a specific format.
     * This method constructs a structured prompt containing patient information, Fitbit data,
     * and health metrics formatted according to the required medical report structure.
     *
     * @param userProfile The user's profile containing health metrics and personal information
     * @param BMI The calculated Body Mass Index value
     * @param firstQ The user's response to the general health self-assessment question
     * @param BMIQ The user's response to the BMI self-assessment question
     * @param diseasesStates A list of the user's assessments of their disease states
     * @return A formatted string prompt for the OpenAI API
     */
    public String generatePrompt(Patient userProfile, double BMI, String firstQ, String BMIQ, List<String> diseasesStates, String lastReport) {
        String diseasesString = diseasesStates != null ? String.join(", ", diseasesStates) : "";

        @SuppressLint("DefaultLocale") String prompt = String.format(
                "**" + userProfile.getName() + "**\n\n" +
                "I. Introduction {subtitle aligned left}\n\n" +
                "This weekly report provides a comprehensive overview of the patient state of the overall health over the last week. " +
                "The report aims to ensure continuous monitoring of the doctors over the patient with chronic diseases and improvement in healthcare service delivery at MediTrack.\n\n" +
                "II. Summary of the Feedback {subtitle aligned left}\n\n" +
                "A. Patient Log {subsubtitle aligned left}\n\n" +
                "[Analyze the patient response to feedback questions about physical, mental state and if there are any diseases to them] (Patient feedback - General health: %s, BMI self-assessment: %s, Current conditions: %s)\n\n\n\n" +
                "B. BMI and Metabollic Balance {subsubtitle aligned left}\n\n" +
                "[Analyze the patient feedback in concordance with the BMI and Metabollic Balance data you know about the patient and give some insights and find patterns for the patient to optimize] (Height: %.2f cm, Weight: %.2f kg, BMI: %.2f, Body Fat: %.1f%%, Blood Pressure: %d/%d mmHg, Blood Glucose: %.1f mg/dL, Cholesterol - Total: %.1f mg/dL, HDL: %.1f mg/dL, LDL: %.1f mg/dL, Health Score: %d/100)\n\n\n\n" +
                "III. Observations and Trends {subtitle aligned left}\n\n" +
                "[Fill the tables with real data comparing the previous medical reports and give reference to them and mention any uptrends / downtrends of the patient, find his challenges and offer some recommadations specific to his needs] (Activity data - Average Steps: %d steps, Average Time in Bed: %d minutes, Average Active Minutes: %d minutes); This is the last report: %s.\n\n\n\n" +
                "IV. Conclusion {subtitle aligned left}\n\n" +
                "A. Summary {subsubtitle aligned left}\n\n" +
                "Throughout the week, [give some insights and summaries on the progress of the patient].\n\n" +
                "B. Key Takeaways {subsubtitle aligned left}\n\n" +
                "The challenges that have been identified require immediate and focused attention to guarantee the ongoing provision of high-quality medical services at MediTrack.\n\n" +
                "C. Next Steps {subsubtitle aligned left}\n\n" +
                "The implementation of the recommended action plan will undergo a thorough process of monitoring and review, which will be meticulously documented in the upcoming weekly report. " +
                "This detailed assessment aims to evaluate the progress made thus far and identify any areas that might require further enhancements. " +
                "If necessary, additional improvements will be suggested to ensure the action plan continues to be effective and efficient.",


                firstQ,
                BMIQ,
                diseasesString,

                userProfile.getHeight(),
                userProfile.getWeight(),
                BMI,
                userProfile.getBodyFatPercentage(),
                userProfile.getBloodPressureSystolic(),
                userProfile.getBloodPressureDiastolic(),
                userProfile.getBloodGlucose(),
                userProfile.getCholesterolTotal(),
                userProfile.getCholesterolHDL(),
                userProfile.getCholesterolLDL(),
                userProfile.getHealthScore(),

                userProfile.getAverageSteps(),
                userProfile.getTimeInBed(),
                userProfile.getAverageActiveZoneMinutes(),
                lastReport
        );

        return prompt;
    }

    /**
     * Determines if a user can submit a new weekly report based on the date of their last report.
     * This method checks two conditions:
     * 1. If there's a report in the database from the last week
     * 2. If the provided date string (from user profile) is more than a week old
     * If either condition indicates the user hasn't submitted a report in the last week,
     * the method returns true, allowing the user to submit a new report.
     *
     * @param dateStr The date string of the user's last medical report in format "yyyy-MM-dd'T'HH:mm:ss"
     * @return true if the user can submit a new report (last report is older than a week or doesn't exist),
     *         false if the user has already submitted a report within the last week
     */
    private boolean isDateOlderThanAWeek(String dateStr) {
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

                    if (reportDate.isAfter(oneWeekAgo)) {
                        return false;
                    }
                } catch (Exception e) {
                    Log.e("WeeklyReportActivity", "Error parsing report date", e);
                }
            }
        }

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

    /**
     * Calculates and formats the time remaining until a user can submit their next weekly report.
     * This method:
     * 1. First checks the database for the latest report and calculates time remaining based on that
     * 2. If no report is found in the database, uses the provided date string from the user profile
     * 3. Calculates the next available date (one week after the last report)
     * 4. Determines if the current time is before or after that date
     * 5. Returns a formatted string representing the time remaining, or "now" if a report can be submitted
     *
     * @param dateStr The date string of the user's last medical report in format "yyyy-MM-dd'T'HH:mm:ss"
     * @return A formatted string representing the time remaining (e.g., "3 days, 5 hours") or "now" if a report can be submitted
     */
    private String calculateTimeRemaining(String dateStr) {
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

    /**
     * Dynamically creates UI components for disease state assessment.
     * This method:
     * 1. Creates a TextInputLayout with appropriate styling
     * 2. Sets the hint text to the disease name plus "state?"
     * 3. Creates an AutoCompleteTextView inside the TextInputLayout
     * 4. Sets up an adapter with predefined states (very poor, poor, ok, good, very good)
     * 5. Adds the created components to the parent layout
     *
     * @param parentLayout The LinearLayout where the disease question UI will be added
     * @param disease The Disease object containing information about the disease
     * @param states An array of strings representing possible disease states (e.g., "very poor", "poor", etc.)
     */
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
