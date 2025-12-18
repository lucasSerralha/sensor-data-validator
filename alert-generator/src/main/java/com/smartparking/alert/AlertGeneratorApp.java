package com.smartparking.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("com.smartparking.backend.model") // Scan the entity package (we will copy the entity or share it)
public class AlertGeneratorApp {
    public static void main(String[] args) {
        SpringApplication.run(AlertGeneratorApp.class, args);
    }
}
