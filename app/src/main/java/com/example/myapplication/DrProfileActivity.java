package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrProfileActivity extends BaseActivity {
    private String lastLoadedCnp = "";
    private TextView textViewBodyType;
    private boolean isCreatingNewPatient = false;
    private boolean isEditingPatientProfile = false;
    private TextView textViewGeneratedUsername;
    private TextView textViewGeneratedPassword;
    private String creatingDoctorId = "";
    private String viewingPatientId = "";
    private String originalUsername = "";
    private String originalPassword = "";

    private EditText editTextCnp;
    private EditText editTextName;
    private EditText editTextAge;
    private EditText editTextHeight;
    private EditText editTextWeight;
    private EditText editTextBodyFat;
    private EditText editTextBPSystolic;
    private EditText editTextBPDiastolic;
    private EditText editTextHR;
    private EditText editTextBloodGlucose;
    private EditText editTextCholesterolTotal;
    private EditText editTextCholesterolHDL;
    private EditText editTextCholesterolLDL;
    private com.google.android.material.textfield.MaterialAutoCompleteTextView spinnerGender;

    /**
     * Generates a username from the patient's full name by removing spaces
     * @param fullName The patient's full name
     * @return The generated username
     */
    private String generateUsername(String fullName) {
        return fullName.trim().replace(" ", "");
    }

    /**
     * Generates a password from the patient's full name and the last 3 digits of their CNP
     * @param fullName The patient's full name
     * @param cnp The patient's CNP
     * @return The generated password
     */
    private String generatePassword(String fullName, String cnp) {
        String last3Digits = "";
        if (cnp != null && cnp.length() >= 3) {
            last3Digits = cnp.substring(cnp.length() - 3);
        }

        return fullName.trim().replace(" ", "") + last3Digits;
    }

    /**
     * Updates the generated username and password TextViews based on the patient's name and CNP
     * @param name The patient's name
     * @param cnp The patient's CNP
     */
    private void updateGeneratedCredentials(String name, String cnp) {
        if (isEditingPatientProfile && !originalUsername.isEmpty() && !originalPassword.isEmpty()) {
            textViewGeneratedUsername.setText(originalUsername);
            textViewGeneratedPassword.setText(originalPassword);
        } else if (!TextUtils.isEmpty(name)) {
            String username = generateUsername(name);
            textViewGeneratedUsername.setText(username);

            String password = generatePassword(name, cnp);
            textViewGeneratedPassword.setText(password);
        } else {
            textViewGeneratedUsername.setText("");
            textViewGeneratedPassword.setText("");
        }
    }

    /**
     * Handles the profile submission logic.
     * First checks if a patient with the given CNP exists, then:
     * - If editing an existing profile, updates it
     * - If creating a new patient and a patient with the CNP exists, updates that profile
     * - If creating a new patient and no patient with the CNP exists, creates a new patient
     *
     * @param cnp The CNP entered by the user
     * @param userProfile The user profile to save
     * @param dbHelper Database helper to perform operations
     * @return The user ID to use for the rest of the process
     */
    private String handleProfileSubmission(String cnp, Patient userProfile, DatabaseHelper dbHelper) {
        if (isEditingPatientProfile && !TextUtils.isEmpty(viewingPatientId)) {
            dbHelper.insertOrUpdateProfile(viewingPatientId, userProfile);
            return viewingPatientId;
        }

        Patient existingProfile = dbHelper.getPatientByCnp(cnp);
        if (existingProfile != null) {
            if (!TextUtils.isEmpty(creatingDoctorId)) {
                String existingPatientId = null;
                boolean patientAlreadyAssigned = false;

                List<User> doctorPatients = dbHelper.getPatientsForDoctor(creatingDoctorId);
                for (User patient : doctorPatients) {
                    if (patient.getPatient() != null && cnp.equals(patient.getPatient().getCnp())) {
                        patientAlreadyAssigned = true;
                        existingPatientId = patient.getUserId();
                        Snackbar.make(findViewById(android.R.id.content), 
                            "A patient with this CNP is already assigned to you, his profile will be updated!",
                            Snackbar.LENGTH_SHORT).show();
                        break;
                    }
                }

                if (patientAlreadyAssigned && existingPatientId != null) {
                    dbHelper.insertOrUpdateProfile(existingPatientId, userProfile);
                    Snackbar.make(findViewById(android.R.id.content), 
                        "A patient with this CNP is already assigned to you, his profile will be updated!",
                        Snackbar.LENGTH_SHORT).show();
                    return existingPatientId;
                } else {
                    Cursor cursor = dbHelper.getReadableDatabase().query(
                        DatabaseHelper.TABLE_PATIENTS,
                        new String[] { DatabaseHelper.COLUMN_USER_ID },
                        "cnp=?",
                        new String[] { cnp },
                        null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        @SuppressLint("Range")
                        String patientIdFromCnp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
                        cursor.close();

                        dbHelper.insertOrUpdateProfile(patientIdFromCnp, userProfile);
                        dbHelper.assignPatientToDoctor(creatingDoctorId, patientIdFromCnp);

                        Snackbar.make(findViewById(android.R.id.content), 
                            "Existing patient assigned to you",
                            Snackbar.LENGTH_SHORT).show();
                        return patientIdFromCnp;
                    }

                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                Cursor cursor = dbHelper.getReadableDatabase().query(
                    DatabaseHelper.TABLE_PATIENTS,
                    new String[] { DatabaseHelper.COLUMN_USER_ID },
                    "cnp=?",
                    new String[] { cnp },
                    null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    String patientIdFromCnp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
                    cursor.close();

                    dbHelper.insertOrUpdateProfile(patientIdFromCnp, userProfile);
                    Snackbar.make(findViewById(android.R.id.content), 
                        "Updated existing patient profile", 
                        Snackbar.LENGTH_SHORT).show();
                    return patientIdFromCnp;
                }

                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        String username = generateUsername(userProfile.getName());
        String password = generatePassword(userProfile.getName(), cnp);

        long newUserId = dbHelper.addTestUser(username, password, "patient");
        if (newUserId != -1) {
            String newUserIdStr = String.valueOf(newUserId);
            dbHelper.insertOrUpdateProfile(newUserIdStr, userProfile);

            if (!TextUtils.isEmpty(creatingDoctorId)) {
                dbHelper.assignPatientToDoctor(creatingDoctorId, newUserIdStr);
            }

            Snackbar.make(findViewById(android.R.id.content), 
                "Created new patient profile", 
                Snackbar.LENGTH_SHORT).show();

            return newUserIdStr;
        } else {
            Snackbar.make(findViewById(android.R.id.content), 
                "Failed to create new patient profile", 
                Snackbar.LENGTH_SHORT).show();
            return "";
        }
    }

    private static final int BODY_TYPE_QUIZ_REQUEST_CODE = 1001;

    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BODY_TYPE_QUIZ_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String bodyType = data.getStringExtra("bodyType");
            if (bodyType != null && !bodyType.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Body type determined: " + bodyType, 
                    Snackbar.LENGTH_SHORT).show();

                textViewBodyType.setText(bodyType);
                textViewBodyType.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        editTextCnp = findViewById(R.id.editTextCnp);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextBodyFat = findViewById(R.id.editTextBodyFat);
        editTextBPSystolic = findViewById(R.id.editTextBPSystolic);
        editTextBPDiastolic = findViewById(R.id.editTextBPDiastolic);
        editTextHR = findViewById(R.id.editTextHR);
        editTextBloodGlucose = findViewById(R.id.editTextBloodGlucose);
        editTextCholesterolTotal = findViewById(R.id.editTextCholesterolTotal);
        editTextCholesterolHDL = findViewById(R.id.editTextCholesterolHDL);
        editTextCholesterolLDL = findViewById(R.id.editTextCholesterolLDL);
        Button buttonSubmitProfile = findViewById(R.id.buttonSubmitProfile);
        Button buttonBodyTypeQuiz = findViewById(R.id.buttonBodyTypeQuiz);
        textViewBodyType = findViewById(R.id.textViewBodyType);

        spinnerGender = findViewById(R.id.spinnerGender);
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        spinnerGender.setSimpleItems(genderOptions);

        textViewGeneratedUsername = findViewById(R.id.textViewGeneratedUsername);
        textViewGeneratedPassword = findViewById(R.id.textViewGeneratedPassword);

        isCreatingNewPatient = getIntent().getBooleanExtra("create_new_patient", false);

        boolean isViewingPatientProfile = getIntent().getBooleanExtra("view_patient_profile", false);
        isEditingPatientProfile = getIntent().getBooleanExtra("edit_patient_profile", false);

        if (isViewingPatientProfile) {
            viewingPatientId = getIntent().getStringExtra("patient_id");
        }

        if (isViewingPatientProfile && !TextUtils.isEmpty(viewingPatientId)) {
            ImageView titleImage = findViewById(R.id.image);
            titleImage.setImageResource(R.drawable.title_profile);

            User patient = dbHelper.getUser(viewingPatientId);
            if (patient != null && patient.getPatient() != null) {
                Patient userProfile = patient.getPatient();
                if (userProfile.getCnp() != null && !userProfile.getCnp().isEmpty()) {
                    editTextCnp.setText(userProfile.getCnp());
                }
                editTextName.setText(userProfile.getName());
                editTextAge.setText(String.valueOf(userProfile.getAge()));
                editTextHeight.setText(String.valueOf(userProfile.getHeight()));
                editTextWeight.setText(String.valueOf(userProfile.getWeight()));

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

                String bodyType = userProfile.getBodyType();
                if (bodyType != null && !bodyType.isEmpty()) {
                    textViewBodyType.setText(bodyType);
                } else {
                    textViewBodyType.setText("Not specified");
                }
                textViewBodyType.setVisibility(View.VISIBLE);

                String gender = userProfile.getGender();
                if (gender != null && !gender.isEmpty() && spinnerGender != null) {
                    spinnerGender.setText(gender, false);
                }

                editTextCnp.setEnabled(false);

                if (isEditingPatientProfile) {
                    editTextName.setEnabled(true);
                    editTextAge.setEnabled(true);
                    editTextHeight.setEnabled(true);
                    editTextWeight.setEnabled(true);
                    editTextBodyFat.setEnabled(true);
                    editTextBPSystolic.setEnabled(true);
                    editTextBPDiastolic.setEnabled(true);
                    editTextHR.setEnabled(true);
                    editTextBloodGlucose.setEnabled(true);
                    editTextCholesterolTotal.setEnabled(true);
                    editTextCholesterolHDL.setEnabled(true);
                    editTextCholesterolLDL.setEnabled(true);
                    spinnerGender.setEnabled(true);

                }
                View credentialsContainer = findViewById(R.id.credentialsContainer);
                credentialsContainer.setVisibility(View.VISIBLE);

                // Retrieve the actual username and password from the database
                String[] credentials = dbHelper.getUserCredentials(viewingPatientId);
                if (credentials != null) {
                    originalUsername = credentials[0];
                    originalPassword = credentials[1];
                } else {
                    // Fallback to generated credentials if retrieval fails
                    originalUsername = generateUsername(userProfile.getName());
                    originalPassword = generatePassword(userProfile.getName(), userProfile.getCnp());
                }

                updateGeneratedCredentials(userProfile.getName(), userProfile.getCnp());
            }
        }
        else if (isCreatingNewPatient) {
            creatingDoctorId = sharedPreferences.getString("userId", "");

            View credentialsContainer = findViewById(R.id.credentialsContainer);
            credentialsContainer.setVisibility(View.VISIBLE);

            ImageView titleImage = findViewById(R.id.image);
            titleImage.setImageResource(R.drawable.title_profile);

            editTextName.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    updateGeneratedCredentials(editTextName.getText().toString(), editTextCnp.getText().toString());
                }
            });

            editTextCnp.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    updateGeneratedCredentials(editTextName.getText().toString(), editTextCnp.getText().toString());
                }
            });
        }

        buttonBodyTypeQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(DrProfileActivity.this, DrQuizActivity.class);
            startActivityForResult(intent, BODY_TYPE_QUIZ_REQUEST_CODE);
        });


        // Add CNP validation and search functionality
        editTextCnp.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String cnp = editTextCnp.getText().toString();
                if (!TextUtils.isEmpty(cnp)) {
                    String cnpRegex = "^[1-9]\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])(0[1-9]|[1-4]\\d|5[0-2]|99)(00[1-9]|0[1-9]\\d|[1-9]\\d\\d)\\d$";
                    if (!cnp.matches(cnpRegex)) {
                        Snackbar.make(findViewById(android.R.id.content), "Invalid CNP format!", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    if (!cnp.isEmpty()) {
                        char firstDigit = cnp.charAt(0);
                        if (firstDigit == '1' || firstDigit == '5') {
                            spinnerGender.setText("male", false);
                        } else if (firstDigit == '2' || firstDigit == '6') {
                            spinnerGender.setText("female", false);
                        }
                    }

                    Patient existingProfile = dbHelper.getPatientByCnp(cnp);
                    if (existingProfile != null) {
                        lastLoadedCnp = cnp;

                        editTextName.setText(existingProfile.getName());
                        editTextAge.setText(String.valueOf(existingProfile.getAge()));
                        editTextHeight.setText(String.valueOf(existingProfile.getHeight()));
                        editTextWeight.setText(String.valueOf(existingProfile.getWeight()));

                        if (existingProfile.getBodyFatPercentage() > 0) {
                            editTextBodyFat.setText(String.valueOf(existingProfile.getBodyFatPercentage()));
                        }
                        if (existingProfile.getBloodPressureSystolic() > 0) {
                            editTextBPSystolic.setText(String.valueOf(existingProfile.getBloodPressureSystolic()));
                        }
                        if (existingProfile.getBloodPressureDiastolic() > 0) {
                            editTextBPDiastolic.setText(String.valueOf(existingProfile.getBloodPressureDiastolic()));
                        }
                        if (existingProfile.getRestingHeartRate() > 0) {
                            editTextHR.setText(String.valueOf(existingProfile.getRestingHeartRate()));
                        }
                        if (existingProfile.getBloodGlucose() > 0) {
                            editTextBloodGlucose.setText(String.valueOf(existingProfile.getBloodGlucose()));
                        }
                        if (existingProfile.getCholesterolTotal() > 0) {
                            editTextCholesterolTotal.setText(String.valueOf(existingProfile.getCholesterolTotal()));
                        }
                        if (existingProfile.getCholesterolHDL() > 0) {
                            editTextCholesterolHDL.setText(String.valueOf(existingProfile.getCholesterolHDL()));
                        }
                        if (existingProfile.getCholesterolLDL() > 0) {
                            editTextCholesterolLDL.setText(String.valueOf(existingProfile.getCholesterolLDL()));
                        }

                        String bodyType = existingProfile.getBodyType();
                        if (bodyType != null && !bodyType.isEmpty()) {
                            textViewBodyType.setText(bodyType);
                        } else {
                            textViewBodyType.setText("Not specified");
                        }
                        textViewBodyType.setVisibility(View.VISIBLE);

                        Snackbar.make(findViewById(android.R.id.content), "Profile data loaded from existing patient!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonSubmitProfile.setOnClickListener(v -> {
            String cnp = editTextCnp.getText().toString();
            String name = editTextName.getText().toString();
            String ageTxt = editTextAge.getText().toString();
            String heightTxt = editTextHeight.getText().toString();
            String weightTxt = editTextWeight.getText().toString();

            if (!TextUtils.isEmpty(cnp)) {
                String cnpRegex = "^[1-9]\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])(0[1-9]|[1-4]\\d|5[0-2]|99)(00[1-9]|0[1-9]\\d|[1-9]\\d\\d)\\d$";
                if (!cnp.matches(cnpRegex)) {
                    Snackbar.make(findViewById(android.R.id.content), "Invalid CNP format!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }

            if (TextUtils.isEmpty(cnp) || TextUtils.isEmpty(name) || TextUtils.isEmpty(ageTxt)
                    || TextUtils.isEmpty(heightTxt) || TextUtils.isEmpty(weightTxt)) {
                Snackbar
                        .make(findViewById(android.R.id.content), "Please fill out all required fields!",
                                Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                final Dialog dialog = new Dialog(DrProfileActivity.this);

                dialog.setContentView(R.layout.custom_dialog);

                dialog.findViewById(R.id.progress);
                TextView textView = dialog.findViewById(R.id.text);

                textView.setText("Saving profile...");

                dialog.setCancelable(false);

                dialog.show();

                int age = Integer.parseInt(ageTxt);
                float height = Float.parseFloat(heightTxt);
                float weight = Float.parseFloat(weightTxt);

                Patient userProfile = new Patient(name, age, height, weight, "");
                userProfile.setCnp(cnp);

                if (isEditingPatientProfile && !TextUtils.isEmpty(viewingPatientId)) {
                    User existingPatient = dbHelper.getUser(viewingPatientId);
                    if (existingPatient != null && existingPatient.getPatient() != null) {
                        int existingHealthScore = existingPatient.getPatient().getHealthScore();
                        userProfile.setHealthScore(existingHealthScore);
                    }
                }

                if (textViewBodyType.getVisibility() == View.VISIBLE && textViewBodyType.getText() != null) {
                    String bodyType = textViewBodyType.getText().toString();
                    if (!bodyType.isEmpty()) {
                        userProfile.setBodyType(bodyType);
                    }
                }

                if (spinnerGender != null && spinnerGender.getText() != null && !spinnerGender.getText().toString().isEmpty()) {
                    String gender = spinnerGender.getText().toString();
                    userProfile.setGender(gender);
                }

                String bodyFatTxt = editTextBodyFat.getText().toString();
                String bpSystolicTxt = editTextBPSystolic.getText().toString();
                String bpDiastolicTxt = editTextBPDiastolic.getText().toString();
                String hrTxt = editTextHR.getText().toString();
                String bloodGlucoseTxt = editTextBloodGlucose.getText().toString();
                String cholesterolTotalTxt = editTextCholesterolTotal.getText().toString();
                String cholesterolHDLTxt = editTextCholesterolHDL.getText().toString();
                String cholesterolLDLTxt = editTextCholesterolLDL.getText().toString();

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

                final String finalUserId;
                if (isEditingPatientProfile && !TextUtils.isEmpty(viewingPatientId)) {
                    int currentHealthScore = userProfile.getHealthScore();

                    dbHelper.insertOrUpdateProfile(viewingPatientId, userProfile);
                    finalUserId = viewingPatientId;

                    Log.d("ProfileSetupActivity", "Health score after initial profile update: " + userProfile.getHealthScore() +
                          " (was " + currentHealthScore + ")");

                    Snackbar.make(findViewById(android.R.id.content),
                        "Updated patient profile",
                        Snackbar.LENGTH_SHORT).show();
                } else {
                    finalUserId = handleProfileSubmission(cnp, userProfile, dbHelper);
                    if (TextUtils.isEmpty(finalUserId)) {
                        dialog.dismiss();
                        return;
                    }
                }

                float heightInMeters = height / 100;
                float BMI = weight / (heightInMeters * heightInMeters);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                executor.execute(() -> {
                    try {

                        String bmiPrompt = userProfile.getAge() + " year old having " + userProfile.getHeight()
                                + " cm "
                                + " and " + userProfile.getWeight() + " kg and " + BMI
                                + "BMI. Provide a detailed analysis of this BMI value, including health implications, potential risks, and personalized recommendations based on the person's age and measurements. Include specific advice for diet and exercise if appropriate.";

                        ChatCompletionRequest bmiRequest = ChatCompletionRequest.builder()
                                .model("gpt-3.5-turbo")
                                .messages(Collections.singletonList(
                                        new ChatMessage("user", bmiPrompt)
                                ))
                                .build();
                        ChatCompletionResult bmiResult = service.createChatCompletion(bmiRequest);

                        String bmiResponse = bmiResult.getChoices().get(0).getMessage().getContent()
                                .replace('*', ' ')
                                .replace('#', ' ');
                        userProfile.setBmiInterpretation(bmiResponse);

                        StringBuilder metabolicPrompt = new StringBuilder();
                        metabolicPrompt.append(userProfile.getAge()).append(" year old having ").append(userProfile.getHeight()).append(" cm and ").append(userProfile.getWeight()).append(" kg");

                        if (userProfile.getBodyFatPercentage() > 0) {
                            metabolicPrompt.append(" with body fat percentage of ").append(userProfile.getBodyFatPercentage()).append("%");
                        }

                        if (userProfile.getBodyType() != null && !userProfile.getBodyType().isEmpty()) {
                            metabolicPrompt.append(", body type: ").append(userProfile.getBodyType());
                        }

                        if (userProfile.getBloodPressureSystolic() > 0 && userProfile.getBloodPressureDiastolic() > 0) {
                            metabolicPrompt.append(", blood pressure ").append(userProfile.getBloodPressureSystolic()).append("/").append(userProfile.getBloodPressureDiastolic()).append(" mmHg");
                        }

                        if (userProfile.getRestingHeartRate() > 0) {
                            metabolicPrompt.append(", resting heart rate ").append(userProfile.getRestingHeartRate()).append(" bpm");
                        }

                        if (userProfile.getBloodGlucose() > 0) {
                            metabolicPrompt.append(", blood glucose ").append(userProfile.getBloodGlucose()).append(" mg/dL");
                        }

                        if (userProfile.getCholesterolTotal() > 0) {
                            metabolicPrompt.append(", total cholesterol ").append(userProfile.getCholesterolTotal()).append(" mg/dL");

                            if (userProfile.getCholesterolHDL() > 0) {
                                metabolicPrompt.append(", HDL cholesterol ").append(userProfile.getCholesterolHDL()).append(" mg/dL");
                            }

                            if (userProfile.getCholesterolLDL() > 0) {
                                metabolicPrompt.append(", LDL cholesterol ").append(userProfile.getCholesterolLDL()).append(" mg/dL");
                            }
                        }

                        metabolicPrompt.append("\nPlease provide a detailed analysis of metabolic health based on these values, including:");
                        metabolicPrompt.append("\n1. Overall metabolic health assessment");
                        metabolicPrompt.append("\n2. Potential metabolic risks or concerns");
                        metabolicPrompt.append("\n3. Specific recommendations for improving metabolic health");
                        metabolicPrompt.append("\n4. Lifestyle changes that would benefit this individual");

                        ChatCompletionRequest metabolicRequest = ChatCompletionRequest.builder()
                                .model("gpt-3.5-turbo")
                                .messages(Collections.singletonList(
                                        new ChatMessage("user", metabolicPrompt.toString())
                                ))
                                .build();
                        ChatCompletionResult metabolicResult = service.createChatCompletion(metabolicRequest);

                        String metabolicResponse = metabolicResult.getChoices().get(0).getMessage().getContent()
                                .replace('*', ' ')
                                .replace('#', ' ');
                        userProfile.setMetabolicInterpretation(metabolicResponse);

                        // Calculate health score using the HealthScoreCalculator
                        int calculatedHealthScore = HealthScoreCalculator.calculateHealthScore(userProfile);

                        // Only update if we have a valid score
                        if (calculatedHealthScore > 0) {
                            Log.d("ProfileSetupActivity", "Changing health score from " +
                                  userProfile.getHealthScore() + " to " + calculatedHealthScore + 
                                  " (calculated by HealthScoreCalculator)");
                            userProfile.setHealthScore(calculatedHealthScore);
                        } else {
                            Log.d("ProfileSetupActivity", "HealthScoreCalculator returned invalid score: " + 
                                  calculatedHealthScore + ". Keeping existing health score: " + 
                                  userProfile.getHealthScore());
                        }

                        dbHelper.insertOrUpdateProfile(finalUserId, userProfile);
                        dialog.dismiss();

                        if (isCreatingNewPatient) {
                            Intent intent = new Intent(DrProfileActivity.this, DrDashboardActivity.class);

                            SharedPreferences doctorPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                            doctorPrefs.edit().putString("userId", creatingDoctorId).apply();

                            startActivity(intent);
                            finish();
                        } 
                        else if (isEditingPatientProfile) {
                            Intent intent = new Intent(DrProfileActivity.this, DrPatientsActivity.class);

                            intent.putExtra("patient_id", viewingPatientId);

                            startActivity(intent);
                            finish();
                        }
                        else {
                            Intent intent = new Intent(DrProfileActivity.this, PtDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

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
    }
}
