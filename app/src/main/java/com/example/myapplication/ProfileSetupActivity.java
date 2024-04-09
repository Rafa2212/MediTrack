package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;
import com.theokanning.openai.threads.ThreadRequest;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextAge = findViewById(R.id.editTextAge);
        EditText editTextHeight = findViewById(R.id.editTextHeight);
        EditText editTextWeight = findViewById(R.id.editTextWeight);
        EditText editTextBloodPressure = findViewById(R.id.editTextBP);
        EditText editTextHeartrate = findViewById(R.id.editTextHR);
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
            editTextBloodPressure.setText(String.valueOf(userProfile.getBloodPressure()));
            editTextHeartrate.setText(String.valueOf(userProfile.getHeartrate()));
        }

        buttonSubmitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = editTextName.getText().toString();
                String ageTxt = editTextAge.getText().toString();
                String heightTxt = editTextHeight.getText().toString();
                String weightTxt = editTextWeight.getText().toString();
                String bpTxt = editTextBloodPressure.getText().toString();
                String hrTxt = editTextHeartrate.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageTxt)
                        || TextUtils.isEmpty(heightTxt) || TextUtils.isEmpty(weightTxt) ||
                        TextUtils.isEmpty(bpTxt) || TextUtils.isEmpty(hrTxt)) {
                    Snackbar.make(findViewById(android.R.id.content), "Please fill out all fields", Snackbar.LENGTH_SHORT).show();
                }
                else{
                    final Dialog dialog = new Dialog(ProfileSetupActivity.this);

                    dialog.setContentView(R.layout.custom_dialog);

                    ProgressBar progressBar = dialog.findViewById(R.id.progress);
                    TextView textView = dialog.findViewById(R.id.text);

                    textView.setText("Saving profile...");

                    dialog.setCancelable(false);

                    dialog.show();

                    int age = Integer.parseInt(ageTxt);
                    float height = Float.parseFloat(heightTxt);
                    float weight = Float.parseFloat(weightTxt);
                    int bloodPressure = Integer.parseInt(bpTxt);
                    int heartrate = Integer.parseInt(hrTxt);

                    UserProfile userProfile = new UserProfile(name, age, height, weight, bloodPressure, heartrate);
                    dbHelper.insertOrUpdateProfile(userId, userProfile);

                    float BMI = weight / (height * height);

                    boolean exists = false;

                    SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                    Map<String, ?> allEntries = preferences.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        String[] key = entry.getKey().split("#");
                        if (Objects.equals(key[0], "BMI") && Objects.equals(key[1], String.valueOf(BMI))) {
                            exists = true;
                        }
                    }

                    if (!exists){
                        ExecutorService executor = Executors.newSingleThreadExecutor();

                        Handler handler = new Handler(Looper.getMainLooper());

                        OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Assistant assistant = service.retrieveAssistant(TokenData.ASSISTANT_ID.getToken());

                                    Thread thread = service.createThread(new ThreadRequest());

                                    String prompt = userProfile.getAge() + " year old having " + userProfile.getHeight() + " cm " + " and " + userProfile.getWeight() + " kg and " + BMI + "BMI. Show me some insights if that is over average, under, possible diseases based on the BMI.";

                                    MessageRequest messageRequest = MessageRequest.builder()
                                            .role("user")
                                            .content(prompt)
                                            .build();

                                    service.createMessage(thread.getId(), messageRequest);

                                    RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                                            .assistantId(assistant.getId())
                                            .build();

                                    Run run = service.createRun(thread.getId(), runCreateRequest);

                                    Run retrievedRun;
                                    do {
                                        retrievedRun = service.retrieveRun(thread.getId(), run.getId());
                                    }
                                    while (!(retrievedRun.getStatus().equals("completed")) && !(retrievedRun.getStatus().equals("failed")));

                                    OpenAiResponse<Message> response = service.listMessages(thread.getId());

                                    Message respMsg = service.retrieveMessage(thread.getId(), response.getFirstId());

                                    SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                                    editor.putString("BMI#" + String.valueOf(BMI), respMsg.getContent().get(0).getText().
                                            getValue().replace('*', ' ').replace('#', ' '));
                                    editor.apply();

                                    dialog.dismiss();
                                    Intent intent = new Intent(ProfileSetupActivity.this, AddDiseaseActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                } catch (Exception e) {
                                    handler.post(() -> {
                                        Snackbar.make(findViewById(android.R.id.content), "Saving failed! Try again later!", Snackbar.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        });
                    }
                    else{
                        dialog.dismiss();
                        Intent intent = new Intent(ProfileSetupActivity.this, AddDiseaseActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
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