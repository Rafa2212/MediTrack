package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity that allows doctors to view and manage their patients' information and diseases.
 * This activity displays patient details, health metrics, and disease information.
 * Doctors can add new diseases using ICD-10 codes, which are validated against the doctor's specialty.
 * The activity also provides functionality to view medical reports and edit patient profiles.
 * It uses OpenAI API to generate personalized health advice for patients based on their
 * profile and diagnosed diseases.
 */
public class DrPatientsActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private TextView patientName, patientAge, patientHeight, patientWeight, patientMedicalReport, patientHealthScore;
    private String currentPatientId;
    private static final String PREF_LAST_PATIENT_ID = "last_patient_id";
    private static final String TAG = "DoctorPatientsActivity";

    private LinearLayout patientSelectorLayout;
    private List<User> patientsList;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseasesList;

    private MaterialAutoCompleteTextView editTextICD10;
    private static final OkHttpClient client = new OkHttpClient();
    private ICDSearchAdapter searchAdapter;
    private final List<ICDSearchResult> searchResults = new ArrayList<>();

    /**
     * Initializes the activity, sets up UI components, and loads patient data.
     * This method performs several key operations:
     * - Sets up the activity layout and window transitions
     * - Initializes UI components including text views, buttons, and recycler views
     * - Configures the ICD-10 search functionality with auto-complete
     * - Sets up the disease adapter with click listeners for disease removal
     * - Configures the bottom navigation bar for doctor users
     * - Loads the list of patients assigned to the doctor
     * - Selects and displays the appropriate patient information
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_doctor_patients);

        dbHelper = DatabaseHelper.getInstance(this);

        patientAge = findViewById(R.id.patient_age);
        patientHeight = findViewById(R.id.patient_height);
        patientWeight = findViewById(R.id.patient_weight);
        patientMedicalReport = findViewById(R.id.patient_medical_report);
        patientHealthScore = findViewById(R.id.patient_health_score);
        RecyclerView diseasesRecyclerView = findViewById(R.id.diseases_recyclerview);
        Button viewMedicalReportsButton = findViewById(R.id.view_medical_reports_button);
        patientSelectorLayout = findViewById(R.id.patient_selector_layout);

        editTextICD10 = findViewById(R.id.editTextICD10Code);
        Button saveDiseaseButton = findViewById(R.id.saveDiseaseButton);

        SharedPreferences prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = prefs.getString("userId", "");

        searchAdapter = new ICDSearchAdapter(this, searchResults);
        editTextICD10.setAdapter(searchAdapter);

        editTextICD10.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String query = s.toString().trim();

                if (query.length() >= 2) {
                    searchRunnable = () -> searchIcd10Codes(query);
                    handler.postDelayed(searchRunnable, 300); // 300ms delay
                }
            }
        });

        editTextICD10.setOnItemClickListener((parent, view, position, id) -> {
            ICDSearchResult selectedResult = searchAdapter.getItem(position);
            if (selectedResult != null) {
                editTextICD10.setText(selectedResult.getCode());
            }
        });

        saveDiseaseButton.setOnClickListener(v -> {
            try {
                saveDisease();
            } catch (IOException e) {
                Log.e(TAG, "Error saving disease: " + e.getMessage());
                Snackbar.make(findViewById(android.R.id.content),
                        "Error saving disease", Snackbar.LENGTH_SHORT).show();
            }
        });

        diseasesList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(this, diseasesList, disease -> {
            if (currentPatientId != null && !currentPatientId.isEmpty()) {
                SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                String doctorId1 = preferences.getString("userId", "");

                if (doctorId1.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Doctor ID not found!",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String checkQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USER_DISEASES +
                        " WHERE " + DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND " +
                        DatabaseHelper.COLUMN_DISEASE_ID_FK + "=? AND " +
                        DatabaseHelper.COLUMN_DOCTOR_ID_FK + "=?";
                String[] checkArgs = {currentPatientId, String.valueOf(disease.getDiseaseId()), doctorId1};
                Cursor cursor = db.rawQuery(checkQuery, checkArgs);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.close();

                    String whereClause = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND "
                            + DatabaseHelper.COLUMN_DISEASE_ID_FK + "=? AND "
                            + DatabaseHelper.COLUMN_DOCTOR_ID_FK + "=?";
                    String[] whereArgs = {currentPatientId, String.valueOf(disease.getDiseaseId()), doctorId1};
                    int deletedRows = db.delete(DatabaseHelper.TABLE_USER_DISEASES, whereClause, whereArgs);

                    if (deletedRows > 0) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Disease removed from the patient",
                                Snackbar.LENGTH_SHORT).show();
                        loadDiseases(currentPatientId);
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Failed to remove disease!",
                                Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    if (cursor != null) {
                        cursor.close();
                    }

                    Snackbar.make(findViewById(android.R.id.content),
                            "You can only delete diseases that you have added!",
                            Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "No patient selected!",
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diseasesRecyclerView.setAdapter(diseaseAdapter);

        viewMedicalReportsButton.setOnClickListener(v -> {
            if (currentPatientId != null && !currentPatientId.isEmpty()) {
                Intent intent = new Intent(DrPatientsActivity.this, DrReportsActivity.class);
                intent.putExtra("patient_id", currentPatientId);
                startActivity(intent);
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "No patient selected!",
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_patients);

        String patientId = getIntent().getStringExtra("patient_id");
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);

        patientsList = dbHelper.getPatientsForDoctor(doctorId);

        setupPatientSelector();

        if (patientId == null || patientId.isEmpty()) {
            patientId = preferences.getString(PREF_LAST_PATIENT_ID, "");

            if (patientId.isEmpty() && patientsList != null && !patientsList.isEmpty()) {
                patientId = patientsList.get(0).getUserId();
            }
        }

        if (patientId != null && !patientId.isEmpty()) {
            User patient = dbHelper.getUser(patientId);

            if (patient != null && patient.getUserProfile() != null) {
                preferences.edit().putString(PREF_LAST_PATIENT_ID, patientId).apply();

                currentPatientId = patientId;

                displayPatientInfo(patient);
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Patient information not found!",
                        Snackbar.LENGTH_SHORT).show();

                if (patientsList != null && !patientsList.isEmpty()) {
                    User firstPatient = patientsList.get(0);
                    currentPatientId = firstPatient.getUserId();
                    preferences.edit().putString(PREF_LAST_PATIENT_ID, currentPatientId).apply();
                    displayPatientInfo(firstPatient);
                }
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "No patients available!",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up the patient selector layout.
     * This method configures the visibility of the patient selector layout
     * and displays a Snackbar message if no patients are assigned to the doctor.
     */
    private void setupPatientSelector() {
        patientSelectorLayout.setVisibility(View.GONE);

        if (patientsList == null || patientsList.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "No patients assigned to you!",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays the patient's information in the UI.
     * This method populates the UI with the patient's details including:
     * - Age, height, and weight
     * - Latest medical report date (formatted)
     * - Health score with appropriate color coding
     * - Patient name
     * It also sets up transition names for shared element transitions,
     * configures the profile card click listener to open the profile setup activity,
     * and loads the patient's diseases.
     *
     * @param patient The User object containing the patient's information to display
     */
    @SuppressLint("SetTextI18n")
    private void displayPatientInfo(User patient) {
        if (patient != null && patient.getUserProfile() != null) {
            UserProfile profile = patient.getUserProfile();
            patientAge.setText(String.valueOf(profile.getAge()));

            float height = profile.getHeight();
            if (height == Math.floor(height)) {
                patientHeight.setText(String.valueOf((int)height));
            } else {
                patientHeight.setText(String.valueOf(height));
            }

            float weight = profile.getWeight();
            if (weight == Math.floor(weight)) {
                patientWeight.setText(String.valueOf((int)weight));
            } else {
                patientWeight.setText(String.valueOf(weight));
            }
            MedicalReport latestReport = dbHelper.getLatestMedicalReportForPatient(patient.getUserId());
            if (latestReport != null && latestReport.getReportDate() != null && !latestReport.getReportDate().isEmpty()) {
                String lastReportDate = latestReport.getReportDate();
                String formattedDate = "";
                try {
                    java.time.format.DateTimeFormatter inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(lastReportDate, inputFormatter);
                    java.time.format.DateTimeFormatter outputFormatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH);
                    formattedDate = dateTime.format(outputFormatter);
                    patientMedicalReport.setText("Last Medical Report: " + formattedDate);
                } catch (Exception e) {
                    patientMedicalReport.setText("Last Medical Report: " + lastReportDate);
                }
            } else {
                patientMedicalReport.setText("");
            }

            int healthScore = profile.getHealthScore();
            patientHealthScore.setText(healthScore + "/100");

            if (healthScore >= 80) {
                patientHealthScore.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (healthScore >= 60) {
                patientHealthScore.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                patientHealthScore.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            patientAge.setTransitionName("patient_age_" + patient.getUserId());
            patientMedicalReport.setTransitionName("patient_report_" + patient.getUserId());

            TextView patientTitle = findViewById(R.id.patient_title);
            patientTitle.setText(profile.getName());

            View profileCard = findViewById(R.id.profile_card);
            profileCard.setOnClickListener(v -> {
                Intent intent = new Intent(DrPatientsActivity.this, DrProfileActivity.class);
                intent.putExtra("view_patient_profile", true);
                intent.putExtra("edit_patient_profile", true);
                intent.putExtra("patient_id", patient.getUserId());
                startActivity(intent);
            });

            loadDiseases(patient.getUserId());
        }
    }

    /**
     * Saves a new disease to the patient's record.
     * This method performs several operations:
     * 1. Validates that a patient is selected and an ICD-10 code is entered
     * 2. Checks if the patient already has the disease
     * 3. Validates that the ICD-10 code is appropriate for the doctor's specialty
     * 4. Verifies the ICD-10 code is valid using an external API
     * 5. Saves the disease to the database
     * 6. Uses OpenAI to generate personalized health advice based on the patient's profile and disease
     * 7. Stores the generated advice and updates the UI
     * 
     * The method shows appropriate feedback messages to the user throughout the process.
     *
     * @throws IOException If there is an error communicating with the external API
     */
    @SuppressLint("SetTextI18n")
    private void saveDisease() throws IOException {
        if (currentPatientId == null || currentPatientId.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), 
                    "No patient selected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String icd10Code = editTextICD10.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER_DISEASES + " ud "
                + "INNER JOIN " + DatabaseHelper.TABLE_DISEASES + " d "
                + "ON ud." + DatabaseHelper.COLUMN_DISEASE_ID_FK + " = d."
                + DatabaseHelper.COLUMN_DISEASE_ID + " WHERE (d."
                + DatabaseHelper.COLUMN_ICD10 + " =?)"
                + " AND ud." + DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + " =?";

        String[] selectionArgs = {icd10Code, currentPatientId};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (icd10Code.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please enter ICD-10 code", Snackbar.LENGTH_SHORT).show();
            return;
        } else {
            if (cursor.getCount() > 0) {
                Snackbar.make(findViewById(android.R.id.content), 
                        "The patient already has this disease!", Snackbar.LENGTH_SHORT).show();
            } else {
                SharedPreferences prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                String doctorId = prefs.getString("userId", "");

                if (!doctorId.isEmpty()) {
                    UserProfile doctorProfile = dbHelper.getUserProfile(doctorId);
                    String specialty = doctorProfile.getSpecialty();

                    if (!ICDSpecialityMapper.isCodeValidForSpecialty(icd10Code, specialty)) {
                        Snackbar.make(
                                findViewById(android.R.id.content), 
                                "This ICD-10 code is not valid for your specialty (" + specialty + "). ",
                                Snackbar.LENGTH_LONG
                        ).show();
                        return;
                    }
                }

                isValidIcd10Code(icd10Code, (isValid, disease) -> {
                    if (!isValid) {
                        Snackbar.make(findViewById(android.R.id.content), 
                                "The ICD-10 code is invalid or incomplete, please verify.", 
                                Snackbar.LENGTH_LONG).show();
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION, disease);
                        values.put(DatabaseHelper.COLUMN_ICD10, icd10Code);

                        long newRowId = db.insert(DatabaseHelper.TABLE_DISEASES, null, values);

                        ContentValues userDiseaseValues = new ContentValues();
                        userDiseaseValues.put(DatabaseHelper.COLUMN_USER_ID_FK_DISEASE, currentPatientId);
                        userDiseaseValues.put(DatabaseHelper.COLUMN_DISEASE_ID_FK, newRowId);
                        if (!doctorId.isEmpty()) {
                            userDiseaseValues.put(DatabaseHelper.COLUMN_DOCTOR_ID_FK, doctorId);
                        }

                        String currentTimestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                        userDiseaseValues.put("diagnosis_date", currentTimestamp);

                        final Dialog dialog = new Dialog(DrPatientsActivity.this);
                        dialog.setContentView(R.layout.custom_dialog);
                        dialog.findViewById(R.id.progress);
                        TextView textView = dialog.findViewById(R.id.text);
                        textView.setText("Saving disease...");
                        dialog.setCancelable(false);
                        dialog.show();

                        boolean exists = false;

                        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                        Map<String, ?> allEntries = preferences.getAll();
                        if (allEntries != null) {
                            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                                try {
                                    String entryKey = entry.getKey();
                                    if (entryKey == null) continue;

                                    String[] key = entryKey.split("#");
                                    if (key.length >= 2 && Objects.equals(key[0], "Disease") && Objects.equals(key[1], icd10Code)) {
                                        exists = true;
                                        break;
                                    }
                                } catch (Exception e) {
                                    Log.e("DoctorPatientsActivity", "Error processing entry", e);
                                }
                            }
                        }

                        if (!exists) {
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());
                            OpenAiService service = new OpenAiService(TokenData.OPEN_AI_SERVICE_KEY.getToken());

                            executor.execute(() -> {
                                try {
                                    UserProfile userProfile = dbHelper.getUserProfile(currentPatientId);

                                    String prompt = userProfile.getName() + " is a " + userProfile.getAge() + " year old individual with a height of " + userProfile.getHeight() + " cm and a weight of " + userProfile.getWeight() + " kg. They have been diagnosed with a disease coded as " + icd10Code + " (ICD10). The patient has been prescribed a treatment and medication by their doctor.";
                                    prompt += "\n\n Please provide a short summary on cautions and advice specific to the patient's profile:";
                                    prompt += "\n1. Give general advice on how to take the prescribed medication.";
                                    prompt += "\n2. Mention any cautions related to alcohol consumption, considering the patient's age (" + userProfile.getAge() + " years old).";
                                    prompt += "\n3. Provide diet recommendations considering the patient's weight (" + userProfile.getWeight() + " kg) and height (" + userProfile.getHeight() + " cm).";
                                    prompt += "\n4. Suggest general lifestyle adjustments like staying hydrated, avoiding smoking, and suitable physical activities, keeping in mind their diagnosed condition and overall health.";
                                    prompt += "\n5. Provide any other relevant advice or cautions based on the given profile.";

                                    ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                                            .model("gpt-3.5-turbo")
                                            .messages(Collections.singletonList(
                                                    new ChatMessage("user", prompt)
                                            ))
                                            .build();

                                    ChatCompletionResult result = service.createChatCompletion(completionRequest);

                                    String diseaseResponse =
                                            result.getChoices().get(0).getMessage().getContent().replace('*', ' ').replace(
                                                    '#', ' ');
                                    String key = "Disease#" + icd10Code + "#" + disease;
                                    long diseaseId = dbHelper.insertOnSession(currentPatientId, key, diseaseResponse);

                                    SharedPreferences.Editor editor =
                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                                    editor.putString(key, String.valueOf(diseaseId));
                                    editor.apply();

                                    db.insert(DatabaseHelper.TABLE_USER_DISEASES, null, userDiseaseValues);

                                    handler.post(() -> {
                                        dialog.dismiss();
                                        loadDiseases(currentPatientId);
                                        editTextICD10.setText("");
                                        Snackbar.make(findViewById(android.R.id.content), 
                                                "Disease added successfully", Snackbar.LENGTH_SHORT).show();
                                    });

                                } catch (Exception e) {
                                    handler.post(() -> {
                                        dialog.dismiss();
                                        Snackbar.make(findViewById(android.R.id.content), 
                                                "Saving failed! Try again later!", Snackbar.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), 
                                    "The ICD-10 exists!", Snackbar.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                });
            }
        }

        cursor.close();
    }

    /**
     * Interface for receiving ICD-10 code validation results.
     * This callback interface is used to handle the asynchronous results
     * of ICD-10 code validation, providing both the validation status
     * and the associated disease name if the code is valid.
     */
    public interface Icd10CodeValidationCallback {
        /**
         * Called when the validation result is available.
         *
         * @param isValid True if the ICD-10 code is valid, false otherwise
         * @param diseaseName The name of the disease associated with the code if valid, empty string otherwise
         */
        void onResultReceived(boolean isValid, String diseaseName);
    }

    /**
     * Searches for ICD-10 codes based on the user's input.
     * This method performs an asynchronous search using the NLM Clinical Tables API,
     * retrieving ICD-10 codes that match the query. The search results are limited
     * to a maximum of 5 items and include both the code and disease name.
     * When results are received, the search adapter is updated and the dropdown
     * is displayed to the user.
     *
     * @param query The search query string (minimum 2 characters)
     */
    private void searchIcd10Codes(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String url = "https://clinicaltables.nlm.nih.gov/api/icd10cm/v3/search?sf=code,name&terms=" + query;
                Request request = new Request.Builder().url(url).get().build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String resultStr = Objects.requireNonNull(response.body()).string();
                    JSONArray result = new JSONArray(resultStr);

                    if (result.length() >= 4 && result.getInt(0) > 0) {
                        searchResults.clear();

                        JSONArray codes = result.getJSONArray(1);

                        JSONArray details = result.getJSONArray(3);

                        int resultCount = Math.min(5, codes.length());

                        for (int i = 0; i < resultCount; i++) {
                            String code = codes.getString(i);
                            JSONArray detailItem = details.getJSONArray(i);
                            String name = detailItem.getString(1);

                            searchResults.add(new ICDSearchResult(code, name));
                        }

                        handler.post(() -> {
                            searchAdapter.updateResults(searchResults);
                            searchAdapter.notifyDataSetChanged();

                            if (!editTextICD10.isPopupShowing()) {
                                editTextICD10.showDropDown();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching for ICD-10 codes", e);
            }
        });
    }

    /**
     * Validates an ICD-10 code using both regex pattern and an external API.
     * This method performs a two-step validation process:
     * 1. First checks if the code matches the expected ICD-10 format using regex
     * 2. Then verifies the code exists in the NLM Clinical Tables database
     * 
     * The validation is performed asynchronously, and the result is delivered
     * through the provided callback. If valid, the callback also receives the
     * disease name associated with the code.
     *
     * @param code The ICD-10 code to validate
     * @param callback The callback to receive the validation result and disease name
     */
    public void isValidIcd10Code(String code, Icd10CodeValidationCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        try {
            executor.execute(() -> {
                String regex = "^[A-Z][0-9]{2}(\\.[0-9]{1,4})?$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(code);
                if (!matcher.matches()) {
                    handler.post(() -> callback.onResultReceived(false, ""));
                    return;
                }

                String url = "https://clinicaltables.nlm.nih.gov/api/icd10cm/v3/search?sf=code,name&terms=" + code;

                Request request = new Request.Builder().url(url).get().build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    String resultStr = Objects.requireNonNull(response.body()).string();
                    JSONArray result = new JSONArray(resultStr);

                    if (result.length() >= 2 && result.getInt(0) > 0) {
                        boolean isValid = true;
                        String diseaseName = "";

                        if (result.length() >= 4) {
                            JSONArray diseaseInfo = result.getJSONArray(3);
                            if (diseaseInfo.length() > 0) {
                                JSONArray diseaseNameInfo = diseaseInfo.getJSONArray(0);
                                if (diseaseNameInfo.length() >= 2) {
                                    diseaseName = diseaseNameInfo.getString(1);
                                } else if (diseaseNameInfo.length() >= 1) {
                                    diseaseName = "Disease: " + code;
                                }
                            }
                        }

                        if (diseaseName.isEmpty()) {
                            diseaseName = "Disease: " + code;
                        }

                        final String finalDiseaseName = diseaseName;
                        handler.post(() -> callback.onResultReceived(isValid, finalDiseaseName));
                    } else {
                        handler.post(() -> callback.onResultReceived(false, ""));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Validation failed", e);
                    handler.post(() -> callback.onResultReceived(false, ""));
            }
            });
        }
        catch (Exception e){
            Log.e(TAG, "Error when executing the task", e);
        }
    }

    /**
     * Loads the diseases for a specific patient and updates the UI.
     * This method clears the current diseases list, retrieves all diseases
     * associated with the specified patient from the database, adds them to
     * the list, and notifies the adapter to refresh the UI.
     *
     * @param patientId The ID of the patient whose diseases to load
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadDiseases(String patientId) {
        diseasesList.clear();

        List<Disease> diseases = dbHelper.getDiseasesForPatient(patientId);
        diseasesList.addAll(diseases);

        diseaseAdapter.notifyDataSetChanged();
    }
}
