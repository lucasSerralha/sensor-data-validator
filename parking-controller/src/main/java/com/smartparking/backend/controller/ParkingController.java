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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ParkingController {

    private static final Logger logger = LoggerFactory.getLogger(ParkingController.class);

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

    private final java.util.Map<String, LocalDateTime> pendingSessions = new java.util.concurrent.ConcurrentHashMap<>();

    @KafkaListener(topics = "parking-events", groupId = "controller-sensor-group")
    public void handleSensorEvent(String message) {
        try {
            SensorEvent event = objectMapper.readValue(message, SensorEvent.class);
            String sensorId = event.getSensorId();

            // Check if there is already an active session
            Optional<ParkingSession> existingSession = sessionRepository.findBySensorIdAndEndTimeIsNull(sensorId);

            if (existingSession.isEmpty()) {
                // Logic for 30-second delay
                LocalDateTime now = LocalDateTime.now();
                
                if (!pendingSessions.containsKey(sensorId)) {
                    pendingSessions.put(sensorId, now);
                    logger.info(">>> Sensor {} active. Waiting 30s to confirm session...", sensorId);
                } else {
                    // We have seen it before
                    LocalDateTime firstSeen = pendingSessions.get(sensorId);
                    if (now.isAfter(firstSeen.plusSeconds(30))) {
                        // It has been more than 30 seconds
                        
                        // Create new session
                        ParkingSession session = new ParkingSession();
                        session.setSensorId(sensorId);
                        session.setStartTime(firstSeen); // Use the time we first saw it
                        session.setLastEventTime(now); // Set initial last event time
                        session.setStatus("UNPAID");
                        
                        sessionRepository.save(session);
                        
                        logger.info(">>> Session Created: {} for sensor {}", session.getId(), sensorId);
                        
                        // Publish update
                        publishSessionUpdate(session);
                        
                        // Remove from pending so we don't create it again
                        pendingSessions.remove(sensorId);
                    }
                }
            } else {
                // Session already exists, update lastEventTime
                ParkingSession session = existingSession.get();
                session.setLastEventTime(LocalDateTime.now());
                sessionRepository.save(session);
                
                // Ensure it's not in pending
                pendingSessions.remove(sensorId);
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
                
                // Calculate paid time: 0.10 EUR = 1 minute (For Demo)
                // minutes = (amount / 0.10) * 1
                if (event.getAmount() != null) {
                    long minutesPaid = event.getAmount().divide(new BigDecimal("0.10"), java.math.MathContext.DECIMAL32)
                            .multiply(new BigDecimal("1")).longValue();
                    
                    // paidUntil = startTime + minutesPaid
                    session.setPaidUntil(session.getStartTime().plusMinutes(minutesPaid));
                    
                    logger.info(">>> Payment: {} EUR -> {} minutes. Paid until: {}", event.getAmount(), minutesPaid, session.getPaidUntil());
                }
                
                sessionRepository.save(session);
                
                System.out.println(">>> Payment Processed: Session " + session.getId() + " is now PAID.");
                
                publishSessionUpdate(session);
            } else {
                // Payment received but no car detected? Or maybe race condition.
                // For now, we can log or create an alert.
                logger.warn(">>> WARNING: Payment received for {} but no active session found.", parkingSpot);
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
            
            // Handle Expiration Logic: Update Session to UNPAID
            if ("PAID_EXPIRED".equals(event.getType())) {
                Optional<ParkingSession> existingSession = sessionRepository.findBySensorIdAndEndTimeIsNull(event.getSpot());
                if (existingSession.isPresent()) {
                    ParkingSession session = existingSession.get();
                    session.setStatus("UNPAID");
                    sessionRepository.save(session);
                    
                    logger.info(">>> Session {} expired. Status reverted to UNPAID.", session.getId());
                    publishSessionUpdate(session);
                }
            }
            
            // Send to Dashboard
            dashboardController.sendEvent("alert", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    
    // Scheduled task to check for terminated sessions (no sensor data for > 30s)
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 10000)
    public void checkSessionTermination() {
        LocalDateTime now = LocalDateTime.now();
        // Find active sessions
        java.util.List<ParkingSession> activeSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getEndTime() == null)
                .toList();
        
        for (ParkingSession session : activeSessions) {
            // If lastEventTime is null (legacy) or older than 30 seconds
            if (session.getLastEventTime() != null && session.getLastEventTime().isBefore(now.minusSeconds(30))) {
                
                session.setEndTime(now);
                session.setStatus("TERMINATED"); // Or "FINISHED"
                sessionRepository.save(session);
                
                logger.info(">>> Session Terminated (Timeout): {}", session.getId());
                publishSessionUpdate(session);
            }
        }
    }
}
