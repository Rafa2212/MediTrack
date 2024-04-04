package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AddMedicationActivity extends AppCompatActivity {

    private EditText editTextMedicationName;
    private EditText editTextDosage;
    private EditText editTextFrequency;
    private RecyclerView recyclerView;
    private MedicationAdapter medicationAdapter;
    private List<Medication> medicationList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.page_1) {
                // Action when first item is selected
            } else if (itemId == R.id.page_2) {
                // Action when second item is selected, e.g. start Profile activity
                startActivity(new Intent(this, ProfileSetupActivity.class));
            } else if (itemId == R.id.page_3) {
                // Action when third item is selected, e.g. logout
                startActivity(new Intent(this, LoginActivity.class));
            }
            return true;
        });

        dbHelper = new DatabaseHelper(this);

        editTextMedicationName = findViewById(R.id.editTextMedicationName);
        editTextDosage = findViewById(R.id.editTextDosage);
        editTextFrequency = findViewById(R.id.editTextFrequency);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMedication();
            }
        });

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerViewMedications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicationList = new ArrayList<>();
        medicationAdapter = new MedicationAdapter(AddMedicationActivity.this, medicationList);
        recyclerView.setAdapter(medicationAdapter);

        // Load medications from database
        loadMedications();
    }

    private void saveMedication() {
        String medicationName = editTextMedicationName.getText().toString().trim();
        String dosage = editTextDosage.getText().toString().trim();
        String frequency = editTextFrequency.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_MEDICATION_DESCRIPTION, medicationName);
        values.put(DatabaseHelper.COLUMN_FREQUENCY, frequency);

        long newRowId = db.insert(DatabaseHelper.TABLE_MEDICATIONS, null, values);

        if (newRowId != -1) {
            // Successful insertion
            Toast.makeText(this, "Medication added successfully", Toast.LENGTH_SHORT).show();
            loadMedications(); // Reload medications after adding a new one
        } else {
            // Insertion failed
            Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedications() {
        medicationList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MEDICATIONS,
                null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_MEDICATION_ID));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MEDICATION_DESCRIPTION));
                @SuppressLint("Range") String frequency = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FREQUENCY));

                Medication medication = new Medication(id,description, description, frequency);
                medicationList.add(medication);
            }
            cursor.close();
            medicationAdapter.notifyDataSetChanged();
        }
    }
}
