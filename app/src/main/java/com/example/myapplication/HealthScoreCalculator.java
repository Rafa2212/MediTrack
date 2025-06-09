package com.example.myapplication;

/**
 * Utility class for calculating a patient's health score based on various health metrics.
 * The health score is a value between 0 and 100, where higher scores indicate better health.
 * The score is calculated based on a combination of factors including:
 * - Body fat percentage (relative to gender and age)
 * - Cholesterol levels (total, HDL, LDL)
 * - Blood pressure (systolic and diastolic)
 * - Blood glucose levels
 * - Resting heart rate
 * - BMI (calculated from height and weight)
 */
public class HealthScoreCalculator {

    /**
     * Calculates a health score for a patient based on their health metrics.
     * 
     * @param patient The patient whose health score to calculate
     * @return An integer health score between 0 and 100
     */
    public static int calculateHealthScore(Patient patient) {
        if (patient == null) {
            return 0;
        }

        // Initialize base score
        int score = 70; // Start with a baseline score of 70

        // Add or subtract points based on various health metrics
        score += calculateBmiScore(patient);
        score += calculateBodyFatScore(patient);
        score += calculateBloodPressureScore(patient);
        score += calculateCholesterolScore(patient);
        score += calculateBloodGlucoseScore(patient);
        score += calculateRestingHeartRateScore(patient);

        // Ensure score stays within 0-100 range
        score = Math.max(0, Math.min(100, score));
        
        return score;
    }

    /**
     * Calculates a score component based on BMI.
     */
    private static int calculateBmiScore(Patient patient) {
        float height = patient.getHeight() / 100; // Convert cm to meters
        float weight = patient.getWeight();
        
        if (height <= 0 || weight <= 0) {
            return 0; // Can't calculate BMI with invalid height/weight
        }
        
        float bmi = weight / (height * height);
        
        // BMI scoring
        if (bmi < 18.5) {
            return -5; // Underweight
        } else if (bmi >= 18.5 && bmi < 25) {
            return 5; // Normal weight
        } else if (bmi >= 25 && bmi < 30) {
            return -2; // Overweight
        } else if (bmi >= 30 && bmi < 35) {
            return -5; // Obese Class I
        } else if (bmi >= 35 && bmi < 40) {
            return -10; // Obese Class II
        } else {
            return -15; // Obese Class III
        }
    }

    /**
     * Calculates a score component based on body fat percentage, taking gender into account.
     */
    private static int calculateBodyFatScore(Patient patient) {
        float bodyFatPercentage = patient.getBodyFatPercentage();
        String gender = patient.getGender();
        int age = patient.getAge();
        
        if (bodyFatPercentage <= 0 || gender == null || gender.isEmpty()) {
            return 0; // Can't calculate without body fat or gender
        }
        
        // Different body fat percentage ranges for males and females
        if ("male".equalsIgnoreCase(gender)) {
            if (age < 40) {
                if (bodyFatPercentage < 8) return 0; // Too low
                else if (bodyFatPercentage >= 8 && bodyFatPercentage < 20) return 5; // Ideal
                else if (bodyFatPercentage >= 20 && bodyFatPercentage < 25) return 0; // Average
                else if (bodyFatPercentage >= 25) return -5; // High
            } else {
                if (bodyFatPercentage < 11) return 0; // Too low
                else if (bodyFatPercentage >= 11 && bodyFatPercentage < 22) return 5; // Ideal
                else if (bodyFatPercentage >= 22 && bodyFatPercentage < 28) return 0; // Average
                else if (bodyFatPercentage >= 28) return -5; // High
            }
        } else { // Female
            if (age < 40) {
                if (bodyFatPercentage < 21) return 0; // Too low
                else if (bodyFatPercentage >= 21 && bodyFatPercentage < 33) return 5; // Ideal
                else if (bodyFatPercentage >= 33 && bodyFatPercentage < 39) return 0; // Average
                else if (bodyFatPercentage >= 39) return -5; // High
            } else {
                if (bodyFatPercentage < 23) return 0; // Too low
                else if (bodyFatPercentage >= 23 && bodyFatPercentage < 35) return 5; // Ideal
                else if (bodyFatPercentage >= 35 && bodyFatPercentage < 42) return 0; // Average
                else if (bodyFatPercentage >= 42) return -5; // High
            }
        }
        
        return 0;
    }

    /**
     * Calculates a score component based on blood pressure.
     */
    private static int calculateBloodPressureScore(Patient patient) {
        int systolic = patient.getBloodPressureSystolic();
        int diastolic = patient.getBloodPressureDiastolic();
        
        if (systolic <= 0 || diastolic <= 0) {
            return 0; // Can't calculate without blood pressure
        }
        
        // Blood pressure scoring
        if (systolic < 120 && diastolic < 80) {
            return 5; // Normal
        } else if ((systolic >= 120 && systolic < 130) && diastolic < 80) {
            return 2; // Elevated
        } else if ((systolic >= 130 && systolic < 140) || (diastolic >= 80 && diastolic < 90)) {
            return -2; // Hypertension Stage 1
        } else if (systolic >= 140 || diastolic >= 90) {
            return -5; // Hypertension Stage 2
        } else if (systolic > 180 || diastolic > 120) {
            return -10; // Hypertensive Crisis
        }
        
        return 0;
    }

    /**
     * Calculates a score component based on cholesterol levels.
     */
    private static int calculateCholesterolScore(Patient patient) {
        float totalCholesterol = patient.getCholesterolTotal();
        float hdlCholesterol = patient.getCholesterolHDL();
        float ldlCholesterol = patient.getCholesterolLDL();
        
        int score = 0;
        
        // Total Cholesterol scoring
        if (totalCholesterol > 0) {
            if (totalCholesterol < 200) {
                score += 5; // Desirable
            } else if (totalCholesterol >= 200 && totalCholesterol < 240) {
                score += 0; // Borderline high
            } else if (totalCholesterol >= 240) {
                score -= 5; // High
            }
        }
        
        // HDL (Good) Cholesterol scoring
        if (hdlCholesterol > 0) {
            if (hdlCholesterol >= 60) {
                score += 5; // Optimal
            } else if (hdlCholesterol >= 40 && hdlCholesterol < 60) {
                score += 2; // Good
            } else if (hdlCholesterol < 40) {
                score -= 5; // Low
            }
        }
        
        // LDL (Bad) Cholesterol scoring
        if (ldlCholesterol > 0) {
            if (ldlCholesterol < 100) {
                score += 5; // Optimal
            } else if (ldlCholesterol >= 100 && ldlCholesterol < 130) {
                score += 2; // Near optimal
            } else if (ldlCholesterol >= 130 && ldlCholesterol < 160) {
                score += 0; // Borderline high
            } else if (ldlCholesterol >= 160 && ldlCholesterol < 190) {
                score -= 2; // High
            } else if (ldlCholesterol >= 190) {
                score -= 5; // Very high
            }
        }
        
        return score;
    }

    /**
     * Calculates a score component based on blood glucose levels.
     */
    private static int calculateBloodGlucoseScore(Patient patient) {
        float bloodGlucose = patient.getBloodGlucose();
        
        if (bloodGlucose <= 0) {
            return 0; // Can't calculate without blood glucose
        }
        
        // Blood glucose scoring (fasting)
        if (bloodGlucose < 100) {
            return 5; // Normal
        } else if (bloodGlucose >= 100 && bloodGlucose < 126) {
            return 0; // Prediabetes
        } else if (bloodGlucose >= 126) {
            return -5; // Diabetes
        }
        
        return 0;
    }

    /**
     * Calculates a score component based on resting heart rate.
     */
    private static int calculateRestingHeartRateScore(Patient patient) {
        int restingHeartRate = patient.getRestingHeartRate();
        
        if (restingHeartRate <= 0) {
            return 0; // Can't calculate without resting heart rate
        }
        
        // Resting heart rate scoring
        if (restingHeartRate < 60) {
            return 5; // Excellent
        } else if (restingHeartRate >= 60 && restingHeartRate < 70) {
            return 3; // Very good
        } else if (restingHeartRate >= 70 && restingHeartRate < 80) {
            return 1; // Good
        } else if (restingHeartRate >= 80 && restingHeartRate < 90) {
            return 0; // Average
        } else if (restingHeartRate >= 90) {
            return -3; // Poor
        }
        
        return 0;
    }
}