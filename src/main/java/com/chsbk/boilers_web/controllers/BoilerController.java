// src/main/java/com/example/demo/controller/BoilerController.java
package com.chsbk.boilers_web.controllers;


import com.chsbk.boilers_web.entities.Boiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class BoilerController {

    private static final String IP = "95.142.45.133"; // Замените на нужный IP
    private static final String PORT = "23873"; // Замените на нужный порт
    private static final String BASE_URL = "http://" + IP + ":" + PORT;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/")
    public String index(Model model) {
        String url = BASE_URL + "/getparams";
        try {
            ResponseEntity<Boiler[]> response = restTemplate.getForEntity(url, Boiler[].class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Boiler> boilers = Arrays.asList(response.getBody());
                model.addAttribute("boilers", boilers);
            } else {
                // Обработка случая, когда данные не получены
                model.addAttribute("boilers", createTestListOfBoilers());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // При ошибке получения данных используем тестовые данные
            model.addAttribute("boilers", createTestListOfBoilers());
        }
        return "index";
    }

    // Метод для создания тестовых данных (аналогично вашему мобильному приложению)
    private List<Boiler> createTestListOfBoilers() {
        List<Boiler> boilers = new ArrayList<>();
        String[] boilerNames = {
                "Склады Мищенко",
                "Выставка Ендальцева",
                "ЧукотОптТорг",
                "ЧСБК база",
                "Офис СВТ",
                "Общежитие на Южной",
                "Офис ЧСБК",
                "Рынок",
                "Макатровых",
                "ДС «Сказка»",
                "Полярный",
                "Департамент",
                "Квартиры в офисе",
                "ТО Шишкина"
        };
        for (int i = 0; i < boilerNames.length; i++) {
            Boiler boiler = new Boiler();
            boiler.setId((i + 1));
            boiler.setBoilerIcon("/boilers_images/boiler_icon_" + (i + 1) + ".png");
            boiler.setTPod("***");
            boiler.setPPod("***");
            boiler.setTUlica(String.valueOf(i - 5));
            boiler.setTPlan("***");
            boiler.setTAlarm("Нет связи!");
            boilers.add(boiler);
        }
        return boilers;
    }
    @PostMapping("/boiler/updateTPlan")
    public String updateTPlan(@RequestParam Long id, @RequestParam int adjustment) {
        String[] correctTplanS = new String[14]; // Предполагаем 14 котельных
        Arrays.fill(correctTplanS, "0");
        int index = id.intValue() - 1; // Индекс котельной в массиве
        correctTplanS[index] = String.valueOf(adjustment);

        String url = BASE_URL + "/settemperaturecorrections";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String[]> requestEntity = new HttpEntity<>(correctTplanS, headers);

        try {
            restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            // Обработка ошибки отправки данных
        }

        return "redirect:/";
    }

    @PostMapping("/boiler/updateTAlarm")
    public String updateTAlarm(@RequestParam Long id, @RequestParam int adjustment) {
        String[] correctTAlarmS = new String[14];
        Arrays.fill(correctTAlarmS, "0");
        int index = id.intValue() - 1;
        correctTAlarmS[index] = String.valueOf(adjustment);

        String url = BASE_URL + "/setAlarmCorrections";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String[]> requestEntity = new HttpEntity<>(correctTAlarmS, headers);

        try {
            restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            // Обработка ошибки отправки данных
        }

        return "redirect:/";
    }

}
