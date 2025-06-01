package com.example.myapplication;

import android.annotation.SuppressLint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Client for interacting with the Fitbit API to retrieve health and fitness data.
 * This class provides methods to fetch various health metrics from Fitbit and update
 * a user's profile with the aggregated data. It handles authentication, API requests,
 * and data processing.
 * 
 * The class uses Retrofit for API communication and processes data asynchronously
 * using callbacks and a CountDownLatch to coordinate multiple concurrent requests.
 */
public class FitbitAPI {

    private static final String BASE_URL = "https://api.fitbit.com/1/user/-/";

    /**
     * Interface defining the Fitbit API endpoints used by this client.
     * Each method corresponds to a specific Fitbit API endpoint and returns
     * a Call object that can be executed asynchronously.
     */
    public interface FitbitService {
        /**
         * Gets daily activity data for a specific date.
         * @param url The full URL for the API endpoint including the date
         * @return A Call object containing the activity response
         */
        @GET Call<ActivityResponse> getActivities(@Url String url);

        /**
         * Gets breathing rate data for a date range.
         * @param url The full URL for the API endpoint including the date range
         * @return A Call object containing the breathing rate response
         */
        @GET Call<BreathingRateResponse> getBreathingRate(@Url String url);

        /**
         * Gets sleep data for a date range.
         * @param url The full URL for the API endpoint including the date range
         * @return A Call object containing the sleep response
         */
        @GET Call<SleepResponse> getSleep(@Url String url);

        /**
         * Gets active zone minutes data for a specific date.
         * @param url The full URL for the API endpoint including the date
         * @return A Call object containing the active zone minutes response
         */
        @GET Call<ActiveZoneMinutesResponse> getActiveZoneMinutes(@Url String url);

        /**
         * Gets heart rate variability (HRV) data for a specific date.
         * @param url The full URL for the API endpoint including the date
         * @return A Call object containing the HRV response
         */
        @GET Call<HrvResponse> getHrv(@Url String url);

        /**
         * Gets cardio score data for a specific date.
         * @param url The full URL for the API endpoint including the date
         * @return A Call object containing the cardio score response
         */
        @GET Call<CardioScoreResponse> getCardioScore(@Url String url);
    }

    private final FitbitService fitbitService;

    /**
     * Constructs a new FitbitAPI client with the provided authentication token.
     * This constructor sets up the Retrofit client with proper authentication headers
     * and logging interceptors for API requests.
     *
     * @param token The OAuth2 access token for authenticating with the Fitbit API
     */
    public FitbitAPI(String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder().header("Authorization", "Bearer " + token);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        OkHttpClient client = httpClient.build();
        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        fitbitService = retrofit.create(FitbitService.class);
    }

    /**
     * Updates a user's profile with health metrics retrieved from the Fitbit API.
     * This method fetches data for the last 7 days and calculates average values for various
     * health metrics including steps, sedentary minutes, breathing rate, heart rate variability,
     * sleep metrics, and active zone minutes.
     * 
     * The method makes multiple asynchronous API calls and uses a CountDownLatch to coordinate
     * the responses. Once all data is collected, it calculates averages and updates the user profile.
     * 
     * @param userProfile The UserProfile object to update with the retrieved health metrics
     */
    public void updateUserProfile(UserProfile userProfile) {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            @SuppressLint("DefaultLocale") String date = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            dates.add(date);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        String startDate = dates.get(6);
        String endDate = dates.get(0);

        AtomicInteger totalSteps = new AtomicInteger();
        AtomicInteger totalSedentaryMinutes = new AtomicInteger();
        AtomicInteger totalHeartRate = new AtomicInteger();
        AtomicReference<Double> totalBreathingRate = new AtomicReference<>((double) 0);
        AtomicInteger totalBreathingRateDays = new AtomicInteger();
        AtomicReference<Double> totalDailyRmssd = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalDeepRmssd = new AtomicReference<>((double) 0);
        AtomicInteger totalHrvDays = new AtomicInteger();
        AtomicInteger totalMinutesAfterWakeup = new AtomicInteger();
        AtomicInteger totalMinutesAwake = new AtomicInteger();
        AtomicInteger totalMinutesToFallAsleep = new AtomicInteger();
        AtomicInteger totalRestlessCount = new AtomicInteger();
        AtomicInteger totalRestlessDuration = new AtomicInteger();
        AtomicInteger totalTimeInBed = new AtomicInteger();
        AtomicInteger totalDeepSleep = new AtomicInteger();
        AtomicInteger totalLightSleep = new AtomicInteger();
        AtomicInteger totalRemSleep = new AtomicInteger();
        AtomicInteger totalWakeSleep = new AtomicInteger();
        AtomicInteger totalActiveZoneMinutes = new AtomicInteger();
        AtomicInteger totalSleepDays = new AtomicInteger();
        AtomicInteger totalActiveZoneDays = new AtomicInteger();

        CountDownLatch latch = new CountDownLatch(dates.size() + 5);

        for (String date : dates) {
            String activityUrl = BASE_URL + "activities/date/" + date + ".json";
            fitbitService.getActivities(activityUrl)
                    .enqueue(new Callback<ActivityResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<ActivityResponse> call, @NotNull Response<ActivityResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().summary != null) {
                                totalSteps.addAndGet(response.body().summary.steps);
                                totalSedentaryMinutes.addAndGet(response.body().summary.sedentaryMinutes);
                                totalHeartRate.addAndGet(response.body().summary.restingHeartRate);
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(@NotNull Call<ActivityResponse> call, @NotNull Throwable t) {
                            latch.countDown();
                        }
                    });
        }

        String breathingRateUrl = BASE_URL + "br/date/" + startDate + "/" + endDate + ".json";
        fitbitService.getBreathingRate(breathingRateUrl)
                .enqueue(new Callback<BreathingRateResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<BreathingRateResponse> call, @NotNull Response<BreathingRateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (BreathingRateResponse.BreathingRate br : response.body().br) {
                                totalBreathingRate.updateAndGet(v -> v + br.value.breathingRate);
                                totalBreathingRateDays.getAndIncrement();
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@NotNull Call<BreathingRateResponse> call, @NotNull Throwable t) {
                        latch.countDown();
                    }
                });

        String sleepUrl = BASE_URL + "sleep/date/" + startDate + "/" + endDate + ".json";
        fitbitService.getSleep(sleepUrl).enqueue(new Callback<SleepResponse>() {
            @Override
            public void onResponse(@NotNull Call<SleepResponse> call, @NotNull Response<SleepResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (SleepResponse.Sleep sleep : response.body().sleep) {
                        totalMinutesAfterWakeup.addAndGet(sleep.minutesAfterWakeup);
                        totalMinutesAwake.addAndGet(sleep.minutesAwake);
                        totalMinutesToFallAsleep.addAndGet(sleep.minutesToFallAsleep);
                        totalRestlessCount.addAndGet(sleep.restlessCount);
                        totalRestlessDuration.addAndGet(sleep.restlessDuration);
                        totalTimeInBed.addAndGet(sleep.timeInBed);
                        totalSleepDays.getAndIncrement();
                    }
                }
                latch.countDown();
            }

            @Override
            public void onFailure(@NotNull Call<SleepResponse> call, @NotNull Throwable t) {
                latch.countDown();
            }
        });
        for (String date : dates) {
            String activeZoneMinutesUrl = BASE_URL + "activities/active-zone-minutes/date/" + date + "/1d.json";
            fitbitService.getActiveZoneMinutes(activeZoneMinutesUrl).enqueue(new Callback<ActiveZoneMinutesResponse>() {
                @Override
                public void onResponse(@NotNull Call<ActiveZoneMinutesResponse> call, @NotNull Response<ActiveZoneMinutesResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().activitiesActiveZoneMinutes != null) {
                        totalActiveZoneMinutes.addAndGet(response.body().activitiesActiveZoneMinutes[0].value.activeZoneMinutes);
                        totalActiveZoneDays.getAndIncrement();
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull Call<ActiveZoneMinutesResponse> call, @NotNull Throwable t) {
                    latch.countDown();
                }
            });
        }
        for (String date : dates) {
            String hrvUrl = BASE_URL + "hrv/date/" + date + ".json";
            fitbitService.getHrv(hrvUrl).enqueue(new Callback<HrvResponse>() {
                @Override
                public void onResponse(@NotNull Call<HrvResponse> call, @NotNull Response<HrvResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (HrvResponse.Hrv hrv : response.body().hrv) {
                            totalDailyRmssd.updateAndGet(v -> v + hrv.value.dailyRmssd);
                            totalDeepRmssd.updateAndGet(v -> v + hrv.value.deepRmssd);
                            totalHrvDays.getAndIncrement();
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull Call<HrvResponse> call, @NotNull Throwable t) {
                    latch.countDown();
                }
            });
        }
        for (String date : dates) {
            String cardioUrl = BASE_URL + "cardioscore/date/" + date + ".json";
            fitbitService.getCardioScore(cardioUrl).enqueue(new Callback<CardioScoreResponse>() {
                @Override
                public void onResponse(@NotNull Call<CardioScoreResponse> call, @NotNull Response<CardioScoreResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().cardioScore.length > 0) {
                        userProfile.setVo2Max(response.body().cardioScore[0].value.vo2Max);
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull Call<CardioScoreResponse> call, @NotNull Throwable t) {
                    latch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                latch.await();
                int averageSteps = totalSteps.get() / dates.size();
                int averageSedentaryMinutes = totalSedentaryMinutes.get() / dates.size();
                double averageBreathingRate = totalBreathingRate.get() / (totalBreathingRateDays.get() > 0 ? totalBreathingRateDays.get() : 1);
                double averageDailyRmssd = totalDailyRmssd.get() / (totalHrvDays.get() > 0 ? totalHrvDays.get() : 1);
                double averageDeepRmssd = totalDeepRmssd.get() / (totalHrvDays.get() > 0 ? totalHrvDays.get() : 1);
                int averageMinutesAfterWakeup = totalMinutesAfterWakeup.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageMinutesAwake = totalMinutesAwake.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageMinutesToFallAsleep = totalMinutesToFallAsleep.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageRestlessCount = totalRestlessCount.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageRestlessDuration = totalRestlessDuration.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageTimeInBed = totalTimeInBed.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageDeepSleep = totalDeepSleep.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageLightSleep = totalLightSleep.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageRemSleep = totalRemSleep.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageWakeSleep = totalWakeSleep.get() / (totalSleepDays.get() > 0 ? totalSleepDays.get() : 1);
                int averageActiveZoneMinutes = totalActiveZoneMinutes.get() / (totalActiveZoneDays.get() > 0 ? totalActiveZoneDays.get() : 1);

                userProfile.updateAverageValues(averageSteps, averageSedentaryMinutes, averageBreathingRate,
                        averageDailyRmssd, averageDeepRmssd, averageMinutesAfterWakeup, averageMinutesAwake,
                        averageMinutesToFallAsleep, averageRestlessCount, averageRestlessDuration, averageTimeInBed,
                        averageDeepSleep, averageLightSleep, averageRemSleep, averageWakeSleep, averageActiveZoneMinutes,
                        userProfile.getVo2Max());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Response class for activity data from the Fitbit API.
     * Contains summary information about daily activities.
     */
    static class ActivityResponse {
        /** Summary of daily activity metrics */
        public Summary summary;

        /**
         * Inner class containing summary activity metrics.
         */
        static class Summary {
            /** Number of steps taken */
            public int steps;
            /** Minutes spent in sedentary activity */
            public int sedentaryMinutes;
            /** Resting heart rate in beats per minute */
            public int restingHeartRate;
        }
    }

    /**
     * Response class for breathing rate data from the Fitbit API.
     * Contains an array of breathing rate measurements.
     */
    static class BreathingRateResponse {
        /** Array of breathing rate measurements */
        public BreathingRate[] br;

        /**
         * Inner class representing a single breathing rate measurement.
         */
        static class BreathingRate {
            /** Value container for the breathing rate */
            public Value value;

            /**
             * Inner class containing the actual breathing rate value.
             */
            static class Value {
                /** Breathing rate in breaths per minute */
                public double breathingRate;
            }
        }
    }

    /**
     * Response class for sleep data from the Fitbit API.
     * Contains an array of sleep records.
     */
    static class SleepResponse {
        /** Array of sleep records */
        public Sleep[] sleep;

        /**
         * Inner class representing a single sleep record with various metrics.
         */
        static class Sleep {
            /** Minutes spent awake after waking up */
            public int minutesAfterWakeup;
            /** Minutes spent awake during sleep */
            public int minutesAwake;
            /** Minutes taken to fall asleep */
            public int minutesToFallAsleep;
            /** Count of restless periods during sleep */
            public int restlessCount;
            /** Duration of restless periods in minutes */
            public int restlessDuration;
            /** Total time spent in bed in minutes */
            public int timeInBed;
        }
    }

    /**
     * Response class for active zone minutes data from the Fitbit API.
     * Contains an array of active zone minutes records.
     */
    static class ActiveZoneMinutesResponse {
        /** Array of active zone minutes records */
        public ActiveZoneMinutes[] activitiesActiveZoneMinutes;

        /**
         * Inner class representing a single active zone minutes record.
         */
        static class ActiveZoneMinutes {
            /** Date and time of the record */
            public String dateTime;
            /** Value container for active zone minutes */
            public Value value;

            /**
             * Inner class containing active zone minutes metrics.
             */
            static class Value {
                /** Minutes spent in fat burn zone */
                public int fatBurnActiveZoneMinutes;
                /** Total active zone minutes */
                public int activeZoneMinutes;
            }
        }
    }

    /**
     * Response class for heart rate variability (HRV) data from the Fitbit API.
     * Contains an array of HRV measurements.
     */
    static class HrvResponse {
        /** Array of HRV measurements */
        public Hrv[] hrv;

        /**
         * Inner class representing a single HRV measurement.
         */
        static class Hrv {
            /** Value container for HRV metrics */
            public Value value;

            /**
             * Inner class containing HRV metrics.
             */
            static class Value {
                /** Daily RMSSD (Root Mean Square of Successive Differences) in milliseconds */
                public double dailyRmssd;
                /** Deep sleep RMSSD in milliseconds */
                public double deepRmssd;
            }
        }
    }

    /**
     * Response class for cardio score data from the Fitbit API.
     * Contains an array of cardio score measurements.
     */
    static class CardioScoreResponse {
        /** Array of cardio score measurements */
        public CardioScore[] cardioScore;

        /**
         * Inner class representing a single cardio score measurement.
         */
        static class CardioScore {
            /** Date and time of the measurement */
            public String dateTime;
            /** Value container for cardio score metrics */
            public Value value;

            /**
             * Inner class containing cardio score metrics.
             */
            static class Value {
                /** VO2 Max value (maximum oxygen consumption during exercise) */
                public String vo2Max;
            }
        }
    }
}
