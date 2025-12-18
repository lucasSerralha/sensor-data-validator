package com.smartparking.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("com.smartparking.alert") // Scan the entity package
public class AlertGeneratorApp {
    public static void main(String[] args) {
        SpringApplication.run(AlertGeneratorApp.class, args);
    }
}
