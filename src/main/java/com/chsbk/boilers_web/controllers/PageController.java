package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.configs.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    private String loginPath = SecurityConfig.getLoginPath();



    @RequestMapping(value = "/{path}")
    public String login(@PathVariable String path, HttpServletRequest request) {
        if (request.getRequestURI().equals(loginPath)) {
            return "auth_page";
        } else {
            return "redirect:/login";
        }
    }





}
