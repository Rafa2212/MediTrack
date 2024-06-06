package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
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

        //TODO: implementare pentru Fitbit API: intai faci un prompt cu prepare pentru un structured format (il gandesti inainte)
        // si dupa abia un prompt mare cu toate datele pentru medical report
        //TODO: implement on a new page activity 3-4 questions about how did you felt this week
        //TODO: integrate the new questions and get your medical report button with the new logic and
        // the ICD10 codes add them only based on the API to verify if there are in the right format and take them into account
        // when you press get your medical report
        //TODO: implement a logic so the user can only do this once a week
        //TODO: remove the prompt when you save the user profile
        //TODO: do the prompt only on the get your medical report si eventual implementare intr-un PDF

        //rough estimate: 1 zi jumate (marti, miercuri)

        EditText editTextHeight = findViewById(R.id.editTextHeight);
        EditText editTextWeight = findViewById(R.id.editTextWeight);
        Button buttonSubmitProfile = findViewById(R.id.buttonSubmitProfile);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        User user = dbHelper.getUser(curr_user);

        if (user != null && user.getUserProfile() != null) {
            UserProfile userProfile = user.getUserProfile();
            editTextName.setText(userProfile.getName());
            editTextAge.setText(String.valueOf(userProfile.getAge()));
            editTextHeight.setText(String.valueOf(userProfile.getHeight()));
            editTextWeight.setText(String.valueOf(userProfile.getWeight()));
        }

        buttonSubmitProfile.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String ageTxt = editTextAge.getText().toString();
            String heightTxt = editTextHeight.getText().toString();
            String weightTxt = editTextWeight.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageTxt) || TextUtils.isEmpty(heightTxt)
                    || TextUtils.isEmpty(weightTxt)) {
                Snackbar
                        .make(findViewById(android.R.id.content), "Please fill out all fields",
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

                UserProfile userProfile =
                        new UserProfile(name, age, height, weight, "");
                dbHelper.insertOrUpdateProfile(curr_user, userProfile);

                FitbitAPI fb = new FitbitAPI(TokenData.FITBIT_TOKEN.getToken());
                fb.updateUserProfile(userProfile);

                float heightInMeters = height / 100;
                float BMI = weight / (heightInMeters * heightInMeters);

                boolean exists = false;

                SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                Map<String, ?> allEntries = preferences.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    String[] key = entry.getKey().split("#");
                    if (Objects.equals(key[0], "BMI") && Objects.equals(key[1], String.valueOf(BMI))) {
                        exists = true;
                    }
                }

                if (!exists) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    Handler handler = new Handler(Looper.getMainLooper());

                    OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                    executor.execute(() -> {
                        try {
                            Assistant assistant = service.retrieveAssistant(TokenData.ASSISTANT_ID.getToken());

                            Thread thread = service.createThread(new ThreadRequest());

                            String prompt = userProfile.getAge() + " year old having " + userProfile.getHeight()
                                    + " cm "
                                    + " and " + userProfile.getWeight() + " kg and " + BMI
                                    + "BMI. Show me some short insights if that is over average, under, possible diseases based on the BMI.";

                            MessageRequest messageRequest =
                                    MessageRequest.builder().role("user").content(prompt).build();

                            service.createMessage(thread.getId(), messageRequest);

                            RunCreateRequest runCreateRequest =
                                    RunCreateRequest.builder().assistantId(assistant.getId()).build();

                            Run run = service.createRun(thread.getId(), runCreateRequest);

                            Run retrievedRun;
                            do {
                                retrievedRun = service.retrieveRun(thread.getId(), run.getId());
                            } while (!(retrievedRun.getStatus().equals("completed"))
                                    && !(retrievedRun.getStatus().equals("failed")));
                            OpenAiResponse<Message> response = service.listMessages(thread.getId());
                            Message respMsg = service.retrieveMessage(thread.getId(), response.getFirstId());
                            String bmiResponse =
                                    respMsg.getContent().get(0).getText().getValue().replace('*', ' ').replace(
                                            '#', ' ');
                            String key = "BMI#" + BMI + "#" + LocalDateTime.now();
                            long bmiId = dbHelper.insertOnSession(curr_user, key, bmiResponse);

                            SharedPreferences.Editor editor =
                                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                            editor.putString(key, String.valueOf(bmiId));
                            editor.apply();

                            dialog.dismiss();
                            Intent intent = new Intent(ProfileSetupActivity.this, AddDiseaseActivity.class);
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
                } else {
                    dialog.dismiss();
                    Intent intent = new Intent(ProfileSetupActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

        if (user != null && user.getUserProfile() != null) {
            bottomNav.setVisibility(View.VISIBLE);
        } else {
            bottomNav.setVisibility(View.GONE);
        }
    }
}