package com.example.myapplication;

public class Patient {
    private String cnp;
    private final String name;
    private final int age;
    private final float height;
    private final float weight;
    private String lastMedicalReport;

    private float bodyFatPercentage;
    private int restingHeartRate;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private float bloodGlucose;
    private float cholesterolTotal;
    private float cholesterolHDL;
    private float cholesterolLDL;
    private String bmiInterpretation; // BMI interpretation text
    private String metabolicInterpretation; // Metabolic Balance interpretation text

    private int averageSteps;
    private int averageSedentaryMinutes;
    private double averageBreathingRate;
    private double averageDailyRmssd;
    private double averageDeepRmssd;
    private int minutesAfterWakeup;
    private int minutesAwake;
    private int minutesToFallAsleep;
    private int restlessCount;
    private int restlessDuration;
    private int timeInBed;
    private int deepSleep;
    private int lightSleep;
    private int remSleep;
    private int wakeSleep;
    private int averageActiveZoneMinutes;
    private String vo2Max;
    private int healthScore;
    private String bodyType; // Ectomorph, Mesomorph, Endomorph, or hybrid types (combination)
    private String gender; // male, female, or "don't want to specify"

    public Patient(String name, int age, float height, float weight, String lastMedicalReport) {
        this.cnp = "";
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.lastMedicalReport = lastMedicalReport;

        this.bodyFatPercentage = 0;
        this.restingHeartRate = 0;
        this.bloodPressureSystolic = 0;
        this.bloodPressureDiastolic = 0;
        this.bloodGlucose = 0;
        this.cholesterolTotal = 0;
        this.cholesterolHDL = 0;
        this.cholesterolLDL = 0;
        this.healthScore = 50; // Set default health score to 50 instead of 0
        this.bodyType = "";
        this.bmiInterpretation = "";
        this.metabolicInterpretation = "";
        this.gender = "don't want to specify"; // Default gender value
    }

    public Patient(String cnp, String name, int age, float height, float weight, String lastMedicalReport) {
        this.cnp = cnp;
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.lastMedicalReport = lastMedicalReport;

        this.bodyFatPercentage = 0;
        this.restingHeartRate = 0;
        this.bloodPressureSystolic = 0;
        this.bloodPressureDiastolic = 0;
        this.bloodGlucose = 0;
        this.cholesterolTotal = 0;
        this.cholesterolHDL = 0;
        this.cholesterolLDL = 0;
        this.healthScore = 50; // Set default health score to 50 instead of 0
        this.bodyType = "";
        this.bmiInterpretation = "";
        this.metabolicInterpretation = "";
        this.gender = "don't want to specify"; // Default gender value
    }


    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public String getLastMedicalReport() {
        return lastMedicalReport;
    }

    public int getAverageSteps() {
        return averageSteps;
    }

    public int getAverageSedentaryMinutes() {
        return averageSedentaryMinutes;
    }

    public double getAverageBreathingRate() {
        return averageBreathingRate;
    }

    public double getAverageDailyRmssd() {
        return averageDailyRmssd;
    }

    public double getAverageDeepRmssd() {
        return averageDeepRmssd;
    }

    public int getMinutesAfterWakeup() {
        return minutesAfterWakeup;
    }

    public int getMinutesAwake() {
        return minutesAwake;
    }

    public int getMinutesToFallAsleep() {
        return minutesToFallAsleep;
    }

    public int getRestlessCount() {
        return restlessCount;
    }

    public int getRestlessDuration() {
        return restlessDuration;
    }

    public int getTimeInBed() {
        return timeInBed;
    }

    public int getDeepSleep() {
        return deepSleep;
    }

    public int getLightSleep() {
        return lightSleep;
    }

    public int getRemSleep() {
        return remSleep;
    }

    public int getWakeSleep() {
        return wakeSleep;
    }

    public int getAverageActiveZoneMinutes() {
        return averageActiveZoneMinutes;
    }

    public String getVo2Max() {
        return vo2Max;
    }

    public void setLastMedicalReport(String lastMedicalReport) {
        this.lastMedicalReport = lastMedicalReport;
    }

    public void updateAverageValues(int averageSteps, int averageSedentaryMinutes, double averageBreathingRate,
                                   double averageDailyRmssd, double averageDeepRmssd, int minutesAfterWakeup,
                                   int minutesAwake, int minutesToFallAsleep, int restlessCount, int restlessDuration,
                                   int timeInBed, int deepSleep, int lightSleep, int remSleep, int wakeSleep,
                                   int averageActiveZoneMinutes, String vo2Max) {
        this.averageSteps = averageSteps;
        this.averageSedentaryMinutes = averageSedentaryMinutes;
        this.averageBreathingRate = averageBreathingRate;
        this.averageDailyRmssd = averageDailyRmssd;
        this.averageDeepRmssd = averageDeepRmssd;
        this.minutesAfterWakeup = minutesAfterWakeup;
        this.minutesAwake = minutesAwake;
        this.minutesToFallAsleep = minutesToFallAsleep;
        this.restlessCount = restlessCount;
        this.restlessDuration = restlessDuration;
        this.timeInBed = timeInBed;
        this.deepSleep = deepSleep;
        this.lightSleep = lightSleep;
        this.remSleep = remSleep;
        this.wakeSleep = wakeSleep;
        this.averageActiveZoneMinutes = averageActiveZoneMinutes;
        this.vo2Max = vo2Max;
    }

    public void setVo2Max(String vo2Max) {
        this.vo2Max = vo2Max;
    }

    public float getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(float bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public int getRestingHeartRate() {
        return restingHeartRate;
    }

    public void setRestingHeartRate(int restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }

    public int getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(int bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public int getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(int bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }

    public float getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(float bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public float getCholesterolTotal() {
        return cholesterolTotal;
    }

    public void setCholesterolTotal(float cholesterolTotal) {
        this.cholesterolTotal = cholesterolTotal;
    }

    public float getCholesterolHDL() {
        return cholesterolHDL;
    }

    public void setCholesterolHDL(float cholesterolHDL) {
        this.cholesterolHDL = cholesterolHDL;
    }

    public float getCholesterolLDL() {
        return cholesterolLDL;
    }

    public void setCholesterolLDL(float cholesterolLDL) {
        this.cholesterolLDL = cholesterolLDL;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getBmiInterpretation() {
        return bmiInterpretation;
    }

    public void setBmiInterpretation(String bmiInterpretation) {
        this.bmiInterpretation = bmiInterpretation;
    }

    public String getMetabolicInterpretation() {
        return metabolicInterpretation;
    }

    public void setMetabolicInterpretation(String metabolicInterpretation) {
        this.metabolicInterpretation = metabolicInterpretation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFitbitDataSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Fitbit Data Summary:\n\n");

        if (averageSteps > 0) {
            summary.append("Average Steps: ").append(averageSteps).append("\n");
        }

        if (averageSedentaryMinutes > 0) {
            int hours = averageSedentaryMinutes / 60;
            int minutes = averageSedentaryMinutes % 60;
            summary.append("Average Sedentary Time: ").append(hours).append("h ").append(minutes).append("m\n");
        }

        if (averageActiveZoneMinutes > 0) {
            summary.append("Average Active Zone Minutes: ").append(averageActiveZoneMinutes).append("\n");
        }

        if (averageBreathingRate > 0) {
            summary.append("Average Breathing Rate: ").append(String.format("%.1f", averageBreathingRate)).append(" breaths/min\n");
        }

        if (averageDailyRmssd > 0) {
            summary.append("Average Daily HRV (RMSSD): ").append(String.format("%.1f", averageDailyRmssd)).append(" ms\n");
        }

        if (averageDeepRmssd > 0) {
            summary.append("Average Deep Sleep HRV (RMSSD): ").append(String.format("%.1f", averageDeepRmssd)).append(" ms\n");
        }

        if (deepSleep > 0 || lightSleep > 0 || remSleep > 0 || wakeSleep > 0) {
            summary.append("\nSleep Data:\n");

            if (deepSleep > 0) {
                int hours = deepSleep / 60;
                int minutes = deepSleep % 60;
                summary.append("Deep Sleep: ").append(hours).append("h ").append(minutes).append("m\n");
            }

            if (lightSleep > 0) {
                int hours = lightSleep / 60;
                int minutes = lightSleep % 60;
                summary.append("Light Sleep: ").append(hours).append("h ").append(minutes).append("m\n");
            }

            if (remSleep > 0) {
                int hours = remSleep / 60;
                int minutes = remSleep % 60;
                summary.append("REM Sleep: ").append(hours).append("h ").append(minutes).append("m\n");
            }

            if (wakeSleep > 0) {
                int hours = wakeSleep / 60;
                int minutes = wakeSleep % 60;
                summary.append("Awake: ").append(hours).append("h ").append(minutes).append("m\n");
            }

            if (timeInBed > 0) {
                int hours = timeInBed / 60;
                int minutes = timeInBed % 60;
                summary.append("Total Time in Bed: ").append(hours).append("h ").append(minutes).append("m\n");
            }
        }

        if (vo2Max != null && !vo2Max.isEmpty()) {
            summary.append("\nVO2 Max: ").append(vo2Max).append("\n");
        }

        return summary.toString();
    }
}
