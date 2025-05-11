package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @GetMapping("/events")
    public String events(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "50") int size,
                         Model model) {

        Page<?> eventsPage = service.getPage(page, size);

        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("size", size);

        return "events";
    }
}
