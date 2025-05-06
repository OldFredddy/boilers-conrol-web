package com.chsbk.boilers_web.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    private final ObjectMapper om = new ObjectMapper();

    public EventDTO toDto(Event e) {
        Object payloadObj;
        try {                     // payload хранится как jsonb‑строка
            payloadObj = om.readValue(e.getPayload(), Object.class);
        } catch (Exception ex) {  // fallback – отдаём строкой
            payloadObj = e.getPayload();
        }
        return new EventDTO(
                e.getId(),
                e.getSession() != null ? String.valueOf(e.getSession().getId()) : null,
                e.getEvtType(),
                e.getAction(),
                payloadObj,
                e.getStatus(),
                e.getTs()
        );
    }
}