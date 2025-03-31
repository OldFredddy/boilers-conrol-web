package com.chsbk.boilers_web.services;

import com.chsbk.boilers_web.entities.Boiler;
import com.chsbk.boilers_web.entities.TemperatureCorrections;
import com.chsbk.boilers_web.utils.JsonMapper;
import com.google.gson.Gson;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class BoilersDataService {
    @Getter
    private List<Boiler> boilers = new ArrayList<>();
    @Getter
    private TemperatureCorrections corrections = new TemperatureCorrections();
    private final int BOILERS_COUNT = 14;
    private final OkHttpClient httpClient;
    private static final String IP_CHSBK_VDS = "http://95.142.45.133:23873";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean isUpdateInProgress = new AtomicBoolean(false);
    private final AtomicBoolean isUpdateInProgress2 = new AtomicBoolean(false);
    private final Gson gson = new Gson();

    public BoilersDataService() {
        // Инициализируем список котлов
        for (int i = 0; i < BOILERS_COUNT; i++) {
            Boiler boiler = new Boiler();
            boiler.setId(i);
            boiler.setIsOk(1, 1);
            boiler.setPPod("0.4");
            boiler.setTAlarm("65");
            boiler.setTPlan("70");
            boiler.setTPod("68");
            boiler.setImageResId(2);
            boiler.setTUlica("0");
            boiler.setPPodHighFixed("-1");
            boiler.setPPodLowFixed("-1");
            boiler.setTPodFixed("-1");
            boiler.setName("no data");
            boilers.add(boiler);
        }

        this.httpClient = new OkHttpClient();

        scheduler.scheduleAtFixedRate(this::fetchBoilerData, 0, 3, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void onDestroy() {
        scheduler.shutdown();
        log.info("BoilersDataService stopped.");
    }

    public String getCorrectionTpodAtIndex(int index) {
        String[] corrections = getCorrections().getCorrectionTpod();
        if (corrections == null || corrections.length <= index || corrections[index] == null) {
            return "0";
        }
        return corrections[index];
    }

    private boolean forFirstStart = true;

    private void refreshBoilersParams(List<Boiler> boilersList) {
        for (int i = 0; i < boilersList.size(); i++) {
            Boiler oldBoiler = this.boilers.get(i);
            Boiler newBoiler = boilersList.get(i);

            boolean isValueChanged = false;

            if (!oldBoiler.getTPod().equals(newBoiler.getTPod())) {
                oldBoiler.setTPod(newBoiler.getTPod());
                isValueChanged = true;
            }
            if (!oldBoiler.getPPod().equals(newBoiler.getPPod())) {
                oldBoiler.setPPod(newBoiler.getPPod());
                isValueChanged = true;
            }
            if (!oldBoiler.getTUlica().equals(newBoiler.getTUlica())) {
                oldBoiler.setTUlica(newBoiler.getTUlica());
                isValueChanged = true;
            }
            oldBoiler.setTPodFixed(newBoiler.getTPodFixed());
            oldBoiler.setPPodHighFixed(newBoiler.getPPodHighFixed());
            oldBoiler.setPPodLowFixed(newBoiler.getPPodLowFixed());
            oldBoiler.setTPlan(newBoiler.getTPlan());
            oldBoiler.setImageResId(newBoiler.getImageResId());
            oldBoiler.setId(newBoiler.getId());
            oldBoiler.setTAlarm(newBoiler.getTAlarm());
            oldBoiler.setLastUpdated(System.currentTimeMillis());
            switch (oldBoiler.getId()) {
                case 0:
                    oldBoiler.setName("Котельная «Склады Мищенко»");
                    break;
                case 1:
                    oldBoiler.setName("Котельная «Выставка Ендальцева»");
                    break;
                case 2:
                    oldBoiler.setName("Котельная «ЧукотОптТорг»");
                    break;
                case 3:
                    oldBoiler.setName("Котельная «ЧСБК новая»");
                    break;
                case 4:
                    oldBoiler.setName("Котельная «Офис СВТ»");
                    break;
                case 5:
                    oldBoiler.setName("Котельная «Общежитие на Южной»");
                    break;
                case 6:
                    oldBoiler.setName("Котельная «Офис ЧСБК»");
                    break;
                case 7:
                    oldBoiler.setName("Котельная «Рынок»");
                    break;
                case 8:
                    oldBoiler.setName("Котельная «Макатровых»");
                    break;
                case 9:
                    oldBoiler.setName("Котельная ДС «Сказка»");
                    break;
                case 10:
                    oldBoiler.setName("Котельная «Полярный»");
                    break;
                case 11:
                    oldBoiler.setName("Котельная «Департамент»");
                    break;
                case 12:
                    oldBoiler.setName("Котельная «Офис ЧСБК квартиры»");
                    break;
                case 13:
                    oldBoiler.setName("Котельная Шишкина");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid boiler ID: " + oldBoiler.getId());
            }

            if (isValueChanged) {
                oldBoiler.setLastValueChangedTime(System.currentTimeMillis());
            }
        }
        if (forFirstStart) {
            for (Boiler boiler : this.boilers) {
                boiler.setIsOk(1, 2);
                boiler.setLastValueChangedTime(System.currentTimeMillis());
            }
            forFirstStart = false;
        }
    }

    private void fetchBoilerData() {
        if (isUpdateInProgress.compareAndSet(false, true)) {
            getBoilersFromClient();
        }

        if (isUpdateInProgress2.compareAndSet(false, true)) {
            getCorrectionsTpodFromClient();
        }
    }

    private void getBoilersFromClient() {
        Request request = new Request.Builder()
                .url(IP_CHSBK_VDS + "/getparams")
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Ошибка при получении данных котлов: {}", e.getMessage());
                isUpdateInProgress.set(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        //log.info(Arrays.toString(jsonResponse.getBytes()));
                        List<Boiler> boilersList = JsonMapper.mapJsonToBoilers(jsonResponse);
                        refreshBoilersParams(boilersList);
                    } else {
                        log.error("Неуспешный ответ при получении данных котлов: {}", response.message());
                    }
                } catch (Exception e) {
                    log.error("Ошибка при обработке данных котлов: {}", e.getMessage());
                } finally {
                    isUpdateInProgress.set(false);
                }
            }
        });
    }

    private void getCorrectionsTpodFromClient() {
        Request request = new Request.Builder()
                .url(IP_CHSBK_VDS + "/getcorrect")
                .get()
                .build();

    }

    public void setCorrectionsTpod(String[] corrections1) {
        this.corrections.setCorrectionTpod(corrections1);
        setCorrectionsTpodToClient(corrections1);
    }

    private void setCorrectionsTpodToClient(String[] correctionsTpod) {
        String json = gson.toJson(correctionsTpod);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(IP_CHSBK_VDS + "/setclientparamstPod")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Ошибка при отправке корректировок tPod: {}", e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    log.info("Корректировки tPod успешно отправлены.");
                } else {
                    log.error("Неуспешный ответ при отправке корректировок tPod: {}", response.message());
                }
            }
        });
    }

    public void setCorrectionsTAlarm(String[] corrections1) {
        this.corrections.setTAlarmCorrectionFromUsers(corrections1);
        setTAlarmToClient(corrections1);
    }

    private void setTAlarmToClient(String[] correctionsTAlarm) {
        String json = gson.toJson(correctionsTAlarm);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(IP_CHSBK_VDS + "/setclientparamstAlarm")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Ошибка при отправке корректировок tAlarm: {}", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    log.info("Корректировки tAlarm успешно отправлены.");
                } else {
                    log.error("Неуспешный ответ при отправке корректировок tAlarm: {}", response.message());
                }
            }
        });
    }
}
