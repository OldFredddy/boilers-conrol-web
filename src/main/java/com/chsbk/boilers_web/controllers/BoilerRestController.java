/* src/main/java/com/chsbk/boilers_web/controllers/BoilerRestController.java */

package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.entities.Boiler;
import com.chsbk.boilers_web.services.BoilersDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") // Разрешить доступ для всех доменов
public class BoilerRestController {

    @Autowired
    BoilersDataService boilersDataService;

    @GetMapping("/api/boilers")
    public List<Boiler> getBoilers() {
        return boilersDataService.getBoilers();
    }
}
