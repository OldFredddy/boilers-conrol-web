package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.configs.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth_page";
    }

}

