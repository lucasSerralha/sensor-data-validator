package com.smartparking.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.model.AlertEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {

    private final ObjectMapper objectMapper;

    public NotificationDispatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "alert.incident", groupId = "notification-group")
    public void handleAlert(String message) {
        try {
            AlertEvent event = objectMapper.readValue(message, AlertEvent.class);
            
            // Simulate sending Push Notification
            System.out.println(">>> [PUSH NOTIFICATION] To Fiscal App: " + event.getMessage() + " (Spot: " + event.getSpot() + ")");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
