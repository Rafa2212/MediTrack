package com.example.myapplication;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.*;
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

public class AddDiseaseActivity
        extends BaseActivity implements DiseaseAdapter.OnDiseaseActionListener {
    private EditText editTextICD10;
    DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;
    private DatabaseHelper dbHelper;
    private String CURRENT_USER_ID;
    private static final OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_disease);
        CURRENT_USER_ID =
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("userId", "default_value");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        setupNavigation(bottomNav, R.id.menu_diseases);

        dbHelper = new DatabaseHelper(this);

        editTextICD10 = findViewById(R.id.editTextICD10Code);

        Button saveButton = findViewById(R.id.saveDiseaseButton);
        saveButton.setOnClickListener(v -> {
            try {
                saveDisease();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDiseases);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        diseaseList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(AddDiseaseActivity.this, diseaseList, this);
        recyclerView.setAdapter(diseaseAdapter);

        loadDiseases();
    }

    @SuppressLint("SetTextI18n")
    private void saveDisease() throws IOException {
        String icd10Code = editTextICD10.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER_DISEASES + " ud "
                + "INNER JOIN " + DatabaseHelper.TABLE_DISEASES + " d "
                + "ON ud." + DatabaseHelper.COLUMN_DISEASE_ID_FK + " = d."
                + DatabaseHelper.COLUMN_DISEASE_ID + " WHERE (d."
                + DatabaseHelper.COLUMN_ICD10 + " =?)"
                + " AND ud." + DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + " =?";

        String[] selectionArgs = {icd10Code, String.valueOf(CURRENT_USER_ID)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (icd10Code.isEmpty()) {
            Snackbar
                    .make(findViewById(android.R.id.content),
                            "Please enter ICD-10 code", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        } else {
            if (cursor.getCount() > 0) {
                Snackbar
                        .make(findViewById(android.R.id.content), "The user already has this disease!",
                                Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                isValidIcd10Code(icd10Code, (isValid, disease) -> {
                    if (!isValid) {
                        Snackbar
                                .make(
                                        findViewById(android.R.id.content), "The ICD-10 code is invalid or incomplete, please verify.", Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_DISEASE_DESCRIPTION, disease);
                        values.put(DatabaseHelper.COLUMN_ICD10, icd10Code);

                        long newRowId = db.insert(DatabaseHelper.TABLE_DISEASES, null, values);

                        ContentValues userDiseaseValues = new ContentValues();
                        userDiseaseValues.put(DatabaseHelper.COLUMN_USER_ID_FK_DISEASE, CURRENT_USER_ID);
                        userDiseaseValues.put(DatabaseHelper.COLUMN_DISEASE_ID_FK, newRowId);

                        db.insert(DatabaseHelper.TABLE_USER_DISEASES, null, userDiseaseValues);

                        final Dialog dialog = new Dialog(AddDiseaseActivity.this);

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
                                    Assistant assistant = service.retrieveAssistant(TokenData.ASSISTANT_ID.getToken());

                                    Thread thread = service.createThread(new ThreadRequest());

                                    UserProfile userProfile = dbHelper.getUserProfile(CURRENT_USER_ID);

                                    String prompt = userProfile.getAge() + " year old having " + userProfile.getHeight()
                                            + " cm "
                                            + " and " + userProfile.getWeight() + " kg and " + icd10Code
                                            + " ICD10 disease code. "
                                            + "Information that may help you in prescribing tratment: blood pressure is "
                                            + userProfile.getBloodPressure() + "mmHG and heartrate "
                                            + userProfile.getHeartrate() + "BPM."
                                            + "Please provide the correct medication on the disease.";

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
                                    String diseaseResponse =
                                            respMsg.getContent().get(0).getText().getValue().replace('*', ' ').replace(
                                                    '#', ' ');
                                    String key = "Disease#" + icd10Code + "#" + disease;
                                    long diseaseId = dbHelper.insertOnSession(CURRENT_USER_ID, key, diseaseResponse);

                                    SharedPreferences.Editor editor =
                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
                                    editor.putString(key, String.valueOf(diseaseId));
                                    editor.apply();

                                    dialog.dismiss();

                                } catch (Exception e) {
                                    handler.post(() -> Snackbar
                                            .make(findViewById(android.R.id.content), "Saving failed! Try again later!",
                                                    Snackbar.LENGTH_SHORT)
                                            .show());
                                }
                            });

                            loadDiseases();
                            editTextICD10.getText().clear();
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
            }
        }

        cursor.close();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDiseases() {
        diseaseList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=?";
        String[] selectionArgs = {String.valueOf(CURRENT_USER_ID)};
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

    @Override
    public void onDeleteDisease(Disease disease) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_USER_ID_FK_DISEASE + "=? AND "
                + DatabaseHelper.COLUMN_DISEASE_ID_FK + "=?";
        String[] whereArgs = {String.valueOf(CURRENT_USER_ID), String.valueOf(disease.getDiseaseId())};
        int deletedRows = db.delete(DatabaseHelper.TABLE_USER_DISEASES, whereClause, whereArgs);

        if (deletedRows > 0) {
            SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
            String key = "Disease#" + disease.getICD10() + "#" + disease.getName();
            if (preferences.contains(key)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(key);
                editor.apply();
            }

            Snackbar
                    .make(findViewById(android.R.id.content), "Disease deleted successfully!",
                            Snackbar.LENGTH_SHORT)
                    .show();
            loadDiseases();
        } else {
            Snackbar
                    .make(findViewById(android.R.id.content), "Failed to delete disease!",
                            Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    public interface Icd10CodeValidationCallback {
        void onResultReceived(boolean isValid, String diseaseName);
    }

    public void isValidIcd10Code(String code, Icd10CodeValidationCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String regex = "^[A-TV-Z][0-9]{2}(\\.[0-9]{1,4})?$";
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
                boolean isValid = resultStr.startsWith("[1,");
                if (isValid && result.length() >= 4) {
                    JSONArray diseaseInfo = result.getJSONArray(3);
                    if (diseaseInfo.length() > 0) {
                        JSONArray diseaseNameInfo = diseaseInfo.getJSONArray(0);
                        if (diseaseNameInfo.length() >= 2) {
                            String diseaseName = diseaseNameInfo.getString(1);
                            handler.post(() -> callback.onResultReceived(isValid, diseaseName));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ICD10CodeValidation", "Validation failed", e);
                handler.post(() -> callback.onResultReceived(false, ""));
            }
        });
    }
}
