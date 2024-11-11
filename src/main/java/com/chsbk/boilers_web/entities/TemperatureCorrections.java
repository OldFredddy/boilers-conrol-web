package com.chsbk.boilers_web.entities;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class TemperatureCorrections {
    private String[] tAlarmCorrectionFromUsers;
    private String[] correctionTpod;
}
