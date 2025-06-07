package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper class that manages SQLite database operations for the MediTrack application.
 * This class handles database creation, version management, and provides methods for
 * CRUD operations on users, profiles, diseases, doctor-patient relationships,
 * medical reports, and notifications.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "meditrack.db";
    private static final int DATABASE_VERSION = 66;
    private static DatabaseHelper instance;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_DOCTOR_PATIENTS = "doctor_patients";
    public static final String TABLE_MEDICAL_REPORTS = "medical_reports";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";

    public static final String TABLE_PATIENTS = "patients";
    public static final String TABLE_DOCTORS = "doctors";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_LAST_MED_REP = "last_medical_report";
    public static final String COLUMN_CNP = "cnp";
    public static final String COLUMN_BODY_FAT_PERCENTAGE = "body_fat_percentage";
    public static final String COLUMN_BLOOD_PRESSURE_SYSTOLIC = "blood_pressure_systolic";
    public static final String COLUMN_BLOOD_PRESSURE_DIASTOLIC = "blood_pressure_diastolic";
    public static final String COLUMN_RESTING_HEART_RATE = "resting_heart_rate";
    public static final String COLUMN_BLOOD_GLUCOSE = "blood_glucose";
    public static final String COLUMN_CHOLESTEROL_TOTAL = "cholesterol_total";
    public static final String COLUMN_CHOLESTEROL_HDL = "cholesterol_hdl";
    public static final String COLUMN_CHOLESTEROL_LDL = "cholesterol_ldl";
    public static final String COLUMN_HEALTH_SCORE = "health_score";
    public static final String COLUMN_BODY_TYPE = "body_type";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_BMI_INTERPRETATION = "bmi_interpretation";
    public static final String COLUMN_METABOLIC_INTERPRETATION = "metabolic_interpretation";
    public static final String COLUMN_SPECIALTY = "specialty";

    public static final String TABLE_USER_DISEASES = "user_diseases";

    public static final String COLUMN_ICD10 = "icd10";
    public static final String COLUMN_DISEASE_DESCRIPTION = "disease_description";

    public static final String COLUMN_USER_DISEASE_ID = "id";
    public static final String COLUMN_USER_ID_FK_DISEASE = "id_fk_user";



    public static final String COLUMN_DOCTOR_PATIENT_ID = "id";
    public static final String COLUMN_DOCTOR_ID_FK = "doctor_id_fk";
    public static final String COLUMN_PATIENT_ID_FK = "patient_id_fk";

    // Medical Reports table columns
    public static final String COLUMN_REPORT_ID = "report_id";
    public static final String COLUMN_REPORT_DATE = "report_date";
    public static final String COLUMN_REPORT_CONTENT = "report_content";
    public static final String COLUMN_REPORT_PATH = "report_path";

    // Notifications table columns
    public static final String COLUMN_NOTIFICATION_ID = "notification_id";
    public static final String COLUMN_NOTIFICATION_USER_ID = "user_id";
    public static final String COLUMN_NOTIFICATION_MESSAGE = "message";
    public static final String COLUMN_NOTIFICATION_DATE = "date";
    public static final String COLUMN_NOTIFICATION_READ = "is_read";
    public static final String COLUMN_NOTIFICATION_TYPE = "type";


    private static final String CREATE_TABLE_USER_DISEASES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_USER_DISEASES + " (" + COLUMN_USER_DISEASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID_FK_DISEASE + " INTEGER, "
            + COLUMN_DOCTOR_ID_FK + " INTEGER, " + "diagnosis_date TEXT, "
            + COLUMN_DISEASE_DESCRIPTION + " TEXT, " + COLUMN_ICD10 + " TEXT, "
            + "interpretation TEXT, " + "disease_key TEXT, "
            + "FOREIGN KEY (" + COLUMN_USER_ID_FK_DISEASE + ") REFERENCES " + TABLE_USERS + "("
            + COLUMN_USER_ID + "), "
            + "FOREIGN KEY (" + COLUMN_DOCTOR_ID_FK + ") REFERENCES " + TABLE_USERS + "("
            + COLUMN_USER_ID + "))";

    private static final String CREATE_TABLE_PATIENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_PATIENTS
            + " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY, " + COLUMN_CNP + " TEXT, " + COLUMN_NAME + " TEXT, "
            + COLUMN_AGE + " INTEGER, " + COLUMN_HEIGHT + " FLOAT, "
            + COLUMN_WEIGHT + " FLOAT, " + COLUMN_LAST_MED_REP + " TEXT, "
            + COLUMN_BODY_FAT_PERCENTAGE + " FLOAT, "
            + COLUMN_BLOOD_PRESSURE_SYSTOLIC + " INTEGER, "
            + COLUMN_BLOOD_PRESSURE_DIASTOLIC + " INTEGER, "
            + COLUMN_RESTING_HEART_RATE + " INTEGER, "
            + COLUMN_BLOOD_GLUCOSE + " FLOAT, "
            + COLUMN_CHOLESTEROL_TOTAL + " FLOAT, "
            + COLUMN_CHOLESTEROL_HDL + " FLOAT, "
            + COLUMN_CHOLESTEROL_LDL + " FLOAT, "
            + COLUMN_HEALTH_SCORE + " INTEGER, "
            + COLUMN_BODY_TYPE + " TEXT, "
            + COLUMN_GENDER + " TEXT, "
            + COLUMN_BMI_INTERPRETATION + " TEXT, "
            + COLUMN_METABOLIC_INTERPRETATION + " TEXT, "
            + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    private static final String CREATE_TABLE_DOCTORS = "CREATE TABLE IF NOT EXISTS " + TABLE_DOCTORS
            + " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY, " + COLUMN_NAME + " TEXT, "
            + COLUMN_SPECIALTY + " TEXT, "
            + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS
            + " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_USERNAME + " TEXT, "
            + COLUMN_PASSWORD + " TEXT, " + COLUMN_USER_ROLE + " TEXT DEFAULT 'patient')";


    private static final String CREATE_TABLE_DOCTOR_PATIENTS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_DOCTOR_PATIENTS + " (" + COLUMN_DOCTOR_PATIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DOCTOR_ID_FK + " INTEGER, " + COLUMN_PATIENT_ID_FK + " INTEGER, "
            + "FOREIGN KEY (" + COLUMN_DOCTOR_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), "
            + "FOREIGN KEY (" + COLUMN_PATIENT_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    private static final String CREATE_TABLE_MEDICAL_REPORTS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MEDICAL_REPORTS + " (" + COLUMN_REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PATIENT_ID_FK + " INTEGER, " + COLUMN_DOCTOR_ID_FK + " INTEGER, " 
            + COLUMN_REPORT_DATE + " TEXT, " + COLUMN_REPORT_CONTENT + " TEXT, " 
            + COLUMN_REPORT_PATH + " TEXT, "
            + "FOREIGN KEY (" + COLUMN_PATIENT_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), "
            + "FOREIGN KEY (" + COLUMN_DOCTOR_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NOTIFICATIONS + " (" + COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NOTIFICATION_USER_ID + " INTEGER, " + COLUMN_NOTIFICATION_MESSAGE + " TEXT, "
            + COLUMN_NOTIFICATION_DATE + " TEXT, " + COLUMN_NOTIFICATION_READ + " INTEGER, "
            + COLUMN_NOTIFICATION_TYPE + " TEXT, "
            + "FOREIGN KEY (" + COLUMN_NOTIFICATION_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";


    /**
     * Constructor for the DatabaseHelper.
     * Initializes the SQLite database with the specified name and version.
     *
     * @param context The context used to locate the database
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Gets the singleton instance of the DatabaseHelper.
     * This ensures that only one instance of the database helper exists throughout the application.
     *
     * @param context The context used to create the database helper if it doesn't exist
     * @return The singleton instance of DatabaseHelper
     */
    static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Called when the database is created for the first time.
     * Creates all the necessary tables for the application.
     *
     * @param db The database instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PATIENTS);
        db.execSQL(CREATE_TABLE_DOCTORS);
        db.execSQL(CREATE_TABLE_USER_DISEASES);
        db.execSQL(CREATE_TABLE_DOCTOR_PATIENTS);
        db.execSQL(CREATE_TABLE_MEDICAL_REPORTS);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    /**
     * Called when the database needs to be upgraded from an older version to a newer one.
     * Handles schema changes and data migration between versions.
     * For version 32, adds a body_type column to the profile table.
     * For other version changes, recreates all tables.
     *
     * @param db The database instance
     * @param oldVersion The old database version
     * @param newVersion The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > 1) {
            // For older versions or newer versions > 1, just recreate all tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTORS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DISEASES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTOR_PATIENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAL_REPORTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);

            onCreate(db);
        }
    }

    /**
     * Retrieves a user's profile from the database.
     * Fetches all profile information including basic details and health metrics.
     * Depending on the user's role, it will retrieve data from either the patients or doctors table.
     *
     * @param userId The ID of the user whose profile to retrieve
     * @return The Object (either Patient or Doctor) containing all profile information, or null if no profile exists
     */
    public Object getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // First, determine the user's role
        String role = "";
        Cursor roleCursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ROLE}, 
                COLUMN_USER_ID + "=?", new String[]{userId}, null, null, null);
        if (roleCursor != null && roleCursor.moveToFirst()) {
            @SuppressLint("Range") 
            String userRole = roleCursor.getString(roleCursor.getColumnIndex(COLUMN_USER_ROLE));
            role = userRole;
            roleCursor.close();
        }

        if ("doctor".equals(role)) {
            // Get doctor profile
            Doctor doctor = null;

            String[] doctorColumns = {
                COLUMN_NAME,
                COLUMN_SPECIALTY
            };

            try (Cursor cursor = db.query(TABLE_DOCTORS, doctorColumns, COLUMN_USER_ID + "=?", new String[]{userId},
                    null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    @SuppressLint("Range") String specialty = cursor.getString(cursor.getColumnIndex(COLUMN_SPECIALTY));

                    doctor = new Doctor(name, specialty);
                }
            }
            return doctor;
        } else {
            // Get patient profile
            Patient patient = null;

            String[] patientColumns = {
                COLUMN_CNP,
                COLUMN_NAME, 
                COLUMN_AGE, 
                COLUMN_HEIGHT, 
                COLUMN_WEIGHT, 
                COLUMN_LAST_MED_REP,
                COLUMN_BODY_FAT_PERCENTAGE,
                COLUMN_BLOOD_PRESSURE_SYSTOLIC,
                COLUMN_BLOOD_PRESSURE_DIASTOLIC,
                COLUMN_RESTING_HEART_RATE,
                COLUMN_BLOOD_GLUCOSE,
                COLUMN_CHOLESTEROL_TOTAL,
                COLUMN_CHOLESTEROL_HDL,
                COLUMN_CHOLESTEROL_LDL,
                COLUMN_HEALTH_SCORE,
                COLUMN_BODY_TYPE,
                COLUMN_GENDER,
                COLUMN_BMI_INTERPRETATION,
                COLUMN_METABOLIC_INTERPRETATION
            };

            try (Cursor cursor = db.query(TABLE_PATIENTS, patientColumns, COLUMN_USER_ID + "=?", new String[]{userId},
                    null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") String cnp = cursor.getString(cursor.getColumnIndex(COLUMN_CNP));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(COLUMN_AGE));
                    @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));
                    @SuppressLint("Range") float weight = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
                    @SuppressLint("Range") String lastMedicalReport = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MED_REP));

                    patient = new Patient(name, age, height, weight, lastMedicalReport);
                    if (cnp != null) {
                        patient.setCnp(cnp);
                    }

                    @SuppressLint("Range") float bodyFatPercentage = cursor.getFloat(cursor.getColumnIndex(COLUMN_BODY_FAT_PERCENTAGE));
                    if (bodyFatPercentage > 0) {
                        patient.setBodyFatPercentage(bodyFatPercentage);
                    }

                    @SuppressLint("Range") int bloodPressureSystolic = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOOD_PRESSURE_SYSTOLIC));
                    if (bloodPressureSystolic > 0) {
                        patient.setBloodPressureSystolic(bloodPressureSystolic);
                    }

                    @SuppressLint("Range") int bloodPressureDiastolic = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOOD_PRESSURE_DIASTOLIC));
                    if (bloodPressureDiastolic > 0) {
                        patient.setBloodPressureDiastolic(bloodPressureDiastolic);
                    }

                    @SuppressLint("Range") int restingHeartRate = cursor.getInt(cursor.getColumnIndex(COLUMN_RESTING_HEART_RATE));
                    if (restingHeartRate > 0) {
                        patient.setRestingHeartRate(restingHeartRate);
                    }

                    @SuppressLint("Range") float bloodGlucose = cursor.getFloat(cursor.getColumnIndex(COLUMN_BLOOD_GLUCOSE));
                    if (bloodGlucose > 0) {
                        patient.setBloodGlucose(bloodGlucose);
                    }

                    @SuppressLint("Range") float cholesterolTotal = cursor.getFloat(cursor.getColumnIndex(COLUMN_CHOLESTEROL_TOTAL));
                    if (cholesterolTotal > 0) {
                        patient.setCholesterolTotal(cholesterolTotal);
                    }

                    @SuppressLint("Range") float cholesterolHDL = cursor.getFloat(cursor.getColumnIndex(COLUMN_CHOLESTEROL_HDL));
                    if (cholesterolHDL > 0) {
                        patient.setCholesterolHDL(cholesterolHDL);
                    }

                    @SuppressLint("Range") float cholesterolLDL = cursor.getFloat(cursor.getColumnIndex(COLUMN_CHOLESTEROL_LDL));
                    if (cholesterolLDL > 0) {
                        patient.setCholesterolLDL(cholesterolLDL);
                    }

                    @SuppressLint("Range") int healthScore = cursor.getInt(cursor.getColumnIndex(COLUMN_HEALTH_SCORE));
                    if (healthScore > 0) {
                        patient.setHealthScore(healthScore);
                    }

                    @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex(COLUMN_BODY_TYPE));
                    if (bodyType != null && !bodyType.isEmpty()) {
                        patient.setBodyType(bodyType);
                    }

                    @SuppressLint("Range") String gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER));
                    if (gender != null && !gender.isEmpty()) {
                        patient.setGender(gender);
                    }

                    @SuppressLint("Range") String bmiInterpretation = cursor.getString(cursor.getColumnIndex(COLUMN_BMI_INTERPRETATION));
                    if (bmiInterpretation != null && !bmiInterpretation.isEmpty()) {
                        patient.setBmiInterpretation(bmiInterpretation);
                    }

                    @SuppressLint("Range") String metabolicInterpretation = cursor.getString(cursor.getColumnIndex(COLUMN_METABOLIC_INTERPRETATION));
                    if (metabolicInterpretation != null && !metabolicInterpretation.isEmpty()) {
                        patient.setMetabolicInterpretation(metabolicInterpretation);
                    }
                }
            }
            return patient;
        }
    }

    /**
     * Inserts a new user profile or updates an existing one in the database.
     * Saves all profile information including basic details and health metrics.
     * If a profile with the given userId already exists, it will be updated;
     * otherwise, a new profile will be created.
     * Depending on the user's role and the type of profile object, it will insert/update
     * either the patients or doctors table.
     *
     * @param userId The ID of the user whose profile to save or update
     * @param profile The Patient or Doctor object containing the profile information to save
     */
    public void insertOrUpdateProfile(String userId, Object profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            // First, determine the user's role
            String role = "";
            Cursor roleCursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ROLE}, 
                    COLUMN_USER_ID + "=?", new String[]{userId}, null, null, null);
            if (roleCursor != null && roleCursor.moveToFirst()) {
                @SuppressLint("Range") 
                String userRole = roleCursor.getString(roleCursor.getColumnIndex(COLUMN_USER_ROLE));
                role = userRole;
                roleCursor.close();
            }

            if ("doctor".equals(role) && profile instanceof Doctor) {
                Doctor doctor = (Doctor) profile;
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_NAME, doctor.getName());
                values.put(COLUMN_SPECIALTY, doctor.getSpecialty());

                cursor = db.query(
                        TABLE_DOCTORS, null, COLUMN_USER_ID + "=?", new String[] {userId}, null, null, null);

                db.beginTransaction();

                if (cursor != null && cursor.getCount() > 0) {
                    db.update(TABLE_DOCTORS, values, COLUMN_USER_ID + "=?", new String[] {userId});
                } else {
                    db.insert(TABLE_DOCTORS, null, values);
                }

                db.setTransactionSuccessful();
            } else if (profile instanceof Patient) {
                Patient patient = (Patient) profile;
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_CNP, patient.getCnp());
                values.put(COLUMN_NAME, patient.getName());
                values.put(COLUMN_AGE, patient.getAge());
                values.put(COLUMN_HEIGHT, patient.getHeight());
                values.put(COLUMN_WEIGHT, patient.getWeight());
                values.put(COLUMN_LAST_MED_REP, patient.getLastMedicalReport());

                values.put(COLUMN_BODY_FAT_PERCENTAGE, patient.getBodyFatPercentage());
                values.put(COLUMN_BLOOD_PRESSURE_SYSTOLIC, patient.getBloodPressureSystolic());
                values.put(COLUMN_BLOOD_PRESSURE_DIASTOLIC, patient.getBloodPressureDiastolic());
                values.put(COLUMN_RESTING_HEART_RATE, patient.getRestingHeartRate());
                values.put(COLUMN_BLOOD_GLUCOSE, patient.getBloodGlucose());
                values.put(COLUMN_CHOLESTEROL_TOTAL, patient.getCholesterolTotal());
                values.put(COLUMN_CHOLESTEROL_HDL, patient.getCholesterolHDL());
                values.put(COLUMN_CHOLESTEROL_LDL, patient.getCholesterolLDL());
                values.put(COLUMN_HEALTH_SCORE, patient.getHealthScore());
                values.put(COLUMN_BODY_TYPE, patient.getBodyType());
                values.put(COLUMN_GENDER, patient.getGender());
                values.put(COLUMN_BMI_INTERPRETATION, patient.getBmiInterpretation());
                values.put(COLUMN_METABOLIC_INTERPRETATION, patient.getMetabolicInterpretation());

                cursor = db.query(
                        TABLE_PATIENTS, null, COLUMN_USER_ID + "=?", new String[] {userId}, null, null, null);

                db.beginTransaction();

                if (cursor != null && cursor.getCount() > 0) {
                    db.update(TABLE_PATIENTS, values, COLUMN_USER_ID + "=?", new String[] {userId});
                } else {
                    db.insert(TABLE_PATIENTS, null, values);
                }

                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }
    }

    /**
     * Synchronous method to check user credentials and return a User object directly.
     * This method uses the SQLite database to check the user credentials, making it
     * backward compatible with the existing code.
     *
     * @param username The username to check
     * @param password The password to check
     * @return The User object if authentication is successful, or null if it fails
     */
    public User checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
                COLUMN_USER_ROLE
        };

        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
            @SuppressLint("Range")
            String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
            @SuppressLint("Range")
            String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));

            Object profile = getUserProfile(userId);

            if ("doctor".equals(role) && profile instanceof Doctor) {
                user = new User(userId, (Doctor) profile, role);
            } else if (profile instanceof Patient) {
                user = new User(userId, (Patient) profile, role);
            } else {
                if ("doctor".equals(role)) {
                    user = new User(userId, (Doctor) null, role);
                } else {
                    user = new User(userId, (Patient) null, role);
                }
            }
        }

        cursor.close();
        return user;
    }

    /**
     * Adds a test user to the database. This method should only be used by the AddDoctorTest class.
     */
    public long addTestUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, password);
            values.put(COLUMN_USER_ROLE, role);

            db.beginTransaction();
            result = db.insert(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return result;
    }

    /**
     * Adds a doctor user to the database. This method should only be used by the AddDoctorTest class.
     */
    public long addDoctor(String username, String password) {
        return addTestUser(username, password, "doctor");
    }

    /**
     * Retrieves a user from the database by their ID.
     * Returns a User object containing the user's ID, profile, and role.
     * Depending on the user's role, it will create a User object with either a Patient or Doctor object.
     *
     * @param userId The ID of the user to retrieve
     * @return A User object if the user exists, or null if no user with the given ID is found
     */
    public User getUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
                COLUMN_USER_ROLE
        };

        Cursor cursor = db.query(
                TABLE_USERS, columns, COLUMN_USER_ID + "=?", new String[] {userId}, null, null, null);

        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
            @SuppressLint("Range")
            String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));

            Object profile = getUserProfile(userId);
            cursor.close();

            if ("doctor".equals(role) && profile instanceof Doctor) {
                return new User(userId, (Doctor) profile, role);
            } else if (profile instanceof Patient) {
                return new User(userId, (Patient) profile, role);
            } else {
                if ("doctor".equals(role)) {
                    return new User(userId, (Doctor) null, role);
                } else {
                    return new User(userId, (Patient) null, role);
                }
            }
        }
        cursor.close();
        return null;
    }

    /**
     * Checks if a user has the doctor role.
     * This method queries the database to determine if the user with the given ID is a doctor.
     *
     * @param userId The ID of the user to check
     * @return true if the user is a doctor, false otherwise
     */
    public boolean isDoctor(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { COLUMN_USER_ROLE };

        Cursor cursor = db.query(
                TABLE_USERS, columns, COLUMN_USER_ID + "=?", new String[] {userId}, null, null, null);

        boolean isDoctor = false;

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range")
            String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));
            isDoctor = "doctor".equals(role);
        }

        if (cursor != null) {
            cursor.close();
        }

        return isDoctor;
    }

    /**
     * Retrieves a user's credentials (username and password) from the database.
     *
     * @param userId The ID of the user whose credentials to retrieve
     * @return A String array containing the username at index 0 and password at index 1,
     *         or null if the user is not found
     */
    public String[] getUserCredentials(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USERNAME,
                COLUMN_PASSWORD
        };

        Cursor cursor = db.query(
                TABLE_USERS, columns, COLUMN_USER_ID + "=?", new String[] {userId}, null, null, null);

        String[] credentials = null;

        if (cursor != null && cursor.moveToFirst()) {
            credentials = new String[2];
            @SuppressLint("Range")
            String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            @SuppressLint("Range")
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));

            credentials[0] = username;
            credentials[1] = password;
        }

        if (cursor != null) {
            cursor.close();
        }

        return credentials;
    }

    /**
     * Assigns a patient to a doctor in the database.
     * This method creates a relationship between a doctor and a patient.
     * The assignment will only occur if:
     * 1. The doctorId belongs to a user with the doctor role
     * 2. The patientId belongs to a user who is not a doctor
     * 3. The patient is not already assigned to this doctor
     *
     * @param doctorId The ID of the doctor
     * @param patientId The ID of the patient
     */
    public void assignPatientToDoctor(String doctorId, String patientId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            if (!isDoctor(doctorId)) {
                return;
            }

            if (isDoctor(patientId)) {
                return;
            }

            if (isPatientAssignedToDoctor(doctorId, patientId)) {
                return;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_DOCTOR_ID_FK, doctorId);
            values.put(COLUMN_PATIENT_ID_FK, patientId);

            db.beginTransaction();
            db.insert(TABLE_DOCTOR_PATIENTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    /**
     * Checks if a patient is already assigned to a doctor.
     * This method queries the database to determine if a doctor-patient relationship exists.
     *
     * @param doctorId The ID of the doctor
     * @param patientId The ID of the patient
     * @return true if the patient is assigned to the doctor, false otherwise
     */
    public boolean isPatientAssignedToDoctor(String doctorId, String patientId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_DOCTOR_PATIENTS, 
                new String[] { COLUMN_DOCTOR_PATIENT_ID }, 
                COLUMN_DOCTOR_ID_FK + "=? AND " + COLUMN_PATIENT_ID_FK + "=?", 
                new String[] { doctorId, patientId }, 
                null, null, null);

        boolean isAssigned = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }

        return isAssigned;
    }

    /**
     * Gets all patients assigned to a specific doctor.
     * This method retrieves a list of users with the patient role who have been assigned to the doctor.
     * If the provided ID does not belong to a doctor, an empty list is returned.
     *
     * @param doctorId The ID of the doctor
     * @return A list of User objects representing the patients assigned to the doctor
     */
    public java.util.List<User> getPatientsForDoctor(String doctorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<User> patients = new java.util.ArrayList<>();

        if (!isDoctor(doctorId)) {
            return patients;
        }

        String query = "SELECT p." + COLUMN_USER_ID + ", p." + COLUMN_USERNAME + ", p." + COLUMN_USER_ROLE +
                " FROM " + TABLE_USERS + " p" +
                " INNER JOIN " + TABLE_DOCTOR_PATIENTS + " dp" +
                " ON p." + COLUMN_USER_ID + " = dp." + COLUMN_PATIENT_ID_FK +
                " WHERE dp." + COLUMN_DOCTOR_ID_FK + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] { doctorId });

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range")
                String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));

                Object profile = getUserProfile(userId);
                if (profile instanceof Patient) {
                    patients.add(new User(userId, (Patient) profile, role));
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return patients;
    }

    /**
     * Gets all doctors for a patient
     * @param patientId The ID of the patient
     * @return A list of doctors for the patient
     */
    public java.util.List<User> getDoctorsForPatient(String patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<User> doctors = new java.util.ArrayList<>();

        String query = "SELECT d." + COLUMN_USER_ID + ", d." + COLUMN_USERNAME + ", d." + COLUMN_USER_ROLE +
                " FROM " + TABLE_USERS + " d" +
                " INNER JOIN " + TABLE_DOCTOR_PATIENTS + " dp" +
                " ON d." + COLUMN_USER_ID + " = dp." + COLUMN_DOCTOR_ID_FK +
                " WHERE dp." + COLUMN_PATIENT_ID_FK + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] { patientId });

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range")
                String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));

                Object profile = getUserProfile(userId);
                if (profile instanceof Doctor) {
                    doctors.add(new User(userId, (Doctor) profile, role));
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return doctors;
    }

    /**
     * Saves a medical report to the database
     * @param patientId The ID of the patient
     * @param doctorId The ID of the doctor who created the report
     * @param reportDate The date of the report
     * @param reportContent The content of the report
     * @param reportPath The path to the PDF file
     * @return The ID of the newly inserted report, or -1 if the insertion failed
     */
    public long saveMedicalReport(String patientId, String doctorId, String reportDate, String reportContent, String reportPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_PATIENT_ID_FK, patientId);
            values.put(COLUMN_DOCTOR_ID_FK, doctorId);
            values.put(COLUMN_REPORT_DATE, reportDate);
            values.put(COLUMN_REPORT_CONTENT, reportContent);
            values.put(COLUMN_REPORT_PATH, reportPath);

            db.beginTransaction();
            result = db.insert(TABLE_MEDICAL_REPORTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return result;
    }

    /**
     * Gets all medical reports for a patient
     * @param patientId The ID of the patient
     * @return A list of medical reports for the patient
     */
    public java.util.List<MedicalReport> getMedicalReportsForPatient(String patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<MedicalReport> reports = new java.util.ArrayList<>();

        String[] columns = {
            COLUMN_REPORT_ID,
            COLUMN_PATIENT_ID_FK,
            COLUMN_DOCTOR_ID_FK,
            COLUMN_REPORT_DATE,
            COLUMN_REPORT_CONTENT,
            COLUMN_REPORT_PATH
        };

        String selection = COLUMN_PATIENT_ID_FK + "=?";
        String[] selectionArgs = { patientId };
        String orderBy = COLUMN_REPORT_DATE + " DESC";

        Cursor cursor = db.query(
            TABLE_MEDICAL_REPORTS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String reportId = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_ID));
                @SuppressLint("Range")
                String doctorId = cursor.getString(cursor.getColumnIndex(COLUMN_DOCTOR_ID_FK));
                @SuppressLint("Range")
                String reportDate = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_DATE));
                @SuppressLint("Range")
                String reportContent = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_CONTENT));
                @SuppressLint("Range")
                String reportPath = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_PATH));

                MedicalReport report = new MedicalReport(reportId, patientId, doctorId, reportDate, reportContent, reportPath);
                reports.add(report);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return reports;
    }

    /**
     * Gets the most recent medical report for a patient
     * @param patientId The ID of the patient
     * @return The most recent medical report for the patient, or null if no report exists
     */
    public MedicalReport getLatestMedicalReportForPatient(String patientId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
            COLUMN_REPORT_ID,
            COLUMN_PATIENT_ID_FK,
            COLUMN_DOCTOR_ID_FK,
            COLUMN_REPORT_DATE,
            COLUMN_REPORT_CONTENT,
            COLUMN_REPORT_PATH
        };

        String selection = COLUMN_PATIENT_ID_FK + "=?";
        String[] selectionArgs = { patientId };
        String orderBy = COLUMN_REPORT_DATE + " DESC";
        String limit = "1";

        Cursor cursor = db.query(
            TABLE_MEDICAL_REPORTS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            orderBy,
            limit
        );

        MedicalReport report = null;

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range")
            String reportId = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_ID));
            @SuppressLint("Range")
            String doctorId = cursor.getString(cursor.getColumnIndex(COLUMN_DOCTOR_ID_FK));
            @SuppressLint("Range")
            String reportDate = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_DATE));
            @SuppressLint("Range")
            String reportContent = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_CONTENT));
            @SuppressLint("Range")
            String reportPath = cursor.getString(cursor.getColumnIndex(COLUMN_REPORT_PATH));

            report = new MedicalReport(reportId, patientId, doctorId, reportDate, reportContent, reportPath);
        }

        if (cursor != null) {
            cursor.close();
        }

        return report;
    }

    /**
     * Gets all diseases for a patient, including those added by all doctors assigned to the patient
     * @param patientId The ID of the patient
     * @return A list of diseases for the patient
     */
    public java.util.List<Disease> getDiseasesForPatient(String patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<Disease> diseases = new java.util.ArrayList<>();
        java.util.Set<Integer> addedDiseaseIds = new java.util.HashSet<>();

        String query = "SELECT ud.*, u.username AS doctor_name " +
                "FROM " + TABLE_USER_DISEASES + " ud " +
                "LEFT JOIN " + TABLE_USERS + " u ON ud." + COLUMN_DOCTOR_ID_FK + " = u." + COLUMN_USER_ID + " " +
                "WHERE ud." + COLUMN_USER_ID_FK_DISEASE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{patientId});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int diseaseId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_DISEASE_ID));

                if (addedDiseaseIds.contains(diseaseId)) {
                    continue;
                }

                @SuppressLint("Range")
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_DISEASE_DESCRIPTION));
                @SuppressLint("Range")
                String icd10 = cursor.getString(cursor.getColumnIndex(COLUMN_ICD10));
                @SuppressLint("Range")
                String doctorName = cursor.getString(cursor.getColumnIndex("doctor_name"));
                @SuppressLint("Range")
                String doctorId = cursor.getString(cursor.getColumnIndex(COLUMN_DOCTOR_ID_FK));
                @SuppressLint("Range")
                String diagnosisDate = cursor.getString(cursor.getColumnIndex("diagnosis_date"));

                String formattedDate = "";
                if (diagnosisDate != null && !diagnosisDate.isEmpty()) {
                    try {
                        java.time.format.DateTimeFormatter inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(diagnosisDate, inputFormatter);
                        java.time.format.DateTimeFormatter outputFormatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH);
                        formattedDate = dateTime.format(outputFormatter);
                    } catch (Exception e) {
                        formattedDate = diagnosisDate;
                    }
                }

                Disease disease = new Disease(diseaseId, description, icd10, doctorName, doctorId, formattedDate);
                diseases.add(disease);
                addedDiseaseIds.add(diseaseId);
            }
            cursor.close();
        }

        return diseases;
    }

    /**
     * Saves a notification to the database
     *
     * @param userId  The ID of the user this notification is for
     * @param message The notification message
     * @param type    The type of notification (e.g., "timer_expired", "feedback_submitted")
     */
    public void saveNotification(String userId, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            values.put(COLUMN_NOTIFICATION_USER_ID, userId);
            values.put(COLUMN_NOTIFICATION_MESSAGE, message);
            values.put(COLUMN_NOTIFICATION_DATE, date);
            values.put(COLUMN_NOTIFICATION_READ, 0);
            values.put(COLUMN_NOTIFICATION_TYPE, type);

            db.beginTransaction();
            // Insert the notification into the database
            long result = db.insert(TABLE_NOTIFICATIONS, null, values);
            if (result != -1) {
                android.util.Log.d("DatabaseHelper", "Notification saved successfully for user " + userId);
            } else {
                android.util.Log.e("DatabaseHelper", "Failed to save notification for user " + userId);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error saving notification", e);
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Gets all notifications for a user
     * @param userId The ID of the user
     * @return A list of notifications for the user
     */
    public java.util.List<Notification> getNotificationsForUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<Notification> notifications = new java.util.ArrayList<>();

        String[] columns = {
            COLUMN_NOTIFICATION_ID,
            COLUMN_NOTIFICATION_USER_ID,
            COLUMN_NOTIFICATION_MESSAGE,
            COLUMN_NOTIFICATION_DATE,
            COLUMN_NOTIFICATION_READ,
            COLUMN_NOTIFICATION_TYPE
        };

        String selection = COLUMN_NOTIFICATION_USER_ID + "=?";
        String[] selectionArgs = { userId };
        String orderBy = COLUMN_NOTIFICATION_DATE + " DESC";

        Cursor cursor = db.query(
            TABLE_NOTIFICATIONS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION_ID));
                @SuppressLint("Range")
                String message = cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION_MESSAGE));
                @SuppressLint("Range")
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION_DATE));
                @SuppressLint("Range")
                boolean isRead = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_READ)) == 1;
                @SuppressLint("Range")
                String type = cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION_TYPE));

                Notification notification = new Notification(id, userId, message, date, isRead, type);
                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return notifications;
    }

    /**
     * Deassigns a patient from a doctor
     * @param doctorId The ID of the doctor
     * @param patientId The ID of the patient
     * @return true if the patient was deassigned, false otherwise
     */
    public boolean deassignPatientFromDoctor(String doctorId, String patientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            db.beginTransaction();

            String whereClause = COLUMN_DOCTOR_ID_FK + "=? AND " + COLUMN_PATIENT_ID_FK + "=?";
            String[] whereArgs = { doctorId, patientId };

            rowsAffected = db.delete(TABLE_DOCTOR_PATIENTS, whereClause, whereArgs);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }

    /**
     * Searches for a patient by CNP
     * @param cnp The CNP to search for
     * @return The Patient object with the given CNP, or null if no patient exists
     */
    public Patient getPatientByCnp(String cnp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Patient patient = null;

        String[] columns = {
            COLUMN_USER_ID,
            COLUMN_CNP,
            COLUMN_NAME, 
            COLUMN_AGE, 
            COLUMN_HEIGHT, 
            COLUMN_WEIGHT, 
            COLUMN_LAST_MED_REP,
            COLUMN_BODY_FAT_PERCENTAGE,
            COLUMN_BLOOD_PRESSURE_SYSTOLIC,
            COLUMN_BLOOD_PRESSURE_DIASTOLIC,
            COLUMN_RESTING_HEART_RATE,
            COLUMN_BLOOD_GLUCOSE,
            COLUMN_CHOLESTEROL_TOTAL,
            COLUMN_CHOLESTEROL_HDL,
            COLUMN_CHOLESTEROL_LDL,
            COLUMN_HEALTH_SCORE,
            COLUMN_BODY_TYPE,
            COLUMN_GENDER,
            COLUMN_BMI_INTERPRETATION,
            COLUMN_METABOLIC_INTERPRETATION
        };

        try (Cursor cursor = db.query(TABLE_PATIENTS, columns, COLUMN_CNP + "=?", new String[]{cnp},
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(COLUMN_AGE));
                @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));
                @SuppressLint("Range") float weight = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
                @SuppressLint("Range") String lastMedicalReport = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MED_REP));

                patient = new Patient(name, age, height, weight, lastMedicalReport);
                patient.setCnp(cnp);

                @SuppressLint("Range") float bodyFatPercentage = cursor.getFloat(cursor.getColumnIndex(COLUMN_BODY_FAT_PERCENTAGE));
                if (bodyFatPercentage > 0) {
                    patient.setBodyFatPercentage(bodyFatPercentage);
                }

                @SuppressLint("Range") int bloodPressureSystolic = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOOD_PRESSURE_SYSTOLIC));
                if (bloodPressureSystolic > 0) {
                    patient.setBloodPressureSystolic(bloodPressureSystolic);
                }

                @SuppressLint("Range") int bloodPressureDiastolic = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOOD_PRESSURE_DIASTOLIC));
                if (bloodPressureDiastolic > 0) {
                    patient.setBloodPressureDiastolic(bloodPressureDiastolic);
                }

                @SuppressLint("Range") int restingHeartRate = cursor.getInt(cursor.getColumnIndex(COLUMN_RESTING_HEART_RATE));
                if (restingHeartRate > 0) {
                    patient.setRestingHeartRate(restingHeartRate);
                }

                @SuppressLint("Range") float bloodGlucose = cursor.getFloat(cursor.getColumnIndex(COLUMN_BLOOD_GLUCOSE));
                if (bloodGlucose > 0) {
                    patient.setBloodGlucose(bloodGlucose);
                }

                @SuppressLint("Range") float cholesterolTotal = cursor.getFloat(cursor.getColumnIndex(COLUMN_CHOLESTEROL_TOTAL));
                if (cholesterolTotal > 0) {
                    patient.setCholesterolTotal(cholesterolTotal);
                }

                @SuppressLint("Range") float cholesterolHDL = cursor.getFloat(cursor.getColumnIndex(COLUMN_CHOLESTEROL_HDL));
                if (cholesterolHDL > 0) {
                    patient.setCholesterolHDL(cholesterolHDL);
                }

                @SuppressLint("Range") float cholesterolLDL = cursor.getFloat(cursor.getColumnIndex(COLUMN_CHOLESTEROL_LDL));
                if (cholesterolLDL > 0) {
                    patient.setCholesterolLDL(cholesterolLDL);
                }

                @SuppressLint("Range") int healthScore = cursor.getInt(cursor.getColumnIndex(COLUMN_HEALTH_SCORE));
                if (healthScore > 0) {
                    patient.setHealthScore(healthScore);
                }

                @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex(COLUMN_BODY_TYPE));
                if (bodyType != null && !bodyType.isEmpty()) {
                    patient.setBodyType(bodyType);
                }

                @SuppressLint("Range") String gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER));
                if (gender != null && !gender.isEmpty()) {
                    patient.setGender(gender);
                }

                @SuppressLint("Range") String bmiInterpretation = cursor.getString(cursor.getColumnIndex(COLUMN_BMI_INTERPRETATION));
                if (bmiInterpretation != null && !bmiInterpretation.isEmpty()) {
                    patient.setBmiInterpretation(bmiInterpretation);
                }

                @SuppressLint("Range") String metabolicInterpretation = cursor.getString(cursor.getColumnIndex(COLUMN_METABOLIC_INTERPRETATION));
                if (metabolicInterpretation != null && !metabolicInterpretation.isEmpty()) {
                    patient.setMetabolicInterpretation(metabolicInterpretation);
                }
            }
        }
        return patient;
    }

    /**
     * For backward compatibility
     * @param cnp The CNP to search for
     * @return The Patient object with the given CNP, or null if no profile exists
     * @deprecated Use getPatientByCnp instead
     */
    @Deprecated
    public Object getUserProfileByCnp(String cnp) {
        return getPatientByCnp(cnp);
    }
}
