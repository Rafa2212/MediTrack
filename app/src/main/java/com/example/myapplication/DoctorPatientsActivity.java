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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
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

    private TextInputEditText editTextICD10;
    private Button saveDiseaseButton;
    private static final OkHttpClient client = new OkHttpClient();

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
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    String whereClause = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND "
                            + DatabaseHelper.COLUMN_DISEASE_ID_FK + "=?";
                    String[] whereArgs = {currentPatientId, String.valueOf(disease.getDiseaseId())};
                    int deletedRows = db.delete(DatabaseHelper.TABLE_USER_DISEASES, whereClause, whereArgs);

                    if (deletedRows > 0) {
                        Toast.makeText(DoctorPatientsActivity.this, "Disease removed from patient", Toast.LENGTH_SHORT).show();
                        loadDiseases(currentPatientId);
                    } else {
                        Toast.makeText(DoctorPatientsActivity.this, "Failed to remove disease", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DoctorPatientsActivity.this, "No patient selected", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DoctorPatientsActivity.this, "No patient selected", Toast.LENGTH_SHORT).show();
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
        String doctorId = preferences.getString("userId", "");

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
                Toast.makeText(this, "Patient information not found", Toast.LENGTH_SHORT).show();

                // If patient not found and we have other patients, select the first one
                if (patientsList != null && !patientsList.isEmpty()) {
                    User firstPatient = patientsList.get(0);
                    currentPatientId = firstPatient.getUserId();
                    preferences.edit().putString(PREF_LAST_PATIENT_ID, currentPatientId).apply();
                    displayPatientInfo(firstPatient);
                }
            }
        } else {
            Toast.makeText(this, "No patients available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPatientSelector() {
        // Always hide the patient selector layout
        patientSelectorLayout.setVisibility(View.GONE);

        if (patientsList == null || patientsList.isEmpty()) {
            Toast.makeText(this, "No patients assigned to you", Toast.LENGTH_SHORT).show();
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
                        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                            String[] key = entry.getKey().split("#");
                            if (Objects.equals(key[0], "Disease") && Objects.equals(key[1], icd10Code)) {
                                exists = true;
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
                                        editTextICD10.getText().clear();
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

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=?";
        String[] selectionArgs = {patientId};
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_DISEASES, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int diseaseId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_ID_FK));

                String diseaseSelection = DatabaseHelper.COLUMN_DISEASE_ID + "=?";
                String[] diseaseSelectionArgs = {String.valueOf(diseaseId)};
                Cursor diseaseCursor = db.query(DatabaseHelper.TABLE_DISEASES, null, diseaseSelection,
                        diseaseSelectionArgs, null, null, null);

                if (diseaseCursor != null && diseaseCursor.moveToFirst()) {
                    @SuppressLint("Range")
                    String description = diseaseCursor.getString(
                            diseaseCursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION));
                    @SuppressLint("Range")
                    String icd10 =
                            diseaseCursor.getString(diseaseCursor.getColumnIndex(DatabaseHelper.COLUMN_ICD10));

                    Disease disease = new Disease(diseaseId, description, icd10);
                    diseasesList.add(disease);
                }
                if (diseaseCursor != null) {
                    diseaseCursor.close();
                }
            }
            cursor.close();

            diseaseAdapter.notifyDataSetChanged();
        }
    }
}
