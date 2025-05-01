package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to add a doctor user for testing purposes.
 * This class can be used from the main activity or any other activity to add a doctor user.
 */
public class AddDoctorTest {
    private static final String TAG = "AddDoctorTest";

    /**
     * Adds doctor users to the database for testing purposes.
     * @param context The context to use for database access
     * @return true if the doctors were added successfully, false otherwise
     */
    public static boolean addDoctorForTesting(Context context) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            // First, create all patients if they don't exist
            createAllPatientsIfNeeded(context);

            // Check if doctor1 already exists
            User doctor1 = dbHelper.checkUser("doctor", "password");
            String doctorId1 = "";

            if (doctor1 == null) {
                // Add first doctor if it doesn't exist
                long newDoctorId1 = dbHelper.addDoctor("doctor", "password");
                if (newDoctorId1 != -1) {
                    doctorId1 = String.valueOf(newDoctorId1);
                    Log.d(TAG, "Doctor 1 added successfully with ID: " + doctorId1);

                    // Create a simple profile for the doctor
                    UserProfile doctorProfile1 = new UserProfile("Dr. Smith", 45, 180, 75, "Medical doctor");
                    dbHelper.insertOrUpdateProfile(doctorId1, doctorProfile1);
                    Log.d(TAG, "Doctor 1 profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add doctor 1");
                    return false;
                }
            } else {
                doctorId1 = doctor1.getUserId();
                Log.d(TAG, "Doctor 1 already exists with ID: " + doctorId1);
            }

            // Always assign patients to doctor1, whether it was just created or already existed
            if (!doctorId1.isEmpty()) {
                // Assign patient1 and patient2 to doctor1
                assignPatientsToDoctor1(context, doctorId1);
            }

            // Check if doctor2 already exists
            User doctor2 = dbHelper.checkUser("doctor2", "password");
            String doctorId2 = "";

            if (doctor2 == null) {
                // Add second doctor if it doesn't exist
                long newDoctorId2 = dbHelper.addDoctor("doctor2", "password");
                if (newDoctorId2 != -1) {
                    doctorId2 = String.valueOf(newDoctorId2);
                    Log.d(TAG, "Doctor 2 added successfully with ID: " + doctorId2);

                    // Create a simple profile for the doctor
                    UserProfile doctorProfile2 = new UserProfile("Dr. Johnson", 52, 175, 80, "Cardiologist");
                    dbHelper.insertOrUpdateProfile(doctorId2, doctorProfile2);
                    Log.d(TAG, "Doctor 2 profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add doctor 2");
                }
            } else {
                doctorId2 = doctor2.getUserId();
                Log.d(TAG, "Doctor 2 already exists with ID: " + doctorId2);
            }

            // Always assign patients to doctor2, whether it was just created or already existed
            if (!doctorId2.isEmpty()) {
                // Assign patient3 and patient4 to doctor2
                assignPatientsToDoctor2(context, doctorId2);
            }

            // Check if doctor3 already exists
            User doctor3 = dbHelper.checkUser("doctor3", "password");
            String doctorId3 = "";

            if (doctor3 == null) {
                // Add third doctor if it doesn't exist
                long newDoctorId3 = dbHelper.addDoctor("doctor3", "password");
                if (newDoctorId3 != -1) {
                    doctorId3 = String.valueOf(newDoctorId3);
                    Log.d(TAG, "Doctor 3 added successfully with ID: " + doctorId3);

                    // Create a simple profile for the doctor
                    UserProfile doctorProfile3 = new UserProfile("Dr. Williams", 38, 170, 65, "Neurologist");
                    dbHelper.insertOrUpdateProfile(doctorId3, doctorProfile3);
                    Log.d(TAG, "Doctor 3 profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add doctor 3");
                }
            } else {
                doctorId3 = doctor3.getUserId();
                Log.d(TAG, "Doctor 3 already exists with ID: " + doctorId3);
            }

            // Always assign patients to doctor3, whether it was just created or already existed
            if (!doctorId3.isEmpty()) {
                // Assign patient5, patient6, and patient7 to doctor3
                assignPatientsToDoctor3(context, doctorId3);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding doctors: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates all patients if they don't exist yet.
     * @param context The context to use for database access
     */
    private static void createAllPatientsIfNeeded(Context context) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            // Check if patient1 already exists
            User existingPatient1 = dbHelper.checkUser("patient1", "password");
            long patient1Id = -1;
            if (existingPatient1 != null) {
                Log.d(TAG, "Patient 1 already exists");
                patient1Id = Long.parseLong(existingPatient1.getUserId());
            } else {
                // Add patient 1 only if it doesn't exist
                patient1Id = dbHelper.addTestUser("patient1", "password", "patient");
                Log.d(TAG, "Patient 1 added with ID: " + patient1Id);
            }
            if (patient1Id != -1) {
                UserProfile patient1Profile = new UserProfile("John Doe", 35, 175, 70, "Healthy");

                // Set additional health metrics
                patient1Profile.setBodyFatPercentage(18.5f);
                patient1Profile.setRestingHeartRate(68);
                patient1Profile.setBloodPressureSystolic(120);
                patient1Profile.setBloodPressureDiastolic(80);
                patient1Profile.setBloodGlucose(95.0f);
                patient1Profile.setCholesterolTotal(180.0f);
                patient1Profile.setCholesterolHDL(55.0f);
                patient1Profile.setCholesterolLDL(110.0f);
                patient1Profile.setHealthScore(85);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient1Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient1Id), patient1Profile);
                Log.d(TAG, "Patient 1 profile updated");
            }

            // Check if patient2 already exists
            User existingPatient2 = dbHelper.checkUser("patient2", "password");
            long patient2Id = -1;
            if (existingPatient2 != null) {
                Log.d(TAG, "Patient 2 already exists");
                patient2Id = Long.parseLong(existingPatient2.getUserId());
            } else {
                // Add patient 2 only if it doesn't exist
                patient2Id = dbHelper.addTestUser("patient2", "password", "patient");
                Log.d(TAG, "Patient 2 added with ID: " + patient2Id);
            }
            if (patient2Id != -1) {
                UserProfile patient2Profile = new UserProfile("Jane Smith", 28, 165, 60, "Mild hypertension");

                // Set additional health metrics
                patient2Profile.setBodyFatPercentage(22.0f);
                patient2Profile.setRestingHeartRate(72);
                patient2Profile.setBloodPressureSystolic(135);
                patient2Profile.setBloodPressureDiastolic(85);
                patient2Profile.setBloodGlucose(100.0f);
                patient2Profile.setCholesterolTotal(190.0f);
                patient2Profile.setCholesterolHDL(50.0f);
                patient2Profile.setCholesterolLDL(120.0f);
                patient2Profile.setHealthScore(75);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient2Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient2Id), patient2Profile);
                Log.d(TAG, "Patient 2 profile updated");
            }

            // Check if patient3 already exists
            User existingPatient3 = dbHelper.checkUser("patient3", "password");
            long patient3Id = -1;
            if (existingPatient3 != null) {
                Log.d(TAG, "Patient 3 already exists");
                patient3Id = Long.parseLong(existingPatient3.getUserId());
            } else {
                // Add patient 3 only if it doesn't exist
                patient3Id = dbHelper.addTestUser("patient3", "password", "patient");
                Log.d(TAG, "Patient 3 added with ID: " + patient3Id);
            }
            if (patient3Id != -1) {
                UserProfile patient3Profile = new UserProfile("Bob Johnson", 42, 180, 85, "Type 2 diabetes");

                // Set additional health metrics
                patient3Profile.setBodyFatPercentage(25.0f);
                patient3Profile.setRestingHeartRate(75);
                patient3Profile.setBloodPressureSystolic(140);
                patient3Profile.setBloodPressureDiastolic(90);
                patient3Profile.setBloodGlucose(130.0f);
                patient3Profile.setCholesterolTotal(210.0f);
                patient3Profile.setCholesterolHDL(45.0f);
                patient3Profile.setCholesterolLDL(140.0f);
                patient3Profile.setHealthScore(65);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient3Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient3Id), patient3Profile);
                Log.d(TAG, "Patient 3 profile updated");
            }

            // Check if patient4 already exists
            User existingPatient4 = dbHelper.checkUser("patient4", "password");
            long patient4Id = -1;
            if (existingPatient4 != null) {
                Log.d(TAG, "Patient 4 already exists");
                patient4Id = Long.parseLong(existingPatient4.getUserId());
            } else {
                // Add patient 4 only if it doesn't exist
                patient4Id = dbHelper.addTestUser("patient4", "password", "patient");
                Log.d(TAG, "Patient 4 added with ID: " + patient4Id);
            }
            if (patient4Id != -1) {
                UserProfile patient4Profile = new UserProfile("Emily Davis", 31, 162, 58, "Asthma");

                // Set additional health metrics
                patient4Profile.setBodyFatPercentage(20.0f);
                patient4Profile.setRestingHeartRate(70);
                patient4Profile.setBloodPressureSystolic(118);
                patient4Profile.setBloodPressureDiastolic(75);
                patient4Profile.setBloodGlucose(92.0f);
                patient4Profile.setCholesterolTotal(175.0f);
                patient4Profile.setCholesterolHDL(60.0f);
                patient4Profile.setCholesterolLDL(100.0f);
                patient4Profile.setHealthScore(80);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient4Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient4Id), patient4Profile);
                Log.d(TAG, "Patient 4 profile updated");
            }

            // Check if patient5 already exists
            User existingPatient5 = dbHelper.checkUser("patient5", "password");
            long patient5Id = -1;
            if (existingPatient5 != null) {
                Log.d(TAG, "Patient 5 already exists");
                patient5Id = Long.parseLong(existingPatient5.getUserId());
            } else {
                // Add patient 5 only if it doesn't exist
                patient5Id = dbHelper.addTestUser("patient5", "password", "patient");
                Log.d(TAG, "Patient 5 added with ID: " + patient5Id);
            }
            if (patient5Id != -1) {
                UserProfile patient5Profile = new UserProfile("Michael Wilson", 45, 178, 82, "High cholesterol");

                // Set additional health metrics
                patient5Profile.setBodyFatPercentage(24.0f);
                patient5Profile.setRestingHeartRate(65);
                patient5Profile.setBloodPressureSystolic(125);
                patient5Profile.setBloodPressureDiastolic(82);
                patient5Profile.setBloodGlucose(98.0f);
                patient5Profile.setCholesterolTotal(240.0f);
                patient5Profile.setCholesterolHDL(40.0f);
                patient5Profile.setCholesterolLDL(180.0f);
                patient5Profile.setHealthScore(70);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient5Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient5Id), patient5Profile);
                Log.d(TAG, "Patient 5 profile updated");
            }

            // Check if patient6 already exists
            User existingPatient6 = dbHelper.checkUser("patient6", "password");
            long patient6Id = -1;
            if (existingPatient6 != null) {
                Log.d(TAG, "Patient 6 already exists");
                patient6Id = Long.parseLong(existingPatient6.getUserId());
            } else {
                // Add patient 6 only if it doesn't exist
                patient6Id = dbHelper.addTestUser("patient6", "password", "patient");
                Log.d(TAG, "Patient 6 added with ID: " + patient6Id);
            }
            if (patient6Id != -1) {
                UserProfile patient6Profile = new UserProfile("Sarah Brown", 29, 168, 63, "Migraine");

                // Set additional health metrics
                patient6Profile.setBodyFatPercentage(21.0f);
                patient6Profile.setRestingHeartRate(68);
                patient6Profile.setBloodPressureSystolic(115);
                patient6Profile.setBloodPressureDiastolic(75);
                patient6Profile.setBloodGlucose(90.0f);
                patient6Profile.setCholesterolTotal(170.0f);
                patient6Profile.setCholesterolHDL(65.0f);
                patient6Profile.setCholesterolLDL(95.0f);
                patient6Profile.setHealthScore(90);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient6Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient6Id), patient6Profile);
                Log.d(TAG, "Patient 6 profile updated");
            }

            // Check if patient7 already exists
            User existingPatient7 = dbHelper.checkUser("patient7", "password");
            long patient7Id = -1;
            if (existingPatient7 != null) {
                Log.d(TAG, "Patient 7 already exists");
                patient7Id = Long.parseLong(existingPatient7.getUserId());
            } else {
                // Add patient 7 only if it doesn't exist
                patient7Id = dbHelper.addTestUser("patient7", "password", "patient");
                Log.d(TAG, "Patient 7 added with ID: " + patient7Id);
            }
            if (patient7Id != -1) {
                UserProfile patient7Profile = new UserProfile("David Miller", 52, 183, 90, "Arthritis");

                // Set additional health metrics
                patient7Profile.setBodyFatPercentage(26.0f);
                patient7Profile.setRestingHeartRate(72);
                patient7Profile.setBloodPressureSystolic(130);
                patient7Profile.setBloodPressureDiastolic(85);
                patient7Profile.setBloodGlucose(105.0f);
                patient7Profile.setCholesterolTotal(200.0f);
                patient7Profile.setCholesterolHDL(48.0f);
                patient7Profile.setCholesterolLDL(135.0f);
                patient7Profile.setHealthScore(60);

                // Set last medical report date to more than a week ago to reset the timer
                LocalDateTime pastDate = LocalDateTime.now().minusWeeks(2);
                patient7Profile.setLastMedicalReport(pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

                dbHelper.insertOrUpdateProfile(String.valueOf(patient7Id), patient7Profile);
                Log.d(TAG, "Patient 7 profile updated");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating patients: " + e.getMessage());
        }
    }

    /**
     * Assigns patient1 and patient2 to doctor1.
     * @param context The context to use for database access
     * @param doctorId The ID of doctor1
     */
    private static void assignPatientsToDoctor1(Context context, String doctorId) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            // Get patient1
            User patient1 = dbHelper.checkUser("patient1", "password");
            if (patient1 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient1.getUserId());
                Log.d(TAG, "Patient 1 assigned to doctor1");
            }

            // Get patient2
            User patient2 = dbHelper.checkUser("patient2", "password");
            if (patient2 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient2.getUserId());
                Log.d(TAG, "Patient 2 assigned to doctor1");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error assigning patients to doctor1: " + e.getMessage());
        }
    }

    /**
     * Assigns patient3 and patient4 to doctor2.
     * @param context The context to use for database access
     * @param doctorId The ID of doctor2
     */
    private static void assignPatientsToDoctor2(Context context, String doctorId) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            // Get patient3
            User patient3 = dbHelper.checkUser("patient3", "password");
            if (patient3 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient3.getUserId());
                Log.d(TAG, "Patient 3 assigned to doctor2");
            }

            // Get patient4
            User patient4 = dbHelper.checkUser("patient4", "password");
            if (patient4 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient4.getUserId());
                Log.d(TAG, "Patient 4 assigned to doctor2");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error assigning patients to doctor2: " + e.getMessage());
        }
    }

    /**
     * Assigns patient5, patient6, and patient7 to doctor3.
     * @param context The context to use for database access
     * @param doctorId The ID of doctor3
     */
    private static void assignPatientsToDoctor3(Context context, String doctorId) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            // Get patient5
            User patient5 = dbHelper.checkUser("patient5", "password");
            if (patient5 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient5.getUserId());
                Log.d(TAG, "Patient 5 assigned to doctor3");
            }

            // Get patient6
            User patient6 = dbHelper.checkUser("patient6", "password");
            if (patient6 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient6.getUserId());
                Log.d(TAG, "Patient 6 assigned to doctor3");
            }

            // Get patient7
            User patient7 = dbHelper.checkUser("patient7", "password");
            if (patient7 != null) {
                dbHelper.assignPatientToDoctor(doctorId, patient7.getUserId());
                Log.d(TAG, "Patient 7 assigned to doctor3");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error assigning patients to doctor3: " + e.getMessage());
        }
    }

}
