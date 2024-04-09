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
    private static final int DATABASE_VERSION = 12;
    private static DatabaseHelper instance;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_USER_MEDICATIONS = "user_medications";

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    public static final String COLUMN_MEDICATION_ID = "medication_id";
    public static final String COLUMN_MEDICATION_DESCRIPTION = "medication_description";

    public static final String COLUMN_USER_MEDICATION_ID = "user_medication_id";
    public static final String COLUMN_USER_ID_FK = "user_id_fk";
    public static final String COLUMN_MEDICATION_ID_FK = "medication_id_fk";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_QUANTITY_LEFT = "quantity_id_fk";

    public static final String TABLE_PROFILE = "profile";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_BLOOD_PRESSURE = "blood_pressure";
    public static final String COLUMN_HEARTRATE = "heartrate";

    public static final String TABLE_DISEASES = "diseases";
    public static final String TABLE_USER_DISEASES = "user_diseases";

    public static final String COLUMN_DISEASE_ID = "disease_id";
    public static final String COLUMN_ICD10 = "icd10";
    public static final String COLUMN_DISEASE_DESCRIPTION = "disease_description";

    public static final String COLUMN_USER_DISEASE_ID = "user_disease_id";
    public static final String COLUMN_USER_ID_FK_DISEASE = "user_id_fk_disease";
    public static final String COLUMN_DISEASE_ID_FK = "disease_id_fk";

    private static final String CREATE_TABLE_DISEASES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_DISEASES + " (" +
                    COLUMN_DISEASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DISEASE_DESCRIPTION + " TEXT, " +
                    COLUMN_ICD10 + " TEXT)";

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
                    COLUMN_BLOOD_PRESSURE + " INTEGER, " +
                    COLUMN_HEARTRATE + " INTEGER, " +
                    COLUMN_WEIGHT + " FLOAT) ";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_PASSWORD + " TEXT)";

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
                    COLUMN_QUANTITY_LEFT + " INTEGER, " +
                    COLUMN_DOSAGE + " INTEGER, " +
                    "FOREIGN KEY (" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_MEDICATION_ID_FK + ") REFERENCES " + TABLE_MEDICATIONS + "(" + COLUMN_MEDICATION_ID + "))";


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
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_DISEASES);
        db.execSQL(CREATE_TABLE_USER_DISEASES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISEASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DISEASES);

        onCreate(db);
    }

    public UserProfile getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_NAME,
                COLUMN_AGE,
                COLUMN_HEIGHT,
                COLUMN_WEIGHT,
                COLUMN_BLOOD_PRESSURE,
                COLUMN_HEARTRATE
        };

        Cursor cursor = db.query(TABLE_PROFILE, columns, COLUMN_USER_ID + "=?", new String[]{userId}, null, null, null,null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(COLUMN_AGE));
            @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));
            @SuppressLint("Range") float weight = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
            @SuppressLint("Range") int bloodPresure = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOOD_PRESSURE));
            @SuppressLint("Range") int heartrate = cursor.getInt(cursor.getColumnIndex(COLUMN_HEARTRATE));
            cursor.close();
            return new UserProfile(name, age, height, weight, bloodPresure, heartrate);
        }
        cursor.close();
        return null;
    }

    public void insertOrUpdateProfile(String userId, UserProfile userProfile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, userProfile.getName());
        values.put(COLUMN_AGE, userProfile.getAge());
        values.put(COLUMN_HEIGHT, userProfile.getHeight());
        values.put(COLUMN_WEIGHT, userProfile.getWeight());
        values.put(COLUMN_BLOOD_PRESSURE, userProfile.getBloodPressure());
        values.put(COLUMN_HEARTRATE, userProfile.getHeartrate());

        Cursor cursor = db.query(TABLE_PROFILE, null, COLUMN_USER_ID + "=?",
                new String[]{userId}, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {
            db.update(TABLE_PROFILE, values, COLUMN_USER_ID + "=?", new String[]{userId});
        } else {
            db.insert(TABLE_PROFILE, null, values);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    public User checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
        };

        Cursor cursor = db.query(TABLE_USERS, columns, COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
            UserProfile userProfile = getUserProfile(userId);
            cursor.close();
            return new User(userId, username, password, userProfile);
        }
        cursor.close();
        return null;
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        return db.insert(TABLE_USERS, null, values);
    }

    public User getUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
        };

        Cursor cursor = db.query(TABLE_USERS, columns, COLUMN_USER_ID + "=?",
                new String[]{userId}, null, null, null);

            if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                UserProfile userProfile = getUserProfile(userId);
                cursor.close();
                return new User(userId, username, password, userProfile);
            }
            cursor.close();
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
                        cursor.getString(cursor.getColumnIndex(COLUMN_DISEASE_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ICD10))                );
                diseases.add(disease);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return diseases;
    }

}
