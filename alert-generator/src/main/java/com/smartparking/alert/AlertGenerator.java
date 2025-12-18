package com.smartparking.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.model.AlertEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AlertGenerator {

    private final ParkingSessionRepository sessionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AlertGenerator(ParkingSessionRepository sessionRepository, 
                          KafkaTemplate<String, String> kafkaTemplate, 
                          ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Run every minute
    @Scheduled(fixedRate = 60000)
    public void checkUnpaidSessions() {
        List<ParkingSession> unpaidSessions = sessionRepository.findByStatus("UNPAID");
        LocalDateTime now = LocalDateTime.now();

        for (ParkingSession session : unpaidSessions) {
            // Check if session started more than 5 minutes ago and not yet alerted
            if (!session.isAlerted() && session.getStartTime().isBefore(now.minusMinutes(5))) {
                
                sendAlert(session);
                
                session.setAlerted(true);
                sessionRepository.save(session);
            }
        }
    }

    private void sendAlert(ParkingSession session) {
        try {
            AlertEvent alert = new AlertEvent(
                    "UNPAID_OVERSTAY",
                    session.getSensorId(),
                    "Vehicle in spot " + session.getSensorId() + " unpaid for > 5 minutes.",
                    System.currentTimeMillis()
            );

            String json = objectMapper.writeValueAsString(alert);
            kafkaTemplate.send("alert.incident", session.getSensorId(), json);
            
            System.out.println(">>> Alert Sent: " + alert.getMessage());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
