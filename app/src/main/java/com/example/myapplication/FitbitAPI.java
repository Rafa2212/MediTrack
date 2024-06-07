package com.example.myapplication;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class FitbitAPI {

    private static final String BASE_URL = "https://api.fitbit.com/1/user/-/";

    public interface FitbitService {
        @GET Call<ActivityResponse> getActivities(@Url String url);
        @GET Call<BreathingRateResponse> getBreathingRate(@Url String url);
        @GET Call<SleepResponse> getSleep(@Url String url);
        @GET Call<ActiveZoneMinutesResponse> getActiveZoneMinutes(@Url String url);
        @GET Call<HrvResponse> getHrv(@Url String url);
        @GET Call<CardioScoreResponse> getCardioScore(@Url String url);
    }

    private final FitbitService fitbitService;

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

    public void updateUserProfile(UserProfile userProfile) {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String date = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
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
                        public void onResponse(Call<ActivityResponse> call, Response<ActivityResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().summary != null) {
                                totalSteps.addAndGet(response.body().summary.steps);
                                totalSedentaryMinutes.addAndGet(response.body().summary.sedentaryMinutes);
                                totalHeartRate.addAndGet(response.body().summary.restingHeartRate);
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Call<ActivityResponse> call, Throwable t) {
                            latch.countDown();
                        }
                    });
        }

        String breathingRateUrl = BASE_URL + "br/date/" + startDate + "/" + endDate + ".json";
        fitbitService.getBreathingRate(breathingRateUrl)
                .enqueue(new Callback<BreathingRateResponse>() {
                    @Override
                    public void onResponse(Call<BreathingRateResponse> call, Response<BreathingRateResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().br.length > 0) {
                            for (BreathingRateResponse.BreathingRate br : response.body().br) {
                                totalBreathingRate.updateAndGet(v -> v + br.value.breathingRate);
                                totalBreathingRateDays.getAndIncrement();
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Call<BreathingRateResponse> call, Throwable t) {
                        latch.countDown();
                    }
                });

        String sleepUrl = BASE_URL + "sleep/date/" + startDate + "/" + endDate + ".json";
        fitbitService.getSleep(sleepUrl).enqueue(new Callback<SleepResponse>() {
            @Override
            public void onResponse(Call<SleepResponse> call, Response<SleepResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().sleep.length > 0) {
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
            public void onFailure(Call<SleepResponse> call, Throwable t) {
                latch.countDown();
            }
        });
        for (String date : dates) {
            String activeZoneMinutesUrl = BASE_URL + "activities/active-zone-minutes/date/" + date + "/1d.json";
            fitbitService.getActiveZoneMinutes(activeZoneMinutesUrl).enqueue(new Callback<ActiveZoneMinutesResponse>() {
                @Override
                public void onResponse(Call<ActiveZoneMinutesResponse> call, Response<ActiveZoneMinutesResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().activitiesActiveZoneMinutes != null) {
                        totalActiveZoneMinutes.addAndGet(response.body().activitiesActiveZoneMinutes[0].value.activeZoneMinutes);
                        totalActiveZoneDays.getAndIncrement();
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<ActiveZoneMinutesResponse> call, Throwable t) {
                    latch.countDown();
                }
            });
        }
        for (String date : dates) {
            String hrvUrl = BASE_URL + "hrv/date/" + date + ".json";
            fitbitService.getHrv(hrvUrl).enqueue(new Callback<HrvResponse>() {
                @Override
                public void onResponse(Call<HrvResponse> call, Response<HrvResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().hrv.length > 0) {
                        for (HrvResponse.Hrv hrv : response.body().hrv) {
                            totalDailyRmssd.updateAndGet(v -> v + hrv.value.dailyRmssd);
                            totalDeepRmssd.updateAndGet(v -> v + hrv.value.deepRmssd);
                            totalHrvDays.getAndIncrement();
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<HrvResponse> call, Throwable t) {
                    latch.countDown();
                }
            });
        }
        for (String date : dates) {
            String cardioUrl = BASE_URL + "cardioscore/date/" + date + ".json";
            fitbitService.getCardioScore(cardioUrl).enqueue(new Callback<CardioScoreResponse>() {
                @Override
                public void onResponse(Call<CardioScoreResponse> call, Response<CardioScoreResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().cardioScore.length > 0) {
                        userProfile.setVo2Max(response.body().cardioScore[0].value.vo2Max);
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<CardioScoreResponse> call, Throwable t) {
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

    static class ActivityResponse {
        public Summary summary;
        static class Summary {
            public int steps;
            public int sedentaryMinutes;
            public int restingHeartRate;
        }
    }

    static class BreathingRateResponse {
        public BreathingRate[] br;
        static class BreathingRate {
            public Value value;
            static class Value {
                public double breathingRate;
            }
        }
    }

    static class SleepResponse {
        public Sleep[] sleep;
        static class Sleep {
            public int minutesAfterWakeup;
            public int minutesAwake;
            public int minutesToFallAsleep;
            public int restlessCount;
            public int restlessDuration;
            public int timeInBed;
        }
    }

    static class ActiveZoneMinutesResponse {
        public ActiveZoneMinutes[] activitiesActiveZoneMinutes;
        static class ActiveZoneMinutes {
            public String dateTime;
            public Value value;
            static class Value {
                public int fatBurnActiveZoneMinutes;
                public int activeZoneMinutes;
            }
        }
    }

    static class HrvResponse {
        public Hrv[] hrv;
        static class Hrv {
            public Value value;
            static class Value {
                public double dailyRmssd;
                public double deepRmssd;
            }
        }
    }

    static class CardioScoreResponse {
        public CardioScore[] cardioScore;
        static class CardioScore {
            public String dateTime;
            public Value value;
            static class Value {
                public String vo2Max;
            }
        }
    }
}