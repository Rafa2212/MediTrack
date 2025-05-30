package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "medications.db";
    private static final int DATABASE_VERSION = 43;
    private static DatabaseHelper instance;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_USER_MEDICATIONS = "user_medications";
    public static final String TABLE_DOCTOR_PATIENTS = "doctor_patients";
    public static final String TABLE_MEDICAL_REPORTS = "medical_reports";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";

    public static final String TABLE_PROFILE = "profile";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_LAST_MED_REP = "last_medical_report";

    public static final String TABLE_DISEASES = "diseases";
    public static final String TABLE_USER_DISEASES = "user_diseases";

    public static final String COLUMN_DISEASE_ID = "id";
    public static final String COLUMN_ICD10 = "icd10";
    public static final String COLUMN_DISEASE_DESCRIPTION = "disease_description";

    public static final String COLUMN_USER_DISEASE_ID = "id";
    public static final String COLUMN_USER_ID_FK_DISEASE = "id_fk_user";
    public static final String COLUMN_DISEASE_ID_FK = "id_fk_disease";

    public static final String TABLE_SHAREDPREF = "sharedpref";
    public static final String COLUMN_SHAREDPREF_ID = "id";
    public static final String COLUMN_SHAREDPREF_USER_ID = "id_user";
    public static final String COLUMN_SHAREDPREF_KEY = "key_string";
    public static final String COLUMN_SHAREDPREF_VALUE = "value";

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

    private static final String CREATE_TABLE_DISEASES = "CREATE TABLE IF NOT EXISTS " + TABLE_DISEASES
            + " (" + COLUMN_DISEASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DISEASE_DESCRIPTION + " TEXT, " + COLUMN_ICD10 + " TEXT)";

    private static final String CREATE_TABLE_USER_DISEASES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_USER_DISEASES + " (" + COLUMN_USER_DISEASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID_FK_DISEASE + " INTEGER, " + COLUMN_DISEASE_ID_FK + " INTEGER, "
            + COLUMN_DOCTOR_ID_FK + " INTEGER, " + "diagnosis_date TEXT, "
            + "FOREIGN KEY (" + COLUMN_USER_ID_FK_DISEASE + ") REFERENCES " + TABLE_USERS + "("
            + COLUMN_USER_ID + "), "
            + "FOREIGN KEY (" + COLUMN_DISEASE_ID_FK + ") REFERENCES " + TABLE_DISEASES + "("
            + COLUMN_DISEASE_ID + "), "
            + "FOREIGN KEY (" + COLUMN_DOCTOR_ID_FK + ") REFERENCES " + TABLE_USERS + "("
            + COLUMN_USER_ID + "))";

    private static final String CREATE_TABLE_PROFILE = "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILE
            + " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + "cnp TEXT, " + COLUMN_NAME + " TEXT, "
            + COLUMN_AGE + " INTEGER, " + COLUMN_HEIGHT + " FLOAT, "
            + COLUMN_WEIGHT + " FLOAT, " + COLUMN_LAST_MED_REP + " TEXT, "
            + "body_fat_percentage FLOAT, "
            + "blood_pressure_systolic INTEGER, "
            + "blood_pressure_diastolic INTEGER, "
            + "resting_heart_rate INTEGER, "
            + "blood_glucose FLOAT, "
            + "cholesterol_total FLOAT, "
            + "cholesterol_hdl FLOAT, "
            + "cholesterol_ldl FLOAT, "
            + "health_score INTEGER, "
            + "body_type TEXT, "
            + "specialty TEXT ) ";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS
            + " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_USERNAME + " TEXT, "
            + COLUMN_PASSWORD + " TEXT, " + COLUMN_USER_ROLE + " TEXT DEFAULT 'patient')";

    private static final String CREATE_TABLE_SHAREDPREF = "CREATE TABLE IF NOT EXISTS "
            + TABLE_SHAREDPREF + " (" + COLUMN_SHAREDPREF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SHAREDPREF_USER_ID + " INTEGER, " + COLUMN_SHAREDPREF_KEY + " TEXT, "
            + COLUMN_SHAREDPREF_VALUE + " TEXT)";

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
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_DISEASES);
        db.execSQL(CREATE_TABLE_USER_DISEASES);
        db.execSQL(CREATE_TABLE_SHAREDPREF);
        db.execSQL(CREATE_TABLE_DOCTOR_PATIENTS);
        db.execSQL(CREATE_TABLE_MEDICAL_REPORTS);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For version 32, add body_type column to profile table
        if (oldVersion < 32 && newVersion >= 32) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN body_type TEXT");
            } catch (Exception e) {
                // Column might already exist, ignore
            }
        } else {
            // Full database rebuild for other version changes
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_MEDICATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISEASES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DISEASES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHAREDPREF);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTOR_PATIENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAL_REPORTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);

            onCreate(db);
        }
    }

    public UserProfile getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile userProfile = null;

        String[] columns = {
            "cnp",
            COLUMN_NAME, 
            COLUMN_AGE, 
            COLUMN_HEIGHT, 
            COLUMN_WEIGHT, 
            COLUMN_LAST_MED_REP,
            "body_fat_percentage",
            "blood_pressure_systolic",
            "blood_pressure_diastolic",
            "resting_heart_rate",
            "blood_glucose",
            "cholesterol_total",
            "cholesterol_hdl",
            "cholesterol_ldl",
            "health_score",
            "body_type",
            "specialty"
        };

        Cursor cursor = db.query(TABLE_PROFILE, columns, COLUMN_USER_ID + "=?", new String[] {userId},
                null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String cnp = cursor.getString(cursor.getColumnIndex("cnp"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(COLUMN_AGE));
                @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));
                @SuppressLint("Range") float weight = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
                @SuppressLint("Range") String lastMedicalReport = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MED_REP));

                userProfile = new UserProfile(name, age, height, weight, lastMedicalReport);
                if (cnp != null) {
                    userProfile.setCnp(cnp);
                }

                // Get additional health metrics
                @SuppressLint("Range") float bodyFatPercentage = cursor.getFloat(cursor.getColumnIndex("body_fat_percentage"));
                if (bodyFatPercentage > 0) {
                    userProfile.setBodyFatPercentage(bodyFatPercentage);
                }

                @SuppressLint("Range") int bloodPressureSystolic = cursor.getInt(cursor.getColumnIndex("blood_pressure_systolic"));
                if (bloodPressureSystolic > 0) {
                    userProfile.setBloodPressureSystolic(bloodPressureSystolic);
                }

                @SuppressLint("Range") int bloodPressureDiastolic = cursor.getInt(cursor.getColumnIndex("blood_pressure_diastolic"));
                if (bloodPressureDiastolic > 0) {
                    userProfile.setBloodPressureDiastolic(bloodPressureDiastolic);
                }

                @SuppressLint("Range") int restingHeartRate = cursor.getInt(cursor.getColumnIndex("resting_heart_rate"));
                if (restingHeartRate > 0) {
                    userProfile.setRestingHeartRate(restingHeartRate);
                }

                @SuppressLint("Range") float bloodGlucose = cursor.getFloat(cursor.getColumnIndex("blood_glucose"));
                if (bloodGlucose > 0) {
                    userProfile.setBloodGlucose(bloodGlucose);
                }

                @SuppressLint("Range") float cholesterolTotal = cursor.getFloat(cursor.getColumnIndex("cholesterol_total"));
                if (cholesterolTotal > 0) {
                    userProfile.setCholesterolTotal(cholesterolTotal);
                }

                @SuppressLint("Range") float cholesterolHDL = cursor.getFloat(cursor.getColumnIndex("cholesterol_hdl"));
                if (cholesterolHDL > 0) {
                    userProfile.setCholesterolHDL(cholesterolHDL);
                }

                @SuppressLint("Range") float cholesterolLDL = cursor.getFloat(cursor.getColumnIndex("cholesterol_ldl"));
                if (cholesterolLDL > 0) {
                    userProfile.setCholesterolLDL(cholesterolLDL);
                }

                @SuppressLint("Range") int healthScore = cursor.getInt(cursor.getColumnIndex("health_score"));
                if (healthScore > 0) {
                    userProfile.setHealthScore(healthScore);
                }

                @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex("body_type"));
                if (bodyType != null && !bodyType.isEmpty()) {
                    userProfile.setBodyType(bodyType);
                }

                @SuppressLint("Range") String specialty = cursor.getString(cursor.getColumnIndex("specialty"));
                if (specialty != null && !specialty.isEmpty()) {
                    userProfile.setSpecialty(specialty);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userProfile;
    }

    public void insertOrUpdateProfile(String userId, UserProfile userProfile) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put("cnp", userProfile.getCnp());
            values.put(COLUMN_NAME, userProfile.getName());
            values.put(COLUMN_AGE, userProfile.getAge());
            values.put(COLUMN_HEIGHT, userProfile.getHeight());
            values.put(COLUMN_WEIGHT, userProfile.getWeight());
            values.put(COLUMN_LAST_MED_REP, userProfile.getLastMedicalReport());

            // Add additional health metrics
            values.put("body_fat_percentage", userProfile.getBodyFatPercentage());
            values.put("blood_pressure_systolic", userProfile.getBloodPressureSystolic());
            values.put("blood_pressure_diastolic", userProfile.getBloodPressureDiastolic());
            values.put("resting_heart_rate", userProfile.getRestingHeartRate());
            values.put("blood_glucose", userProfile.getBloodGlucose());
            values.put("cholesterol_total", userProfile.getCholesterolTotal());
            values.put("cholesterol_hdl", userProfile.getCholesterolHDL());
            values.put("cholesterol_ldl", userProfile.getCholesterolLDL());
            values.put("health_score", userProfile.getHealthScore());
            values.put("body_type", userProfile.getBodyType());
            values.put("specialty", userProfile.getSpecialty());

            cursor = db.query(
                    TABLE_PROFILE, null, COLUMN_USER_ID + "=?", new String[] {userId}, null, null, null);

            db.beginTransaction();

            if (cursor != null && cursor.getCount() > 0) {
                db.update(TABLE_PROFILE, values, COLUMN_USER_ID + "=?", new String[] {userId});
            } else {
                db.insert(TABLE_PROFILE, null, values);
            }

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction(); // End the transaction
        }
    }

    public User checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
                COLUMN_USER_ROLE
        };

        Cursor cursor =
                db.query(TABLE_USERS, columns, COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                        new String[] {username, password}, null, null, null);

        if (cursor.moveToFirst()) {
            @SuppressLint("Range")
            String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
            @SuppressLint("Range")
            String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));
            UserProfile userProfile = getUserProfile(userId);
            cursor.close();
            return new User(userId, userProfile, role);
        }
        cursor.close();
        return null;
    }

    /**
     * This method is deprecated and should not be used for regular user registration.
     * Use addTestUser for test purposes only.
     */
    public long addUser(String username, String password) {
        // This method is intentionally disabled to prevent regular user registration
        // Only test users created by AddDoctorTest class should be used
        return -1;
    }

    /**
     * This method is deprecated and should not be used for regular user registration.
     * Use addTestUser for test purposes only.
     */
    public long addUser(String username, String password, String role) {
        // This method is intentionally disabled to prevent regular user registration
        // Only test users created by AddDoctorTest class should be used
        return -1;
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
            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End the transaction
        }

        return result;
    }

    /**
     * Adds a doctor user to the database. This method should only be used by the AddDoctorTest class.
     */
    public long addDoctor(String username, String password) {
        return addTestUser(username, password, "doctor");
    }

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
            UserProfile userProfile = getUserProfile(userId);
            cursor.close();
            return new User(userId, userProfile, role);
        }
        cursor.close();
        return null;
    }

    public long insertOnSession(String userId, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_SHAREDPREF_USER_ID, userId);
        contentValues.put(DatabaseHelper.COLUMN_SHAREDPREF_KEY, key);
        contentValues.put(DatabaseHelper.COLUMN_SHAREDPREF_VALUE, value);

        return db.insert(DatabaseHelper.TABLE_SHAREDPREF, null, contentValues);
    }

    public Session getSession(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SHAREDPREF,
                new String[] {DatabaseHelper.COLUMN_SHAREDPREF_ID, DatabaseHelper.COLUMN_SHAREDPREF_USER_ID,
                        DatabaseHelper.COLUMN_SHAREDPREF_KEY, DatabaseHelper.COLUMN_SHAREDPREF_VALUE},
                DatabaseHelper.COLUMN_SHAREDPREF_ID + "=?", new String[] {String.valueOf(id)}, null, null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range")
            Session session =
                    new Session(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SHAREDPREF_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SHAREDPREF_KEY)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SHAREDPREF_VALUE)));
            cursor.close();
            return session;
        }

        return null;
    }

    public java.util.List<User> getAllDoctors() {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<User> doctors = new java.util.ArrayList<>();

        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_PASSWORD,
                COLUMN_USER_ROLE
        };

        Cursor cursor = db.query(
                TABLE_USERS, columns, COLUMN_USER_ROLE + "=?", new String[] {"doctor"}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range")
                String role = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ROLE));
                UserProfile userProfile = getUserProfile(userId);
                doctors.add(new User(userId, userProfile, role));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return doctors;
    }

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

    public long assignPatientToDoctor(String doctorId, String patientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            // Check if the doctor is actually a doctor
            if (!isDoctor(doctorId)) {
                return -1;
            }

            // Check if the patient is actually a patient
            if (isDoctor(patientId)) {
                return -1;
            }

            // Check if the assignment already exists
            if (isPatientAssignedToDoctor(doctorId, patientId)) {
                return -1;
            }

            // Removed specialty check to allow patients to be assigned to multiple doctors
            // regardless of specialty

            ContentValues values = new ContentValues();
            values.put(COLUMN_DOCTOR_ID_FK, doctorId);
            values.put(COLUMN_PATIENT_ID_FK, patientId);

            db.beginTransaction();
            result = db.insert(TABLE_DOCTOR_PATIENTS, null, values);
            db.setTransactionSuccessful(); // This is crucial - without it, changes won't be committed
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return result;
    }

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

    public java.util.List<User> getPatientsForDoctor(String doctorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<User> patients = new java.util.ArrayList<>();

        // Check if the doctor is actually a doctor
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
                UserProfile userProfile = getUserProfile(userId);
                patients.add(new User(userId, userProfile, role));
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
                UserProfile userProfile = getUserProfile(userId);
                doctors.add(new User(userId, userProfile, role));
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
            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End the transaction
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

        // Query to get all diseases directly linked to the patient
        String query = "SELECT ud.*, d.*, u.username AS doctor_name, ud." + COLUMN_DOCTOR_ID_FK + " AS doctor_id " +
                "FROM " + TABLE_USER_DISEASES + " ud " +
                "INNER JOIN " + TABLE_DISEASES + " d ON ud." + COLUMN_DISEASE_ID_FK + " = d." + COLUMN_DISEASE_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON ud." + COLUMN_DOCTOR_ID_FK + " = u." + COLUMN_USER_ID + " " +
                "WHERE ud." + COLUMN_USER_ID_FK_DISEASE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{patientId});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int diseaseId = cursor.getInt(cursor.getColumnIndex(COLUMN_DISEASE_ID_FK));

                // Skip if we've already added this disease
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
                String doctorId = cursor.getString(cursor.getColumnIndex("doctor_id"));
                @SuppressLint("Range")
                String diagnosisDate = cursor.getString(cursor.getColumnIndex("diagnosis_date"));

                // Format the date if it exists
                String formattedDate = "";
                if (diagnosisDate != null && !diagnosisDate.isEmpty()) {
                    try {
                        java.time.format.DateTimeFormatter inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(diagnosisDate, inputFormatter);
                        java.time.format.DateTimeFormatter outputFormatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH);
                        formattedDate = dateTime.format(outputFormatter);
                    } catch (Exception e) {
                        formattedDate = diagnosisDate; // Fallback to original string if parsing fails
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
     * @param userId The ID of the user this notification is for
     * @param message The notification message
     * @param type The type of notification (e.g., "timer_expired", "feedback_submitted")
     * @return The ID of the newly inserted notification, or -1 if the insertion failed
     */
    public long saveNotification(String userId, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();

            // Get current timestamp for the notification
            String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            values.put(COLUMN_NOTIFICATION_USER_ID, userId);
            values.put(COLUMN_NOTIFICATION_MESSAGE, message);
            values.put(COLUMN_NOTIFICATION_DATE, date);
            values.put(COLUMN_NOTIFICATION_READ, 0); // 0 = false, 1 = true
            values.put(COLUMN_NOTIFICATION_TYPE, type);

            db.beginTransaction();
            result = db.insert(TABLE_NOTIFICATIONS, null, values);
            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End the transaction
        }

        return result;
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
     * Gets unread notifications for a user
     * @param userId The ID of the user
     * @return A list of unread notifications for the user
     */
    public java.util.List<Notification> getUnreadNotificationsForUser(String userId) {
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

        String selection = COLUMN_NOTIFICATION_USER_ID + "=? AND " + COLUMN_NOTIFICATION_READ + "=?";
        String[] selectionArgs = { userId, "0" };
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
     * Marks a notification as read
     * @param notificationId The ID of the notification
     * @return true if the notification was marked as read, false otherwise
     */
    public boolean markNotificationAsRead(String notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOTIFICATION_READ, 1); // 1 = true

            String whereClause = COLUMN_NOTIFICATION_ID + "=?";
            String[] whereArgs = { notificationId };

            db.beginTransaction();
            rowsAffected = db.update(TABLE_NOTIFICATIONS, values, whereClause, whereArgs);
            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End the transaction
        }

        return rowsAffected > 0;
    }

    /**
     * Deletes a notification
     * @param notificationId The ID of the notification
     * @return true if the notification was deleted, false otherwise
     */
    public boolean deleteNotification(String notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            String whereClause = COLUMN_NOTIFICATION_ID + "=?";
            String[] whereArgs = { notificationId };

            db.beginTransaction();
            rowsAffected = db.delete(TABLE_NOTIFICATIONS, whereClause, whereArgs);
            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End the transaction
        }

        return rowsAffected > 0;
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

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End the transaction
        }

        return rowsAffected > 0;
    }

    /**
     * Searches for a user profile by CNP
     * @param cnp The CNP to search for
     * @return The user profile with the given CNP, or null if no profile exists
     */
    public UserProfile getUserProfileByCnp(String cnp) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile userProfile = null;

        String[] columns = {
            COLUMN_USER_ID,
            "cnp",
            COLUMN_NAME, 
            COLUMN_AGE, 
            COLUMN_HEIGHT, 
            COLUMN_WEIGHT, 
            COLUMN_LAST_MED_REP,
            "body_fat_percentage",
            "blood_pressure_systolic",
            "blood_pressure_diastolic",
            "resting_heart_rate",
            "blood_glucose",
            "cholesterol_total",
            "cholesterol_hdl",
            "cholesterol_ldl",
            "health_score",
            "body_type",
            "specialty"
        };

        Cursor cursor = db.query(TABLE_PROFILE, columns, "cnp=?", new String[] {cnp},
                null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(COLUMN_AGE));
                @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));
                @SuppressLint("Range") float weight = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
                @SuppressLint("Range") String lastMedicalReport = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MED_REP));

                userProfile = new UserProfile(name, age, height, weight, lastMedicalReport);
                userProfile.setCnp(cnp);

                // Get additional health metrics
                @SuppressLint("Range") float bodyFatPercentage = cursor.getFloat(cursor.getColumnIndex("body_fat_percentage"));
                if (bodyFatPercentage > 0) {
                    userProfile.setBodyFatPercentage(bodyFatPercentage);
                }

                @SuppressLint("Range") int bloodPressureSystolic = cursor.getInt(cursor.getColumnIndex("blood_pressure_systolic"));
                if (bloodPressureSystolic > 0) {
                    userProfile.setBloodPressureSystolic(bloodPressureSystolic);
                }

                @SuppressLint("Range") int bloodPressureDiastolic = cursor.getInt(cursor.getColumnIndex("blood_pressure_diastolic"));
                if (bloodPressureDiastolic > 0) {
                    userProfile.setBloodPressureDiastolic(bloodPressureDiastolic);
                }

                @SuppressLint("Range") int restingHeartRate = cursor.getInt(cursor.getColumnIndex("resting_heart_rate"));
                if (restingHeartRate > 0) {
                    userProfile.setRestingHeartRate(restingHeartRate);
                }

                @SuppressLint("Range") float bloodGlucose = cursor.getFloat(cursor.getColumnIndex("blood_glucose"));
                if (bloodGlucose > 0) {
                    userProfile.setBloodGlucose(bloodGlucose);
                }

                @SuppressLint("Range") float cholesterolTotal = cursor.getFloat(cursor.getColumnIndex("cholesterol_total"));
                if (cholesterolTotal > 0) {
                    userProfile.setCholesterolTotal(cholesterolTotal);
                }

                @SuppressLint("Range") float cholesterolHDL = cursor.getFloat(cursor.getColumnIndex("cholesterol_hdl"));
                if (cholesterolHDL > 0) {
                    userProfile.setCholesterolHDL(cholesterolHDL);
                }

                @SuppressLint("Range") float cholesterolLDL = cursor.getFloat(cursor.getColumnIndex("cholesterol_ldl"));
                if (cholesterolLDL > 0) {
                    userProfile.setCholesterolLDL(cholesterolLDL);
                }

                @SuppressLint("Range") int healthScore = cursor.getInt(cursor.getColumnIndex("health_score"));
                if (healthScore > 0) {
                    userProfile.setHealthScore(healthScore);
                }

                @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex("body_type"));
                if (bodyType != null && !bodyType.isEmpty()) {
                    userProfile.setBodyType(bodyType);
                }

                @SuppressLint("Range") String specialty = cursor.getString(cursor.getColumnIndex("specialty"));
                if (specialty != null && !specialty.isEmpty()) {
                    userProfile.setSpecialty(specialty);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userProfile;
    }
}
