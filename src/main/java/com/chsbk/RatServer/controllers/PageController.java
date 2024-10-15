package com.chsbk.RatServer.controllers;

import com.chsbk.RatServer.configs.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
