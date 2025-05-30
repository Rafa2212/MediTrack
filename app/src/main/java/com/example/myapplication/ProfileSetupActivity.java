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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileSetupActivity extends BaseActivity {
    private String lastLoadedCnp = ""; // Track the last loaded CNP
    private TextView textViewBodyType; // TextView to display body type
    private boolean isCreatingNewPatient = false; // Flag to indicate if we're creating a new patient
    private boolean isViewingPatientProfile = false; // Flag to indicate if we're viewing a patient's profile
    private boolean isEditingPatientProfile = false; // Flag to indicate if we're editing a patient's profile
    private TextView textViewGeneratedUsername; // TextView to display generated username
    private TextView textViewGeneratedPassword; // TextView to display generated password
    private String creatingDoctorId = ""; // Store the doctor ID when creating a new patient
    private String viewingPatientId = ""; // Store the patient ID when viewing a patient's profile
    private String originalUsername = ""; // Store the original username when viewing/editing a patient's profile
    private String originalPassword = ""; // Store the original password when viewing/editing a patient's profile

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

    // Form field references
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
        // Remove any leading/trailing spaces and return the name without spaces
        return fullName.trim().replace(" ", "");
    }

    /**
     * Generates a password from the patient's full name and the last 3 digits of their CNP
     * @param fullName The patient's full name
     * @param cnp The patient's CNP
     * @return The generated password
     */
    private String generatePassword(String fullName, String cnp) {
        // Get the last 3 digits of the CNP
        String last3Digits = "";
        if (cnp != null && cnp.length() >= 3) {
            last3Digits = cnp.substring(cnp.length() - 3);
        }

        // Combine the full name (without spaces) with the last 3 digits
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
        // Check if we're creating a new patient or if the CNP has changed from the last loaded CNP
        if (isCreatingNewPatient || (!TextUtils.isEmpty(cnp) && !TextUtils.isEmpty(lastLoadedCnp) && !cnp.equals(lastLoadedCnp))) {
            // If creating a new patient, check if a patient with the same CNP is already assigned to this doctor
            if (isCreatingNewPatient && !TextUtils.isEmpty(creatingDoctorId)) {
                // First check if a patient with this CNP exists anywhere in the system
                UserProfile existingProfile = dbHelper.getUserProfileByCnp(cnp);
                if (existingProfile != null) {
                    // Patient exists in the system, get their ID
                    String existingPatientId = null;

                    // Get all patients for this doctor to check if the patient is already assigned
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
                        // Patient is already assigned to this doctor, just update their profile
                        dbHelper.insertOrUpdateProfile(existingPatientId, userProfile);
                        Snackbar.make(findViewById(android.R.id.content), 
                            "Updated existing patient profile", 
                            Snackbar.LENGTH_SHORT).show();
                        return currentUserId; // Return the original user ID (doctor's ID)
                    } else {
                        // Patient exists but is not assigned to this doctor
                        // Find the patient's ID from their profile
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

                            // Update the patient's profile
                            dbHelper.insertOrUpdateProfile(patientId, userProfile);

                            // Assign the patient to this doctor
                            dbHelper.assignPatientToDoctor(creatingDoctorId, patientId);

                            Snackbar.make(findViewById(android.R.id.content), 
                                "Existing patient assigned to you and profile updated", 
                                Snackbar.LENGTH_SHORT).show();
                            return currentUserId; // Return the original user ID (doctor's ID)
                        }

                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }

                // If we get here, either the patient doesn't exist or we couldn't find their ID
                // Continue with creating a new patient
            }

            // Generate username and password
            String username = generateUsername(userProfile.getName());
            String password = generatePassword(userProfile.getName(), cnp);

            // Create a new patient
            long newUserId = dbHelper.addTestUser(username, password, "patient");
            if (newUserId != -1) {
                // Insert the profile for the new user
                dbHelper.insertOrUpdateProfile(String.valueOf(newUserId), userProfile);

                // If we're creating a new patient from the doctor dashboard, assign the patient to the doctor
                if (isCreatingNewPatient) {
                    // Assign the patient to the doctor using the stored doctor ID
                    if (!TextUtils.isEmpty(creatingDoctorId)) {
                        dbHelper.assignPatientToDoctor(creatingDoctorId, String.valueOf(newUserId));
                    }
                }

                // Do not update shared preferences to keep the current user (doctor) logged in

                Snackbar.make(findViewById(android.R.id.content), 
                    "Created new patient profile", 
                    Snackbar.LENGTH_SHORT).show();

                // Return the original user ID instead of the new one
                return currentUserId;
            } else {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Failed to create new patient profile", 
                    Snackbar.LENGTH_SHORT).show();
                return currentUserId;
            }
        } else {
            // CNP hasn't changed, update the existing profile
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

        // Save form data to SharedPreferences
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

        // Save body type if visible
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

                // Update the TextView with the body type
                textViewBodyType.setText(bodyType);
                textViewBodyType.setVisibility(View.VISIBLE);

                // The body type is already saved in the database by the quiz activity
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Hide the bottom navigation menu completely as per requirements
        bottomNav.setVisibility(View.GONE);

        // Get user information
        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        boolean isDoctor = dbHelper.isDoctor(userId);
        String curr_user = sharedPreferences.getString("userId", "");

        // Initialize form field references
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

        // Initialize the generated credentials TextViews
        textViewGeneratedUsername = findViewById(R.id.textViewGeneratedUsername);
        textViewGeneratedPassword = findViewById(R.id.textViewGeneratedPassword);

        // Check if we're creating a new patient
        isCreatingNewPatient = getIntent().getBooleanExtra("create_new_patient", false);

        // Check if we're viewing a patient's profile
        isViewingPatientProfile = getIntent().getBooleanExtra("view_patient_profile", false);

        // Check if we're editing a patient's profile
        isEditingPatientProfile = getIntent().getBooleanExtra("edit_patient_profile", false);

        // If we're viewing a patient's profile, get the patient ID
        if (isViewingPatientProfile) {
            viewingPatientId = getIntent().getStringExtra("patient_id");
        }

        // Restore form data from SharedPreferences if available and we're not viewing a patient's profile or creating a new patient
        if (!isViewingPatientProfile && !isCreatingNewPatient) {
            restoreFormData();
        }

        // If we're viewing a patient's profile, load the patient's information and set all fields to be non-editable
        if (isViewingPatientProfile && !TextUtils.isEmpty(viewingPatientId)) {
            // Set the title to indicate we're viewing a patient's profile
            ImageView titleImage = findViewById(R.id.image);
            titleImage.setImageResource(R.drawable.title_profile);

            // Hide the bottom navigation
            bottomNav.setVisibility(View.GONE);

            // Hide the submit button and body type quiz button
            buttonSubmitProfile.setVisibility(View.GONE);
            buttonBodyTypeQuiz.setVisibility(View.GONE);

            // Load the patient's information
            User patient = dbHelper.getUser(viewingPatientId);
            if (patient != null && patient.getUserProfile() != null) {
                UserProfile userProfile = patient.getUserProfile();

                // Set the values in the form fields
                if (userProfile.getCnp() != null && !userProfile.getCnp().isEmpty()) {
                    editTextCnp.setText(userProfile.getCnp());
                }
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

                // Display body type if available, otherwise show "Not specified"
                String bodyType = userProfile.getBodyType();
                if (bodyType != null && !bodyType.isEmpty()) {
                    textViewBodyType.setText(bodyType);
                } else {
                    textViewBodyType.setText("Not specified");
                }
                textViewBodyType.setVisibility(View.VISIBLE);

                // If editing patient profile, only make CNP non-editable
                // Otherwise, set all form fields to be non-editable
                editTextCnp.setEnabled(false); // CNP is always non-editable

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

                    // Make the submit button visible
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

                // Show the credentials container with generated username and password
                View credentialsContainer = findViewById(R.id.credentialsContainer);
                credentialsContainer.setVisibility(View.VISIBLE);

                // Store the original username and password
                originalUsername = generateUsername(userProfile.getName());
                originalPassword = generatePassword(userProfile.getName(), userProfile.getCnp());

                // Generate and display username and password for the patient
                updateGeneratedCredentials(userProfile.getName(), userProfile.getCnp());
            }
        }
        // If we're creating a new patient, show the credentials container and set up the UI
        else if (isCreatingNewPatient) {
            // Store the doctor's ID
            creatingDoctorId = sharedPreferences.getString("userId", "");

            // Show the credentials container
            View credentialsContainer = findViewById(R.id.credentialsContainer);
            credentialsContainer.setVisibility(View.VISIBLE);

            // Set the title to indicate we're creating a new patient
            ImageView titleImage = findViewById(R.id.image);
            titleImage.setImageResource(R.drawable.title_profile); // You might want to create a new title image

            // Hide the bottom navigation as we're creating a new patient
            bottomNav.setVisibility(View.GONE);

            // Set up listeners to update the generated credentials when the name or CNP changes
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
            Intent intent = new Intent(ProfileSetupActivity.this, BodyTypeQuizActivity.class);
            startActivityForResult(intent, BODY_TYPE_QUIZ_REQUEST_CODE);
        });


        // Add CNP validation and search functionality
        editTextCnp.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String cnp = editTextCnp.getText().toString();
                if (!TextUtils.isEmpty(cnp)) {
                    // Validate CNP using regex
                    String cnpRegex = "^[1-9]\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])(0[1-9]|[1-4]\\d|5[0-2]|99)(00[1-9]|0[1-9]\\d|[1-9]\\d\\d)\\d$";
                    if (!cnp.matches(cnpRegex)) {
                        Snackbar.make(findViewById(android.R.id.content), "Invalid CNP format!", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // Search for existing profile with this CNP
                    UserProfile existingProfile = dbHelper.getUserProfileByCnp(cnp);
                    if (existingProfile != null) {
                        // Store the CNP that was loaded
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

                        // Display body type if available, otherwise show "Not specified"
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

            // Store current user ID for later use
            final String initialUserId = curr_user;

            // Validate CNP if provided
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
                userProfile.setCnp(cnp);

                // Set body type if available
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

                // Use the helper method to handle profile submission
                // Note: handleProfileSubmission now always returns the original currentUserId
                // When editing a patient's profile, use the patient's ID instead of the doctor's ID
                String userIdToUpdate = isEditingPatientProfile ? viewingPatientId : curr_user;
                String updatedUserId = handleProfileSubmission(userIdToUpdate, cnp, userProfile, dbHelper);

                // Update the current user ID for the rest of the process
                final String finalUserId = updatedUserId;

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

                        // Determine which user ID to use for the session
                        String sessionUserId = finalUserId;
                        if (isEditingPatientProfile) {
                            // If we're editing a patient's profile, use the patient's ID
                            sessionUserId = viewingPatientId;
                        } else if (isCreatingNewPatient) {
                            // If we're creating a new patient, we need to find the patient's ID
                            // Get all patients for this doctor
                            List<User> doctorPatients = dbHelper.getPatientsForDoctor(creatingDoctorId);

                            // Find the patient with the matching CNP
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
                        metabolicPrompt.append(userProfile.getAge() + " year old having " + userProfile.getHeight() + " cm and " + userProfile.getWeight() + " kg");

                        if (userProfile.getBodyFatPercentage() > 0) {
                            metabolicPrompt.append(" with body fat percentage of " + userProfile.getBodyFatPercentage() + "%");
                        }

                        if (userProfile.getBodyType() != null && !userProfile.getBodyType().isEmpty()) {
                            metabolicPrompt.append(", body type: " + userProfile.getBodyType());
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

                        // Use the same sessionUserId that we determined for the BMI session
                        long metabolicId = dbHelper.insertOnSession(sessionUserId, metabolicKey, metabolicResponse);

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
                        if (isCreatingNewPatient) {
                            // If we're creating a new patient, we need to find the patient's ID
                            // Get all patients for this doctor
                            List<User> doctorPatients = dbHelper.getPatientsForDoctor(creatingDoctorId);

                            // Find the patient with the matching CNP
                            String patientId = null;
                            for (User patient : doctorPatients) {
                                if (patient.getUserProfile() != null && 
                                    cnp.equals(patient.getUserProfile().getCnp())) {
                                    patientId = patient.getUserId();
                                    break;
                                }
                            }

                            // If we found the patient, update their profile
                            if (patientId != null) {
                                dbHelper.insertOrUpdateProfile(patientId, userProfile);
                            } else {
                                // If we didn't find the patient, log an error
                                Log.e("ProfileSetupActivity", "Could not find patient with CNP: " + cnp);
                            }
                        } else if (isEditingPatientProfile) {
                            // If we're editing a patient's profile, update the patient's profile
                            dbHelper.insertOrUpdateProfile(viewingPatientId, userProfile);
                        } else {
                            // Otherwise, update the current user's profile
                            dbHelper.insertOrUpdateProfile(finalUserId, userProfile);
                        }

                        editor.apply();

                        dialog.dismiss();

                        // Clear saved form data since submission was successful
                        clearSavedFormData();

                        // If we're creating a new patient, go back to the DoctorDashboardActivity
                        if (isCreatingNewPatient) {
                            // Create intent to go back to DoctorDashboardActivity
                            Intent intent = new Intent(ProfileSetupActivity.this, DoctorDashboardActivity.class);

                            // Restore the doctor's ID in SharedPreferences
                            SharedPreferences doctorPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                            doctorPrefs.edit().putString("userId", creatingDoctorId).apply();

                            startActivity(intent);
                            finish();
                        } 
                        // If we're editing a patient's profile, go back to the DoctorPatientsActivity
                        else if (isEditingPatientProfile) {
                            // Create intent to go back to DoctorPatientsActivity
                            Intent intent = new Intent(ProfileSetupActivity.this, DoctorPatientsActivity.class);

                            // Pass the patient ID back to the DoctorPatientsActivity
                            intent.putExtra("patient_id", viewingPatientId);

                            startActivity(intent);
                            finish();
                        }
                        else {
                            // Otherwise, go to the DashboardActivity as usual
                            Intent intent = new Intent(ProfileSetupActivity.this, DashboardActivity.class);
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
