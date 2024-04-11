package com.example.myapplication;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddDiseaseActivity extends AppCompatActivity implements DiseaseAdapter.OnDiseaseActionListener {

    private EditText editTextDiseaseName;
    private EditText editTextICD10;
    DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;
    private DatabaseHelper dbHelper;
    private String CURRENT_USER_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_disease);
        CURRENT_USER_ID = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("userId", "default_value");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_diseases);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_dashboard) {
                Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.move);
                getWindow().setSharedElementExitTransition(fade);

                Intent intent = new Intent(this, DashboardActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(this, bottomNav, "bottomNavTransition");
                startActivity(intent, options.toBundle());
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

        dbHelper = new DatabaseHelper(this);

        editTextDiseaseName = findViewById(R.id.editTextDiseaseName);
        editTextICD10 = findViewById(R.id.editTextICD10Code);

        Button saveButton = findViewById(R.id.saveDiseaseButton);
        saveButton.setOnClickListener(v -> saveDisease());

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDiseases);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        diseaseList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(AddDiseaseActivity.this, diseaseList, this);
        recyclerView.setAdapter(diseaseAdapter);

        loadDiseases();
    }

    private void saveDisease() {
        String diseaseName = editTextDiseaseName.getText().toString().trim();
        String icd10Code = editTextICD10.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER_DISEASES + " ud "
                + "INNER JOIN " + DatabaseHelper.TABLE_DISEASES + " d "
                + "ON ud." + DatabaseHelper.COLUMN_DISEASE_ID_FK + " = d." + DatabaseHelper.COLUMN_DISEASE_ID
                + " WHERE (d." + DatabaseHelper.COLUMN_DISEASE_DESCRIPTION + " =? OR d." + DatabaseHelper.COLUMN_ICD10 + " =?)"
                + " AND ud." + DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + " =?";

        String[] selectionArgs = { diseaseName, icd10Code, String.valueOf(CURRENT_USER_ID) };
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (diseaseName.isEmpty() || icd10Code.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter both disease name and ICD-10 code", Snackbar.LENGTH_SHORT).show();
            return;
        }
        else{
            if (cursor.getCount() > 0) {
                Snackbar.make(findViewById(android.R.id.content), "The user already has this disease!", Snackbar.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION, diseaseName);
                values.put(DatabaseHelper.COLUMN_ICD10, icd10Code);

                long newRowId = db.insert(DatabaseHelper.TABLE_DISEASES, null, values);

                if (newRowId != -1) {
                    ContentValues userDiseaseValues = new ContentValues();
                    userDiseaseValues.put(DatabaseHelper.COLUMN_USER_ID_FK_DISEASE, CURRENT_USER_ID);
                    userDiseaseValues.put(DatabaseHelper.COLUMN_DISEASE_ID_FK, newRowId);

                    long newUserDiseaseRowId = db.insert(DatabaseHelper.TABLE_USER_DISEASES, null, userDiseaseValues);

                    if (newUserDiseaseRowId != -1) {

                        final Dialog dialog = new Dialog(AddDiseaseActivity.this);

                        dialog.setContentView(R.layout.custom_dialog);

                        ProgressBar progressBar = dialog.findViewById(R.id.progress);
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

                                        UserProfile userProfile = dbHelper.getUserProfile(CURRENT_USER_ID);

                                        String prompt = userProfile.getAge() + " year old having " + userProfile.getHeight() + " cm " + " and " + userProfile.getWeight() + " kg and " + icd10Code + " ICD10 disease code. " +
                                                "Information that may help you in prescribing tratment: blood pressure is " + userProfile.getBloodPressure() + "mmHG and heartrate " + userProfile.getHeartrate() + "BPM.";

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
                                        String diseaseResponse = respMsg.getContent().get(0).getText().
                                                getValue().replace('*', ' ').replace('#', ' ');
                                        String key = "Disease#" + icd10Code;
                                        long diseaseId = dbHelper.insertOnSession(CURRENT_USER_ID, key, diseaseResponse);

                                        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                                        editor.putString(key, String.valueOf(diseaseId));
                                        editor.apply();

                                        dialog.dismiss();

                                    } catch (Exception e) {
                                        handler.post(() -> {
                                            Snackbar.make(findViewById(android.R.id.content), "Saving failed! Try again later!", Snackbar.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            });

                            loadDiseases();
                            editTextDiseaseName.getText().clear();
                            editTextICD10.getText().clear();
                        }
                        else{
                            dialog.dismiss();
                        }
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add disease for the user!", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add disease!", Snackbar.LENGTH_SHORT).show();
                }
            }
        }

        cursor.close();
    }

    private void loadDiseases() {
        diseaseList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=?";
        String[] selectionArgs = { String.valueOf(CURRENT_USER_ID) };
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_DISEASES, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int diseaseId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_ID_FK));

                String diseaseSelection = DatabaseHelper.COLUMN_DISEASE_ID + "=?";
                String[] diseaseSelectionArgs = { String.valueOf(diseaseId) };
                Cursor diseaseCursor = db.query(DatabaseHelper.TABLE_DISEASES, null, diseaseSelection, diseaseSelectionArgs, null, null, null);

                if (diseaseCursor != null && diseaseCursor.moveToFirst()) {
                    @SuppressLint("Range") String description = diseaseCursor.getString(diseaseCursor.getColumnIndex(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION));
                    @SuppressLint("Range") String icd10 = diseaseCursor.getString(diseaseCursor.getColumnIndex(DatabaseHelper.COLUMN_ICD10));

                    Disease disease = new Disease(diseaseId, description, icd10);
                    diseaseList.add(disease);
                }
                if (diseaseCursor != null) {
                    diseaseCursor.close();
                }
            }
            cursor.close();

            diseaseAdapter.notifyDataSetChanged();
        }
    }

//    @Override
//    public void onEditDisease(Disease disease) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogLayout = inflater.inflate(R.layout.edit_disease_dialog, null);
//        EditText editTextDiseaseName = dialogLayout.findViewById(R.id.editTextDiseaseName);
//        EditText editTextICD10 = dialogLayout.findViewById(R.id.editTextICD10Code);
//
//        editTextDiseaseName.setText(disease.getName());
//        editTextICD10.setText(disease.getICD10());
//
//        builder.setView(dialogLayout)
//                .setPositiveButton("Update", (dialog, id) -> {
//                    String diseaseName = editTextDiseaseName.getText().toString().trim();
//                    String icd10Code = editTextICD10.getText().toString().trim();
//
//                    SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//                    ContentValues values = new ContentValues();
//                    values.put(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION, diseaseName);
//                    values.put(DatabaseHelper.COLUMN_ICD10, icd10Code);
//
//                    String whereClause = DatabaseHelper.COLUMN_DISEASE_ID + "=?";
//                    String[] whereArgs = { String.valueOf(disease.getDiseaseId()) };
//                    int updatedRows = db.update(DatabaseHelper.TABLE_DISEASES, values, whereClause, whereArgs);
//
//                    if (updatedRows > 0) {
//                        Snackbar.make(findViewById(android.R.id.content), "Disease updated successfully!", Snackbar.LENGTH_SHORT).show();
//                        loadDiseases();
//                    } else {
//                        Snackbar.make(findViewById(android.R.id.content), "Failed to update disease!", Snackbar.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", (dialog, id) -> {
//                });
//
//        builder.create().show();
//    }

    @Override
    public void onDeleteDisease(Disease disease) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND " + DatabaseHelper.COLUMN_DISEASE_ID_FK + "=?";
        String[] whereArgs = { String.valueOf(CURRENT_USER_ID), String.valueOf(disease.getDiseaseId()) };
        int deletedRows = db.delete(DatabaseHelper.TABLE_USER_DISEASES, whereClause, whereArgs);

        if (deletedRows > 0) {
            SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
            String key = "Disease#" + disease.getICD10();
            if (preferences.contains(key)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(key);
                editor.apply();
            }

            Snackbar.make(findViewById(android.R.id.content), "Disease deleted successfully!", Snackbar.LENGTH_SHORT).show();
            loadDiseases();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Failed to delete disease!", Snackbar.LENGTH_SHORT).show();
        }
    }
}
