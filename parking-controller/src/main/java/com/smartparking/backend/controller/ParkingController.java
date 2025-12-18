package com.smartparking.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.backend.model.ParkingSession;
import com.smartparking.backend.repository.ParkingSessionRepository;
import com.smartparking.model.AlertEvent;
import com.smartparking.model.PaymentEvent;
import com.smartparking.model.SensorEvent;
import com.smartparking.model.SessionUpdateEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ParkingController {

    private final ParkingSessionRepository sessionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final DashboardController dashboardController;

    public ParkingController(ParkingSessionRepository sessionRepository, 
                             KafkaTemplate<String, String> kafkaTemplate, 
                             ObjectMapper objectMapper,
                             DashboardController dashboardController) {
        this.sessionRepository = sessionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.dashboardController = dashboardController;
    }

    @KafkaListener(topics = "parking-events", groupId = "controller-sensor-group")
    public void handleSensorEvent(String message) {
        try {
            SensorEvent event = objectMapper.readValue(message, SensorEvent.class);
            String sensorId = event.getSensorId();

            // Check if there is already an active session
            Optional<ParkingSession> existingSession = sessionRepository.findBySensorIdAndEndTimeIsNull(sensorId);

            if (existingSession.isEmpty()) {
                // Create new session
                ParkingSession session = new ParkingSession();
                session.setSensorId(sensorId);
                session.setStartTime(LocalDateTime.now());
                session.setStatus("UNPAID");
                
                sessionRepository.save(session);
                
                System.out.println(">>> Session Created: " + session.getId() + " for sensor " + sensorId);
                
                // Publish update
                publishSessionUpdate(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "controller-payment-group")
    public void handlePaymentEvent(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            String parkingSpot = event.getParkingSpot();

            // Find active session for this spot
            Optional<ParkingSession> existingSession = sessionRepository.findBySensorIdAndEndTimeIsNull(parkingSpot);

            if (existingSession.isPresent()) {
                ParkingSession session = existingSession.get();
                session.setPlate(event.getPlate());
                session.setAmount(event.getAmount());
                session.setStatus("PAID");
                
                sessionRepository.save(session);
                
                System.out.println(">>> Payment Processed: Session " + session.getId() + " is now PAID.");
                
                publishSessionUpdate(session);
            } else {
                // Payment received but no car detected? Or maybe race condition.
                // For now, we can log or create an alert.
                System.out.println(">>> WARNING: Payment received for " + parkingSpot + " but no active session found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishSessionUpdate(ParkingSession session) {
        try {
            SessionUpdateEvent update = new SessionUpdateEvent(
                    session.getId().toString(),
                    session.getStatus(),
                    session.getPlate(),
                    session.getSensorId(),
                    session.getAmount(),
                    System.currentTimeMillis()
            );
            
            String json = objectMapper.writeValueAsString(update);
            kafkaTemplate.send("session.updates", session.getSensorId(), json);
            
            // Send to Dashboard
            dashboardController.sendEvent("sessionUpdate", update);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @KafkaListener(topics = "alert.incident", groupId = "controller-alert-group")
    public void handleAlertEvent(String message) {
        try {
            AlertEvent event = objectMapper.readValue(message, AlertEvent.class);
            // Send to Dashboard
            dashboardController.sendEvent("alert", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // TODO: Implement Alert logic (e.g. check for unpaid sessions > X minutes)
}
