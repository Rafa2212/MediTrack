package com.example.myapplication;

public class UserProfile {
    private String cnp;
    private final String name;
    private final int age;
    private final float height;
    private final float weight;
    private String lastMedicalReport;
    private String specialty; // Doctor specialty (e.g., cardiologist, dermatologist)

    private float bodyFatPercentage;
    private int restingHeartRate;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private float bloodGlucose;
    private float cholesterolTotal;
    private float cholesterolHDL;
    private float cholesterolLDL;

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

    public UserProfile(String name, int age, float height, float weight, String lastMedicalReport) {
        this.cnp = "";
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.lastMedicalReport = lastMedicalReport;
        this.specialty = "";

        this.bodyFatPercentage = 0;
        this.restingHeartRate = 0;
        this.bloodPressureSystolic = 0;
        this.bloodPressureDiastolic = 0;
        this.bloodGlucose = 0;
        this.cholesterolTotal = 0;
        this.cholesterolHDL = 0;
        this.cholesterolLDL = 0;
        this.healthScore = 0;
        this.bodyType = "";
    }

    public UserProfile(String cnp, String name, int age, float height, float weight, String lastMedicalReport) {
        this.cnp = cnp;
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.lastMedicalReport = lastMedicalReport;
        this.specialty = "";

        this.bodyFatPercentage = 0;
        this.restingHeartRate = 0;
        this.bloodPressureSystolic = 0;
        this.bloodPressureDiastolic = 0;
        this.bloodGlucose = 0;
        this.cholesterolTotal = 0;
        this.cholesterolHDL = 0;
        this.cholesterolLDL = 0;
        this.healthScore = 0;
        this.bodyType = "";
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

    /**
     * Gets the doctor's specialty
     * @return The doctor's specialty
     */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * Sets the doctor's specialty
     * @param specialty The doctor's specialty
     */
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

}
