package com.example.myapplication;

import android.content.Context;
import android.util.Log;

/**
 * Utility class to add a doctor user for testing purposes.
 * This class can be used from the main activity or any other activity to add a doctor user.
 */
public class UserTest {
    private static final String TAG = "UserTest";

    /**
     * Adds doctor users to the database for testing purposes.
     *
     * @param context The context to use for database access
     */
    public static void addDoctorForTesting(Context context) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            addPatientsForTesting(context);

            addSpecializedDoctors(context);

            dbHelper.assignPatientToDoctor("14", "19");

            User medicDoctor = dbHelper.checkUser("medic", "password");
            String medicDoctorId = "";

            if (medicDoctor == null) {
                long newMedicDoctorId = dbHelper.addDoctor("medic", "password");
                if (newMedicDoctorId != -1) {
                    medicDoctorId = String.valueOf(newMedicDoctorId);
                    Log.d(TAG, "Medic doctor added successfully with ID: " + medicDoctorId);

                    Doctor medicDoctorProfile = new Doctor("Medic", "General Practitioner");
                    dbHelper.insertOrUpdateProfile(medicDoctorId, medicDoctorProfile);
                    Log.d(TAG, "Medic doctor profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add medic doctor");
                }
            } else {
                medicDoctorId = medicDoctor.getUserId();
                Log.d(TAG, "Medic doctor already exists with ID: " + medicDoctorId);

                Doctor medicDoctorProfile = new Doctor("Medic", "General Practitioner");
                dbHelper.insertOrUpdateProfile(medicDoctorId, medicDoctorProfile);
                Log.d(TAG, "Medic doctor profile updated to ensure name is 'Medic'");
            }

            if (!medicDoctorId.isEmpty()) {
                java.util.List<User> medicPatients = dbHelper.getPatientsForDoctor(medicDoctorId);

                for (User patient : medicPatients) {
                    boolean success = dbHelper.deassignPatientFromDoctor(medicDoctorId, patient.getUserId());
                    if (success) {
                        Log.d(TAG, "Patient " + patient.getUserId() + " deassigned from medic doctor");
                    } else {
                        Log.e(TAG, "Failed to deassign patient " + patient.getUserId() + " from medic doctor");
                    }
                }

                Log.d(TAG, "All patients removed from medic doctor");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error adding doctors: " + e.getMessage());
        }
    }

    /**
     * Adds the three specialized doctors: cardio, pneumo, and nutri
     * @param context The context to use for database access
     */
    private static void addSpecializedDoctors(Context context) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            User cardioDoctor = dbHelper.checkUser("cardio", "password");
            String cardioId = "";

            if (cardioDoctor == null) {
                long newCardioId = dbHelper.addDoctor("cardio", "password");
                if (newCardioId != -1) {
                    cardioId = String.valueOf(newCardioId);
                    Log.d(TAG, "Cardio doctor added successfully with ID: " + cardioId);

                    Doctor cardioProfile = new Doctor("Dr. Cardio", "Cardiologist");
                    dbHelper.insertOrUpdateProfile(cardioId, cardioProfile);
                    Log.d(TAG, "Cardio doctor profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add cardio doctor");
                }
            } else {
                cardioId = cardioDoctor.getUserId();
                Log.d(TAG, "Cardio doctor already exists with ID: " + cardioId);

                Doctor cardioProfile = new Doctor("Dr. Cardio", "Cardiologist");
                dbHelper.insertOrUpdateProfile(cardioId, cardioProfile);
                Log.d(TAG, "Cardio doctor profile updated");
            }

            User pneumoDoctor = dbHelper.checkUser("pneumo", "password");
            String pneumoId = "";

            if (pneumoDoctor == null) {
                long newPneumoId = dbHelper.addDoctor("pneumo", "password");
                if (newPneumoId != -1) {
                    pneumoId = String.valueOf(newPneumoId);
                    Log.d(TAG, "Pneumo doctor added successfully with ID: " + pneumoId);

                    Doctor pneumoProfile = new Doctor("Dr. Pneumo", "Pneumologist");
                    dbHelper.insertOrUpdateProfile(pneumoId, pneumoProfile);
                    Log.d(TAG, "Pneumo doctor profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add pneumo doctor");
                }
            } else {
                pneumoId = pneumoDoctor.getUserId();
                Log.d(TAG, "Pneumo doctor already exists with ID: " + pneumoId);

                Doctor pneumoProfile = new Doctor("Dr. Pneumo", "Pneumologist");
                dbHelper.insertOrUpdateProfile(pneumoId, pneumoProfile);
                Log.d(TAG, "Pneumo doctor profile updated");
            }

            // Add nutritionist (nutri)
            User nutriDoctor = dbHelper.checkUser("nutri", "password");
            String nutriId = "";

            if (nutriDoctor == null) {
                long newNutriId = dbHelper.addDoctor("nutri", "password");
                if (newNutriId != -1) {
                    nutriId = String.valueOf(newNutriId);
                    Log.d(TAG, "Nutri doctor added successfully with ID: " + nutriId);

                    Doctor nutriProfile = new Doctor("Dr. Nutri", "Nutritionist");
                    dbHelper.insertOrUpdateProfile(nutriId, nutriProfile);
                    Log.d(TAG, "Nutri doctor profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add nutri doctor");
                }
            } else {
                nutriId = nutriDoctor.getUserId();
                Log.d(TAG, "Nutri doctor already exists with ID: " + nutriId);

                Doctor nutriProfile = new Doctor("Dr. Nutri", "Nutritionist");
                dbHelper.insertOrUpdateProfile(nutriId, nutriProfile);
                Log.d(TAG, "Nutri doctor profile updated");
            }

            assignPatientsToSpecializedDoctors(context, cardioId, pneumoId, nutriId);

        } catch (Exception e) {
            Log.e(TAG, "Error adding specialized doctors: " + e.getMessage());
        }
    }

    /**
     * Adds test patients to the database for testing purposes.
     *
     * @param context The context to use for database access
     */
    public static void addPatientsForTesting(Context context) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            User patient1 = dbHelper.checkUser("patient1", "password");
            String patient1Id = "";

            if (patient1 == null) {
                long newPatient1Id = dbHelper.addTestUser("patient1", "password", "patient");
                if (newPatient1Id != -1) {
                    patient1Id = String.valueOf(newPatient1Id);
                    Log.d(TAG, "Patient 1 added successfully with ID: " + patient1Id);

                    Patient patient1Profile = new Patient("John Doe", 55, 175, 85, "Hypertension, High cholesterol");
                    patient1Profile.setCnp("1234567890123");
                    patient1Profile.setBloodPressureSystolic(145);
                    patient1Profile.setBloodPressureDiastolic(95);
                    patient1Profile.setRestingHeartRate(78);
                    patient1Profile.setCholesterolTotal(240);
                    patient1Profile.setCholesterolHDL(45);
                    patient1Profile.setCholesterolLDL(160);
                    patient1Profile.setGender("male");
                    // Calculate health score using the HealthScoreCalculator
                    int healthScore = HealthScoreCalculator.calculateHealthScore(patient1Profile);
                    patient1Profile.setHealthScore(healthScore);
                    dbHelper.insertOrUpdateProfile(patient1Id, patient1Profile);
                    Log.d(TAG, "Patient 1 profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add patient 1");
                }
            } else {
                patient1Id = patient1.getUserId();
                Log.d(TAG, "Patient 1 already exists with ID: " + patient1Id);

                Patient patient1Profile = new Patient("John Doe", 55, 175, 85, "Hypertension, High cholesterol");
                patient1Profile.setCnp("1234567890123");
                patient1Profile.setBloodPressureSystolic(145);
                patient1Profile.setBloodPressureDiastolic(95);
                patient1Profile.setRestingHeartRate(78);
                patient1Profile.setCholesterolTotal(240);
                patient1Profile.setCholesterolHDL(45);
                patient1Profile.setCholesterolLDL(160);
                patient1Profile.setGender("male");
                // Calculate health score using the HealthScoreCalculator
                int healthScore = HealthScoreCalculator.calculateHealthScore(patient1Profile);
                patient1Profile.setHealthScore(healthScore);
                dbHelper.insertOrUpdateProfile(patient1Id, patient1Profile);
                Log.d(TAG, "Patient 1 profile updated");
            }

            User patient2 = dbHelper.checkUser("patient2", "password");
            String patient2Id = "";

            if (patient2 == null) {
                long newPatient2Id = dbHelper.addTestUser("patient2", "password", "patient");
                if (newPatient2Id != -1) {
                    patient2Id = String.valueOf(newPatient2Id);
                    Log.d(TAG, "Patient 2 added successfully with ID: " + patient2Id);

                    Patient patient2Profile = new Patient("Jane Smith", 42, 165, 60, "Asthma, Seasonal allergies");
                    patient2Profile.setCnp("2345678901234");
                    patient2Profile.setBloodPressureSystolic(120);
                    patient2Profile.setBloodPressureDiastolic(80);
                    patient2Profile.setRestingHeartRate(68);
                    patient2Profile.setGender("female");
                    // Calculate health score using the HealthScoreCalculator
                    int healthScore = HealthScoreCalculator.calculateHealthScore(patient2Profile);
                    patient2Profile.setHealthScore(healthScore);
                    dbHelper.insertOrUpdateProfile(patient2Id, patient2Profile);
                    Log.d(TAG, "Patient 2 profile added successfully");
                } else {
                    Log.e(TAG, "Failed to add patient 2");
                }
            } else {
                patient2Id = patient2.getUserId();
                Log.d(TAG, "Patient 2 already exists with ID: " + patient2Id);

                Patient patient2Profile = new Patient("Jane Smith", 42, 165, 60, "Asthma, Seasonal allergies");
                patient2Profile.setCnp("2345678901234");
                patient2Profile.setBloodPressureSystolic(120);
                patient2Profile.setBloodPressureDiastolic(80);
                patient2Profile.setRestingHeartRate(68);
                patient2Profile.setGender("female");
                // Calculate health score using the HealthScoreCalculator
                int healthScore = HealthScoreCalculator.calculateHealthScore(patient2Profile);
                patient2Profile.setHealthScore(healthScore);
                dbHelper.insertOrUpdateProfile(patient2Id, patient2Profile);
                Log.d(TAG, "Patient 2 profile updated");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding patients: " + e.getMessage());
        }
    }

    /**
     * Assigns patients to the specialized doctors
     * @param context The context to use for database access
     * @param cardioId The ID of the cardiologist
     * @param pneumoId The ID of the pneumologist
     * @param nutriId The ID of the nutritionist
     */
    private static void assignPatientsToSpecializedDoctors(Context context, String cardioId, String pneumoId, String nutriId) {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

            User patient1 = dbHelper.checkUser("patient1", "password");
            if (patient1 != null) {
                if (!cardioId.isEmpty()) {
                    dbHelper.assignPatientToDoctor(cardioId, patient1.getUserId());
                    Log.d(TAG, "Patient 1 assigned to cardio doctor");
                }

                if (!pneumoId.isEmpty()) {
                    dbHelper.assignPatientToDoctor(pneumoId, patient1.getUserId());
                    Log.d(TAG, "Patient 1 assigned to pneumo doctor");
                }

                if (!nutriId.isEmpty()) {
                    dbHelper.assignPatientToDoctor(nutriId, patient1.getUserId());
                    Log.d(TAG, "Patient 1 assigned to nutri doctor");
                }
            }

            User patient2 = dbHelper.checkUser("patient2", "password");
            if (patient2 != null && !cardioId.isEmpty()) {
                dbHelper.assignPatientToDoctor(cardioId, patient2.getUserId());
                Log.d(TAG, "Patient 2 assigned to cardio doctor");
            }

            User patient3 = dbHelper.checkUser("patient3", "password");
            if (patient3 != null && !pneumoId.isEmpty()) {
                dbHelper.assignPatientToDoctor(pneumoId, patient3.getUserId());
                Log.d(TAG, "Patient 3 assigned to pneumo doctor");
            }

            User patient4 = dbHelper.checkUser("patient4", "password");
            if (patient4 != null && !nutriId.isEmpty()) {
                dbHelper.assignPatientToDoctor(nutriId, patient4.getUserId());
                Log.d(TAG, "Patient 4 assigned to nutri doctor");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error assigning patients to specialized doctors: " + e.getMessage());
        }
    }
}
