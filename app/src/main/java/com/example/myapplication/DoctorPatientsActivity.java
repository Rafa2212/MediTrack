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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoctorPatientsActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private TextView patientName, patientAge, patientHeight, patientWeight, patientMedicalReport, patientHealthScore;
    private RecyclerView diseasesRecyclerView;
    private Button viewMedicalReportsButton;
    private String currentPatientId;
    private static final String PREF_LAST_PATIENT_ID = "last_patient_id";
    private static final String TAG = "DoctorPatientsActivity";

    private LinearLayout patientSelectorLayout;
    private Spinner patientSelector;
    private List<User> patientsList;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseasesList;

    private MaterialAutoCompleteTextView editTextICD10;
    private Button saveDiseaseButton;
    private static final OkHttpClient client = new OkHttpClient();
    private Icd10SearchAdapter searchAdapter;
    private List<Icd10SearchResult> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable window content transitions
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_doctor_patients);

        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        //patientName = findViewById(R.id.patient_name);
        patientAge = findViewById(R.id.patient_age);
        patientHeight = findViewById(R.id.patient_height);
        patientWeight = findViewById(R.id.patient_weight);
        patientMedicalReport = findViewById(R.id.patient_medical_report);
        patientHealthScore = findViewById(R.id.patient_health_score);
        diseasesRecyclerView = findViewById(R.id.diseases_recyclerview);
        viewMedicalReportsButton = findViewById(R.id.view_medical_reports_button);
        patientSelectorLayout = findViewById(R.id.patient_selector_layout);
        patientSelector = findViewById(R.id.patient_selector);

        // Initialize disease addition UI elements
        editTextICD10 = findViewById(R.id.editTextICD10Code);
        saveDiseaseButton = findViewById(R.id.saveDiseaseButton);
        TextView specialtyInfoTextView = findViewById(R.id.specialtyInfoTextView);

        // Check if the current user is a doctor and display specialty information
        SharedPreferences prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String doctorId = prefs.getString("userId", "");
//        if (!doctorId.isEmpty() && dbHelper.isDoctor(doctorId)) {
//            UserProfile doctorProfile = dbHelper.getUserProfile(doctorId);
//            String specialty = doctorProfile.getSpecialty();
//
//            if (specialty != null && !specialty.isEmpty()) {
//                String validCodesDescription = ICD10SpecialtyMapper.getValidCodesDescription(specialty);
//                specialtyInfoTextView.setText("Your specialty: " + specialty + "\n" +
//                        "You can only assign: " + validCodesDescription);
//                specialtyInfoTextView.setVisibility(View.VISIBLE);
//            }
//        }

        // Initialize search adapter
        searchAdapter = new Icd10SearchAdapter(this, searchResults);
        editTextICD10.setAdapter(searchAdapter);

        // Set up text change listener to trigger search
        editTextICD10.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel any pending searches
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String query = s.toString().trim();

                // Only search if we have at least 2 characters
                if (query.length() >= 2) {
                    // Delay the search to avoid too many API calls while typing
                    searchRunnable = () -> searchIcd10Codes(query);
                    handler.postDelayed(searchRunnable, 300); // 300ms delay
                }
            }
        });

        // Set up item click listener
        editTextICD10.setOnItemClickListener((parent, view, position, id) -> {
            Icd10SearchResult selectedResult = searchAdapter.getItem(position);
            if (selectedResult != null) {
                editTextICD10.setText(selectedResult.getCode());
            }
        });

        // Set up save button click listener
        saveDiseaseButton.setOnClickListener(v -> {
            try {
                saveDisease();
            } catch (IOException e) {
                Log.e(TAG, "Error saving disease: " + e.getMessage());
                Snackbar.make(findViewById(android.R.id.content),
                        "Error saving disease", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Initialize diseases list and adapter
        diseasesList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(this, diseasesList, new DiseaseAdapter.OnDiseaseActionListener() {
            @Override
            public void onDeleteDisease(Disease disease) {
                if (currentPatientId != null && !currentPatientId.isEmpty()) {
                    // Get current doctor ID from SharedPreferences
                    SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                    String doctorId = preferences.getString("userId", "");

                    if (doctorId.isEmpty()) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Doctor ID not found!",
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // First check if the current doctor is the one who added the disease
                    String checkQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USER_DISEASES + 
                            " WHERE " + DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND " +
                            DatabaseHelper.COLUMN_DISEASE_ID_FK + "=? AND " +
                            DatabaseHelper.COLUMN_DOCTOR_ID_FK + "=?";
                    String[] checkArgs = {currentPatientId, String.valueOf(disease.getDiseaseId()), doctorId};
                    Cursor cursor = db.rawQuery(checkQuery, checkArgs);

                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.close();

                        // The current doctor added this disease, so they can delete it
                        String whereClause = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND "
                                + DatabaseHelper.COLUMN_DISEASE_ID_FK + "=? AND "
                                + DatabaseHelper.COLUMN_DOCTOR_ID_FK + "=?";
                        String[] whereArgs = {currentPatientId, String.valueOf(disease.getDiseaseId()), doctorId};
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

                        // The current doctor did not add this disease, so they cannot delete it
                        Snackbar.make(findViewById(android.R.id.content),
                                "You can only delete diseases that you have added!",
                                Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "No patient selected!",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // Set up diseases RecyclerView
        diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diseasesRecyclerView.setAdapter(diseaseAdapter);

        // Set up medical reports button
        viewMedicalReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPatientId != null && !currentPatientId.isEmpty()) {
                    Intent intent = new Intent(DoctorPatientsActivity.this, MedicalReportsActivity.class);
                    intent.putExtra("patient_id", currentPatientId);
                    startActivity(intent);
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "No patient selected!",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupDoctorNavigation(bottomNav, R.id.menu_patients);

        // Get patient ID from intent or SharedPreferences
        String patientId = getIntent().getStringExtra("patient_id");
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);

        // Get current doctor ID
        //String doctorId = preferences.getString("userId", "");

        // Get patients for this doctor
        patientsList = dbHelper.getPatientsForDoctor(doctorId);

        // Set up patient selector
        setupPatientSelector();

        // If no patient ID in intent, try to get from SharedPreferences
        if (patientId == null || patientId.isEmpty()) {
            patientId = preferences.getString(PREF_LAST_PATIENT_ID, "");

            // If still no patient ID, select the first patient in the list
            if ((patientId == null || patientId.isEmpty()) && patientsList != null && !patientsList.isEmpty()) {
                patientId = patientsList.get(0).getUserId();
            }
        }

        if (patientId != null && !patientId.isEmpty()) {
            // Get patient information from database
            User patient = dbHelper.getUser(patientId);

            if (patient != null && patient.getUserProfile() != null) {
                // Save this patient ID as the last viewed
                preferences.edit().putString(PREF_LAST_PATIENT_ID, patientId).apply();

                // Store the current patient ID
                currentPatientId = patientId;

                // Display patient information
                displayPatientInfo(patient);
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Patient information not found!",
                        Snackbar.LENGTH_SHORT).show();

                // If patient not found and we have other patients, select the first one
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

    private void setupPatientSelector() {
        // Always hide the patient selector layout
        patientSelectorLayout.setVisibility(View.GONE);

        if (patientsList == null || patientsList.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "No patients assigned to you!",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        // The rest of this method is kept for compatibility but not used
        // since we're always hiding the selector and auto-selecting the first patient
    }

    private void displayPatientInfo(User patient) {
        if (patient != null && patient.getUserProfile() != null) {
            UserProfile profile = patient.getUserProfile();
            //patientName.setText(profile.getName());
            patientAge.setText(String.valueOf(profile.getAge()));

            // Format height without decimal if it's a whole number
            float height = profile.getHeight();
            if (height == Math.floor(height)) {
                patientHeight.setText(String.valueOf((int)height));
            } else {
                patientHeight.setText(String.valueOf(height));
            }

            // Format weight without decimal if it's a whole number
            float weight = profile.getWeight();
            if (weight == Math.floor(weight)) {
                patientWeight.setText(String.valueOf((int)weight));
            } else {
                patientWeight.setText(String.valueOf(weight));
            }
            // Get the latest medical report date when the patient actually logged data
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
                    patientMedicalReport.setText("Last Medical Report: " + lastReportDate); // Fallback to original string if parsing fails
                }
            } else {
                patientMedicalReport.setText("");
            }

            // Set health score
            int healthScore = profile.getHealthScore();
            patientHealthScore.setText(healthScore + "/100");

            // Set color based on health score
            if (healthScore >= 80) {
                patientHealthScore.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (healthScore >= 60) {
                patientHealthScore.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                patientHealthScore.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Set transition names for shared elements
            //patientName.setTransitionName("patient_name_" + patient.getUserId());
            patientAge.setTransitionName("patient_age_" + patient.getUserId());
            patientMedicalReport.setTransitionName("patient_report_" + patient.getUserId());

            // Update title with patient name
            TextView patientTitle = findViewById(R.id.patient_title);
            patientTitle.setText(profile.getName());

            // Make profile card clickable
            View profileCard = findViewById(R.id.profile_card);
            profileCard.setOnClickListener(v -> {
                // Navigate to ProfileSetupActivity with patient information
                Intent intent = new Intent(DoctorPatientsActivity.this, ProfileSetupActivity.class);
                intent.putExtra("view_patient_profile", true);
                intent.putExtra("edit_patient_profile", true); // Allow editing the patient profile
                intent.putExtra("patient_id", patient.getUserId());
                startActivity(intent);
            });

            // Load diseases for this patient
            loadDiseases(patient.getUserId());
        }
    }

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
                // Get current doctor ID from SharedPreferences
                SharedPreferences prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                String doctorId = prefs.getString("userId", "");

                // Check if the ICD-10 code is valid for the doctor's specialty
                if (!doctorId.isEmpty()) {
                    UserProfile doctorProfile = dbHelper.getUserProfile(doctorId);
                    String specialty = doctorProfile.getSpecialty();

                    // Check if the ICD-10 code is valid for the doctor's specialty
                    if (!ICD10SpecialtyMapper.isCodeValidForSpecialty(icd10Code, specialty)) {
                        String validCodesDescription = ICD10SpecialtyMapper.getValidCodesDescription(specialty);
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

                        // Add current timestamp
                        String currentTimestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                        userDiseaseValues.put("diagnosis_date", currentTimestamp);

                        final Dialog dialog = new Dialog(DoctorPatientsActivity.this);
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
                                            .messages(Arrays.asList(
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

    public interface Icd10CodeValidationCallback {
        void onResultReceived(boolean isValid, String diseaseName);
    }

    /**
     * Search for ICD-10 codes based on the user's input
     * @param query The search query
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

                    // Check if the response indicates at least one result was found
                    if (result.length() >= 4 && result.getInt(0) > 0) {
                        // Clear previous results
                        searchResults.clear();

                        // Get the codes array
                        JSONArray codes = result.getJSONArray(1);

                        // Get the details array
                        JSONArray details = result.getJSONArray(3);

                        // Limit to 5 results
                        int resultCount = Math.min(5, codes.length());

                        for (int i = 0; i < resultCount; i++) {
                            String code = codes.getString(i);
                            JSONArray detailItem = details.getJSONArray(i);
                            String name = detailItem.getString(1);

                            searchResults.add(new Icd10SearchResult(code, name));
                        }

                        // Update the adapter on the main thread
                        handler.post(() -> {
                            searchAdapter.updateResults(searchResults);
                            searchAdapter.notifyDataSetChanged();

                            // Show the dropdown if it's not already showing
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

                    // Check if the response indicates at least one result was found
                    if (result.length() >= 2 && result.getInt(0) > 0) {
                        boolean isValid = true;
                        String diseaseName = "";

                        // Try to extract the disease name from the response
                        if (result.length() >= 4) {
                            JSONArray diseaseInfo = result.getJSONArray(3);
                            if (diseaseInfo.length() > 0) {
                                JSONArray diseaseNameInfo = diseaseInfo.getJSONArray(0);
                                if (diseaseNameInfo.length() >= 2) {
                                    diseaseName = diseaseNameInfo.getString(1);
                                } else if (diseaseNameInfo.length() >= 1) {
                                    // Fallback to using the code as the name if no name is provided
                                    diseaseName = "Disease: " + code;
                                }
                            }
                        }

                        // If we couldn't extract a name but the code is valid, use a default name
                        if (diseaseName.isEmpty()) {
                            diseaseName = "Disease: " + code;
                        }

                        final String finalDiseaseName = diseaseName;
                        handler.post(() -> callback.onResultReceived(isValid, finalDiseaseName));
                    } else {
                        // No results found, code is invalid
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

    @SuppressLint("NotifyDataSetChanged")
    private void loadDiseases(String patientId) {
        diseasesList.clear();

        // Use the new method to get all diseases for the patient, including those added by all doctors
        List<Disease> diseases = dbHelper.getDiseasesForPatient(patientId);
        diseasesList.addAll(diseases);

        diseaseAdapter.notifyDataSetChanged();
    }
}
