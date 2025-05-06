package com.chsbk.boilers_web.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EventDTO {
    private String id;
    private String sessionId;
    private String evtType;
    private String action;
    private Object payload;
    private int status;
    private LocalDateTime ts;
}
