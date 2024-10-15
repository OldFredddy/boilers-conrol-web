package com.chsbk.RatServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RatServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(RatServerApplication.class, args);
	}
}
