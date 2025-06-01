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

import java.time.LocalDateTime;
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

    // Constants for SharedPreferences keys
    private static final String PREF_TEMP_DATA = "TEMP_PROFILE_DATA";
    private static final String KEY_CNP = "temp_cnp";
    private static final String KEY_NAME = "temp_name";
    private static final String KEY_AGE = "temp_age";
    private static final String KEY_HEIGHT = "temp_height";
    private static final String KEY_WEIGHT = "temp_weight";
    private static final String KEY_BODY_FAT = "temp_body_fat";
    private static final String KEY_BP_SYSTOLIC = "temp_bp_systolic";
    private static final String KEY_BP_DIASTOLIC = "temp_bp_diastolic";
    private static final String KEY_HR = "temp_hr";
    private static final String KEY_BLOOD_GLUCOSE = "temp_blood_glucose";
    private static final String KEY_CHOLESTEROL_TOTAL = "temp_cholesterol_total";
    private static final String KEY_CHOLESTEROL_HDL = "temp_cholesterol_hdl";
    private static final String KEY_CHOLESTEROL_LDL = "temp_cholesterol_ldl";
    private static final String KEY_BODY_TYPE = "temp_body_type";

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
        // If we're editing a patient's profile, use the original credentials
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
     * If we're creating a new patient or if the CNP has changed from the last loaded CNP, creates a new user.
     * Otherwise, updates the existing user's profile.
     *
     * @param currentUserId The current user ID
     * @param cnp The CNP entered by the user
     * @param userProfile The user profile to save
     * @return The user ID to use for the rest of the process
     */
    private String handleProfileSubmission(String currentUserId, String cnp, UserProfile userProfile, DatabaseHelper dbHelper) {

        if (isCreatingNewPatient || (!TextUtils.isEmpty(cnp) && !TextUtils.isEmpty(lastLoadedCnp) && !cnp.equals(lastLoadedCnp))) {
            if (isCreatingNewPatient && !TextUtils.isEmpty(creatingDoctorId)) {
                UserProfile existingProfile = dbHelper.getUserProfileByCnp(cnp);
                if (existingProfile != null) {
                    String existingPatientId = null;

                    List<User> doctorPatients = dbHelper.getPatientsForDoctor(creatingDoctorId);
                    boolean patientAlreadyAssigned = false;

                    for (User patient : doctorPatients) {
                        if (patient.getUserProfile() != null && 
                            cnp.equals(patient.getUserProfile().getCnp())) {
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
                            "Updated existing patient profile", 
                            Snackbar.LENGTH_SHORT).show();
                        return currentUserId;
                    } else {
                        Cursor cursor = dbHelper.getReadableDatabase().query(
                            DatabaseHelper.TABLE_PROFILE,
                            new String[] { DatabaseHelper.COLUMN_USER_ID },
                            "cnp=?",
                            new String[] { cnp },
                            null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            @SuppressLint("Range")
                            String patientId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
                            cursor.close();

                            dbHelper.insertOrUpdateProfile(patientId, userProfile);

                            dbHelper.assignPatientToDoctor(creatingDoctorId, patientId);

                            Snackbar.make(findViewById(android.R.id.content), 
                                "Existing patient assigned to you and profile updated", 
                                Snackbar.LENGTH_SHORT).show();
                            return currentUserId;
                        }

                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }

            String username = generateUsername(userProfile.getName());
            String password = generatePassword(userProfile.getName(), cnp);

            long newUserId = dbHelper.addTestUser(username, password, "patient");
            if (newUserId != -1) {
                dbHelper.insertOrUpdateProfile(String.valueOf(newUserId), userProfile);

                if (isCreatingNewPatient) {
                    if (!TextUtils.isEmpty(creatingDoctorId)) {
                        dbHelper.assignPatientToDoctor(creatingDoctorId, String.valueOf(newUserId));
                    }
                }


                Snackbar.make(findViewById(android.R.id.content), 
                    "Created new patient profile", 
                    Snackbar.LENGTH_SHORT).show();

                return currentUserId;
            } else {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Failed to create new patient profile", 
                    Snackbar.LENGTH_SHORT).show();
                return currentUserId;
            }
        } else {
            dbHelper.insertOrUpdateProfile(currentUserId, userProfile);
            return currentUserId;
        }
    }

    private static final int BODY_TYPE_QUIZ_REQUEST_CODE = 1001;

    /**
     * Saves the current form data to SharedPreferences when the activity is paused.
     * This ensures that the data is retained when the user navigates away from the activity
     * and then returns to it.
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveFormData();
    }

    /**
     * Saves the current form data to SharedPreferences.
     */
    private void saveFormData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_TEMP_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save basic information
        editor.putString(KEY_CNP, editTextCnp.getText().toString());
        editor.putString(KEY_NAME, editTextName.getText().toString());
        editor.putString(KEY_AGE, editTextAge.getText().toString());
        editor.putString(KEY_HEIGHT, editTextHeight.getText().toString());
        editor.putString(KEY_WEIGHT, editTextWeight.getText().toString());

        // Save additional health metrics
        editor.putString(KEY_BODY_FAT, editTextBodyFat.getText().toString());
        editor.putString(KEY_BP_SYSTOLIC, editTextBPSystolic.getText().toString());
        editor.putString(KEY_BP_DIASTOLIC, editTextBPDiastolic.getText().toString());
        editor.putString(KEY_HR, editTextHR.getText().toString());
        editor.putString(KEY_BLOOD_GLUCOSE, editTextBloodGlucose.getText().toString());
        editor.putString(KEY_CHOLESTEROL_TOTAL, editTextCholesterolTotal.getText().toString());
        editor.putString(KEY_CHOLESTEROL_HDL, editTextCholesterolHDL.getText().toString());
        editor.putString(KEY_CHOLESTEROL_LDL, editTextCholesterolLDL.getText().toString());

        if (textViewBodyType.getVisibility() == View.VISIBLE && textViewBodyType.getText() != null) {
            editor.putString(KEY_BODY_TYPE, textViewBodyType.getText().toString());
        } else {
            editor.putString(KEY_BODY_TYPE, "");
        }

        editor.apply();
    }

    /**
     * Restores the form data from SharedPreferences.
     */
    private void restoreFormData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_TEMP_DATA, MODE_PRIVATE);

        // Restore basic information
        String cnp = sharedPreferences.getString(KEY_CNP, "");
        String name = sharedPreferences.getString(KEY_NAME, "");
        String age = sharedPreferences.getString(KEY_AGE, "");
        String height = sharedPreferences.getString(KEY_HEIGHT, "");
        String weight = sharedPreferences.getString(KEY_WEIGHT, "");

        // Restore additional health metrics
        String bodyFat = sharedPreferences.getString(KEY_BODY_FAT, "");
        String bpSystolic = sharedPreferences.getString(KEY_BP_SYSTOLIC, "");
        String bpDiastolic = sharedPreferences.getString(KEY_BP_DIASTOLIC, "");
        String hr = sharedPreferences.getString(KEY_HR, "");
        String bloodGlucose = sharedPreferences.getString(KEY_BLOOD_GLUCOSE, "");
        String cholesterolTotal = sharedPreferences.getString(KEY_CHOLESTEROL_TOTAL, "");
        String cholesterolHDL = sharedPreferences.getString(KEY_CHOLESTEROL_HDL, "");
        String cholesterolLDL = sharedPreferences.getString(KEY_CHOLESTEROL_LDL, "");
        String bodyType = sharedPreferences.getString(KEY_BODY_TYPE, "");

        // Set values to form fields if they are not empty
        if (!cnp.isEmpty()) editTextCnp.setText(cnp);
        if (!name.isEmpty()) editTextName.setText(name);
        if (!age.isEmpty()) editTextAge.setText(age);
        if (!height.isEmpty()) editTextHeight.setText(height);
        if (!weight.isEmpty()) editTextWeight.setText(weight);
        if (!bodyFat.isEmpty()) editTextBodyFat.setText(bodyFat);
        if (!bpSystolic.isEmpty()) editTextBPSystolic.setText(bpSystolic);
        if (!bpDiastolic.isEmpty()) editTextBPDiastolic.setText(bpDiastolic);
        if (!hr.isEmpty()) editTextHR.setText(hr);
        if (!bloodGlucose.isEmpty()) editTextBloodGlucose.setText(bloodGlucose);
        if (!cholesterolTotal.isEmpty()) editTextCholesterolTotal.setText(cholesterolTotal);
        if (!cholesterolHDL.isEmpty()) editTextCholesterolHDL.setText(cholesterolHDL);
        if (!cholesterolLDL.isEmpty()) editTextCholesterolLDL.setText(cholesterolLDL);

        // Set body type if available
        if (!bodyType.isEmpty()) {
            textViewBodyType.setText(bodyType);
            textViewBodyType.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Clears the saved form data from SharedPreferences.
     * This should be called when the form is successfully submitted.
     */
    private void clearSavedFormData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_TEMP_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

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
        String userId = sharedPreferences.getString("userId", "");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        String curr_user = sharedPreferences.getString("userId", "");

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

        textViewGeneratedUsername = findViewById(R.id.textViewGeneratedUsername);
        textViewGeneratedPassword = findViewById(R.id.textViewGeneratedPassword);

        isCreatingNewPatient = getIntent().getBooleanExtra("create_new_patient", false);

        boolean isViewingPatientProfile = getIntent().getBooleanExtra("view_patient_profile", false);
        isEditingPatientProfile = getIntent().getBooleanExtra("edit_patient_profile", false);

        if (isViewingPatientProfile) {
            viewingPatientId = getIntent().getStringExtra("patient_id");
        }

        if (!isViewingPatientProfile && !isCreatingNewPatient) {
            restoreFormData();
        }

        if (isViewingPatientProfile && !TextUtils.isEmpty(viewingPatientId)) {
            ImageView titleImage = findViewById(R.id.image);
            titleImage.setImageResource(R.drawable.title_profile);

            bottomNav.setVisibility(View.GONE);
            buttonSubmitProfile.setVisibility(View.GONE);
            buttonBodyTypeQuiz.setVisibility(View.GONE);

            // Load the patient's information
            User patient = dbHelper.getUser(viewingPatientId);
            if (patient != null && patient.getUserProfile() != null) {
                UserProfile userProfile = patient.getUserProfile();
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

                editTextCnp.setEnabled(false);

                if (isEditingPatientProfile) {
                    // Make all fields except CNP editable
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

                    buttonSubmitProfile.setVisibility(View.VISIBLE);
                    buttonBodyTypeQuiz.setVisibility(View.VISIBLE);
                } else {
                    // Set all form fields to be non-editable
                    editTextName.setEnabled(false);
                    editTextAge.setEnabled(false);
                    editTextHeight.setEnabled(false);
                    editTextWeight.setEnabled(false);
                    editTextBodyFat.setEnabled(false);
                    editTextBPSystolic.setEnabled(false);
                    editTextBPDiastolic.setEnabled(false);
                    editTextHR.setEnabled(false);
                    editTextBloodGlucose.setEnabled(false);
                    editTextCholesterolTotal.setEnabled(false);
                    editTextCholesterolHDL.setEnabled(false);
                    editTextCholesterolLDL.setEnabled(false);
                }

                View credentialsContainer = findViewById(R.id.credentialsContainer);
                credentialsContainer.setVisibility(View.VISIBLE);

                originalUsername = generateUsername(userProfile.getName());
                originalPassword = generatePassword(userProfile.getName(), userProfile.getCnp());

                updateGeneratedCredentials(userProfile.getName(), userProfile.getCnp());
            }
        }
        // If we're creating a new patient, show the credentials container and set up the UI
        else if (isCreatingNewPatient) {
            creatingDoctorId = sharedPreferences.getString("userId", "");

            View credentialsContainer = findViewById(R.id.credentialsContainer);
            credentialsContainer.setVisibility(View.VISIBLE);

            ImageView titleImage = findViewById(R.id.image);
            titleImage.setImageResource(R.drawable.title_profile);

            bottomNav.setVisibility(View.GONE);

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

        // Set up Body Type Quiz button click listener
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

                    UserProfile existingProfile = dbHelper.getUserProfileByCnp(cnp);
                    if (existingProfile != null) {
                        lastLoadedCnp = cnp;

                        // Auto-populate fields with existing data
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

                UserProfile userProfile = new UserProfile(name, age, height, weight, "");
                userProfile.setCnp(cnp);

                if (textViewBodyType.getVisibility() == View.VISIBLE && textViewBodyType.getText() != null) {
                    String bodyType = textViewBodyType.getText().toString();
                    if (!bodyType.isEmpty()) {
                        userProfile.setBodyType(bodyType);
                    }
                }

                // Get values from additional health metrics fields
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

                String userIdToUpdate = isEditingPatientProfile ? viewingPatientId : curr_user;

                final String finalUserId = handleProfileSubmission(userIdToUpdate, cnp, userProfile, dbHelper);

                float heightInMeters = height / 100;
                float BMI = weight / (heightInMeters * heightInMeters);

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
                                .messages(Collections.singletonList(
                                        new ChatMessage("user", bmiPrompt)
                                ))
                                .build();

                        ChatCompletionResult bmiResult = service.createChatCompletion(bmiRequest);

                        String bmiResponse = bmiResult.getChoices().get(0).getMessage().getContent()
                                .replace('*', ' ')
                                .replace('#', ' ');
                        String bmiKey = "BMI#" + BMI + "#" + LocalDateTime.now();

                        String sessionUserId = finalUserId;
                        if (isEditingPatientProfile) {
                            sessionUserId = viewingPatientId;
                        } else if (isCreatingNewPatient) {
                            List<User> doctorPatients = dbHelper.getPatientsForDoctor(creatingDoctorId);

                            for (User patient : doctorPatients) {
                                if (patient.getUserProfile() != null && 
                                    cnp.equals(patient.getUserProfile().getCnp())) {
                                    sessionUserId = patient.getUserId();
                                    break;
                                }
                            }
                        }

                        long bmiId = dbHelper.insertOnSession(sessionUserId, bmiKey, bmiResponse);

                        SharedPreferences.Editor editor =
                                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                        editor.putString(bmiKey, String.valueOf(bmiId));

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
                        metabolicPrompt.append("\n5. A health score out of 100 that represents how well this patient compares to the average patient of similar age, height, and weight. Format this as 'HEALTH_SCORE: X' where X is a number between 0 and 100.");

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
                        String metabolicKey = "Metabolic#Balance#" + LocalDateTime.now();

                        long metabolicId = dbHelper.insertOnSession(sessionUserId, metabolicKey, metabolicResponse);

                        editor.putString(metabolicKey, String.valueOf(metabolicId));

                        int healthScore = 0;
                        String[] lines = metabolicResponse.split("\n");
                        for (String line : lines) {
                            if (line.contains("HEALTH_SCORE")) {
                                try {
                                    String scoreStr = line.substring(line.indexOf(":") + 1).trim();
                                    scoreStr = scoreStr.replaceAll("[^0-9]", "");
                                    healthScore = Integer.parseInt(scoreStr);
                                    healthScore = Math.max(0, Math.min(100, healthScore));
                                    break;
                                } catch (Exception e) {
                                    Log.e("ProfileSetupActivity", "Error parsing health score: " + e.getMessage());
                                }
                            }
                        }

                        userProfile.setHealthScore(healthScore);

                        if (isCreatingNewPatient) {
                            List<User> doctorPatients = dbHelper.getPatientsForDoctor(creatingDoctorId);

                            String patientId = null;
                            for (User patient : doctorPatients) {
                                if (patient.getUserProfile() != null && 
                                    cnp.equals(patient.getUserProfile().getCnp())) {
                                    patientId = patient.getUserId();
                                    break;
                                }
                            }

                            if (patientId != null) {
                                dbHelper.insertOrUpdateProfile(patientId, userProfile);
                            } else {
                                Log.e("ProfileSetupActivity", "Could not find patient with CNP: " + cnp);
                            }
                        } else if (isEditingPatientProfile) {
                            dbHelper.insertOrUpdateProfile(viewingPatientId, userProfile);
                        } else {
                            dbHelper.insertOrUpdateProfile(finalUserId, userProfile);
                        }

                        editor.apply();

                        dialog.dismiss();

                        clearSavedFormData();

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
