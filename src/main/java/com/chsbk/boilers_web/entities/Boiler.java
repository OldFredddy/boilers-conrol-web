package com.chsbk.boilers_web.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Boiler {
    @JsonProperty("isOk")
    private int isOk;  // 0-waiting, 1 - good, 2 - error

    @JsonProperty("tPod")
    private String tPod;

    @JsonProperty("pPod")
    private String pPod;

    @JsonProperty("tUlica")
    private String tUlica;

    @JsonProperty("tPlan")
    private String tPlan;

    @JsonProperty("tAlarm")
    private String tAlarm;

    @JsonProperty("imageResId")
    private int imageResId;

    @JsonProperty("pPodLowFixed")
    private String pPodLowFixed;

    @JsonProperty("pPodHighFixed")
    private String pPodHighFixed;

    @JsonProperty("tPodFixed")
    private String tPodFixed;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("version")
    private long version;

    @JsonProperty("lastUpdated")
    private long lastUpdated;

    @JsonProperty("lastValueChangedTime")
    private long lastValueChangedTime;

    @JsonProperty("boilerIcon")
    private String boilerIcon;

    @JsonProperty("boilerIcon")
    private String name;
    public void setIsOk(int isOk) {
        this.isOk = isOk;
    }

    public void setIsOk(int isOk, long newVersion) {   // 0-waiting, 1 - good, 2 - error
        if (newVersion > this.version) {
            this.isOk = isOk;
            this.version = newVersion;
        }
    }
}
