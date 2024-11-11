package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.entities.Boiler;
import com.chsbk.boilers_web.services.BoilersDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {
    @Autowired
    BoilersDataService boilersDataService;
    @GetMapping("/linux-devices")
    public String linuxDevices() {
        return "linux_devices_nav";
    }
    @GetMapping("/windows-devices")
    public String windowsDevices() {
        return "win_devices_nav";
    }
    @GetMapping("/devices_nav")
    public String devices_nav(@RequestParam(required = false) String type, Model model) {
        List <Boiler> boilers= boilersDataService.getBoilers();
        model.addAttribute("boilers", boilers);
        return "devices_nav";
    }


}
