package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.entities.Boiler;
import com.chsbk.boilers_web.services.BoilersDataService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
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
    public String devices_nav(Model model, HttpServletResponse resp) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            resp.sendRedirect("/login");
            return null;
        }
        model.addAttribute("boilers", boilersDataService.getBoilers());
        return "devices_nav";
    }



}
