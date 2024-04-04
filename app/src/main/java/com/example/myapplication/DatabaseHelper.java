package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medications.db";
    private static final int DATABASE_VERSION = 3;
    private static DatabaseHelper instance;

    // Define table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_USER_MEDICATIONS = "user_medications";
    public static final String TABLE_QUANTITY = "quantity";

    // Define column names for users table
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_PROFILE_COMPLETED = "is_profile_completed";

    // Define column names for medications table
    public static final String COLUMN_MEDICATION_ID = "medication_id";
    public static final String COLUMN_MEDICATION_DESCRIPTION = "medication_description";
    public static final String COLUMN_FREQUENCY = "frequency";

    // Define column names for user_medications table
    public static final String COLUMN_USER_MEDICATION_ID = "user_medication_id";
    public static final String COLUMN_USER_ID_FK = "user_id_fk";
    public static final String COLUMN_MEDICATION_ID_FK = "medication_id_fk";
    public static final String COLUMN_QUANTITY_ID_FK = "quantity_id_fk";

    // Define column names for quantity table
    public static final String COLUMN_QUANTITY_ID = "quantity_id";
    public static final String COLUMN_REMAINING_QUANTITY = "remaining_quantity";

    // Create table definition for profile table
    public static final String TABLE_PROFILE = "profile";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_KNOWN_DISEASE = "known_disease";
    public static final String COLUMN_KNOWN_MEDICATION = "known_medication";

    public static final String TABLE_DISEASES = "diseases";
    public static final String TABLE_USER_DISEASES = "user_diseases";
//...

    // Define column names for diseases table
    public static final String COLUMN_DISEASE_ID = "disease_id";
    public static final String COLUMN_DISEASE_DESCRIPTION = "disease_description"; // placeholder, replace as necessary

    // Define column names for user diseases table
    public static final String COLUMN_USER_DISEASE_ID = "user_disease_id";
    public static final String COLUMN_USER_ID_FK_DISEASE = "user_id_fk_disease";
    public static final String COLUMN_DISEASE_ID_FK = "disease_id_fk";
//...

    // Create Diseases Table
    private static final String CREATE_TABLE_DISEASES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_DISEASES + " (" +
                    COLUMN_DISEASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DISEASE_DESCRIPTION + " TEXT)";

    // Create User Diseases junction table
    private static final String CREATE_TABLE_USER_DISEASES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER_DISEASES + " (" +
                    COLUMN_USER_DISEASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID_FK_DISEASE + " INTEGER, " +
                    COLUMN_DISEASE_ID_FK + " INTEGER, " +
                    "FOREIGN KEY (" + COLUMN_USER_ID_FK_DISEASE + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_DISEASE_ID_FK + ") REFERENCES " + TABLE_DISEASES + "(" + COLUMN_DISEASE_ID + "))";

    private static final String CREATE_TABLE_PROFILE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILE + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_AGE + " INTEGER, " +
                    COLUMN_HEIGHT + " FLOAT, " +
                    COLUMN_WEIGHT + " FLOAT, " +
                    COLUMN_KNOWN_DISEASE + " TEXT, " +
                    COLUMN_KNOWN_MEDICATION + " TEXT)";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_IS_PROFILE_COMPLETED + " INTEGER DEFAULT 0)";

    private static final String CREATE_TABLE_MEDICATIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MEDICATIONS + " (" +
                    COLUMN_MEDICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MEDICATION_DESCRIPTION + " TEXT, " +
                    COLUMN_FREQUENCY + " TEXT)";

    private static final String CREATE_TABLE_USER_MEDICATIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER_MEDICATIONS + " (" +
                    COLUMN_USER_MEDICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID_FK + " INTEGER, " +
                    COLUMN_MEDICATION_ID_FK + " INTEGER, " +
                    COLUMN_QUANTITY_ID_FK + " INTEGER, " +
                    "FOREIGN KEY (" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_MEDICATION_ID_FK + ") REFERENCES " + TABLE_MEDICATIONS + "(" + COLUMN_MEDICATION_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_QUANTITY_ID_FK + ") REFERENCES " + TABLE_QUANTITY + "(" + COLUMN_QUANTITY_ID + "))";

    private static final String CREATE_TABLE_QUANTITY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_QUANTITY + " (" +
                    COLUMN_QUANTITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_REMAINING_QUANTITY + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_MEDICATIONS);
        db.execSQL(CREATE_TABLE_USER_MEDICATIONS);
        db.execSQL(CREATE_TABLE_QUANTITY);
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_DISEASES);
        db.execSQL(CREATE_TABLE_USER_DISEASES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUANTITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISEASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DISEASES);

        onCreate(db);
    }

    public void insertOrUpdateProfile(String userId, String name, int age, float height, float weight, List<Disease> knownDiseases, List<Medication> knownMedications, int isProfileCompleted){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_WEIGHT, weight);
        for (Disease disease : knownDiseases) {
            ContentValues diseaseValues = new ContentValues();
            diseaseValues.put(COLUMN_USER_ID_FK_DISEASE, userId);
            diseaseValues.put(COLUMN_DISEASE_ID_FK, disease.getDiseaseId());

            db.insertWithOnConflict(TABLE_USER_DISEASES, null, diseaseValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        for (Medication medication : knownMedications) {
            ContentValues diseaseValues = new ContentValues();
            diseaseValues.put(COLUMN_USER_ID_FK_DISEASE, userId);
            diseaseValues.put(COLUMN_DISEASE_ID_FK, medication.getMedicationId());

            db.insertWithOnConflict(TABLE_USER_DISEASES, null, diseaseValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        values.put(COLUMN_IS_PROFILE_COMPLETED, isProfileCompleted);

        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public Cursor getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_NAME,
                COLUMN_AGE,
                COLUMN_HEIGHT,
                COLUMN_WEIGHT,
                COLUMN_KNOWN_DISEASE,
                COLUMN_KNOWN_MEDICATION
        };

        Cursor cursor = db.query(TABLE_PROFILE, columns, COLUMN_USER_ID + "=?", new String[]{userId}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public boolean isProfileCompleted(String userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[] {DatabaseHelper.COLUMN_IS_PROFILE_COMPLETED},
                COLUMN_USER_ID + "=?",
                new String[] {userId},
                null, null, null);

        int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_PROFILE_COMPLETED);

        if(columnIndex != -1 && cursor.moveToFirst()){
            int isProfileCompleted = cursor.getInt(columnIndex);
            cursor.close();
            db.close();
            return isProfileCompleted == 1;
        }
        cursor.close();
        db.close();
        return false;
    }

    public void setProfileCompleted(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("is_profile_completed", 1);

        db.update(TABLE_USERS,
                contentValues,
                COLUMN_USER_ID + " = ?",
                new String[] {userId});
        db.close();
    }

    public User getUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
                COLUMN_IS_PROFILE_COMPLETED
        };

        Cursor cursor = db.query(TABLE_USERS, columns, COLUMN_USER_ID + "=?",
                new String[]{userId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            @SuppressLint("Range") int isProfileCompleted = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PROFILE_COMPLETED));

            cursor.close();

            if (isProfileCompleted == 1) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(COLUMN_AGE));
                @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));
                @SuppressLint("Range") float weight = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
                @SuppressLint("Range") List<Disease> diseasesList = getUserDiseases(userId);
                @SuppressLint("Range") List<Medication> medicationsList = getUserMedications(userId);

                return new User(userId, username, password, true,
                        name, age, height, weight, diseasesList, medicationsList);
            } else {
                // Assume a constructor for User exists that takes only userId, username, password
                return new User(userId, username, password, false);
            }
        }

        return null;
    }

    public List<Disease> getUserDiseases(String userId) {
        List<Disease> diseases = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER_DISEASES +
                        " JOIN " + TABLE_DISEASES +
                        " ON " + COLUMN_DISEASE_ID_FK + " = " + COLUMN_DISEASE_ID +
                        " WHERE " + COLUMN_USER_ID_FK_DISEASE + " = ?",
                new String[]{userId}
        );
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Disease disease = new Disease(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_DISEASE_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DISEASE_DESCRIPTION))
                );
                diseases.add(disease);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return diseases;
    }
    public List<Medication> getUserMedications(String userId) {
        List<Medication> medications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER_MEDICATIONS +
                        " JOIN " + TABLE_MEDICATIONS +
                        " ON " + COLUMN_MEDICATION_ID_FK + " = " + COLUMN_MEDICATION_ID +
                        " WHERE " + COLUMN_USER_MEDICATION_ID + " = ?",
                new String[]{userId}
        );
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Medication medication = new Medication(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_MEDICATION_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MEDICATION_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MEDICATION_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MEDICATION_DESCRIPTION))
                );
                medications.add(medication);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return medications;
    }
}
