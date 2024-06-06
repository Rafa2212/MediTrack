package com.example.myapplication;

public class UserProfile {
    private final String name;
    private final int age;
    private final float height;
    private final float weight;
    private String lastMedicalReport;

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

    public UserProfile(String name, int age, float height, float weight, String lastMedicalReport) {
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.lastMedicalReport = lastMedicalReport;
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
}