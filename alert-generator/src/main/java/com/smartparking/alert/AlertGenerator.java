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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AlertGenerator.class);

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

    // Run every 10 seconds for debugging
    // Run every 10 seconds for debugging
    @Scheduled(fixedRate = 10000)
    public void checkUnpaidSessions() {
        logger.info(">>> Checking for sessions...");
        List<ParkingSession> allSessions = sessionRepository.findAll(); // Check ALL sessions
        logger.info(">>> Found {} total sessions.", allSessions.size());
        LocalDateTime now = LocalDateTime.now();

        for (ParkingSession session : allSessions) {
            
            // 1. Check UNPAID > 1 minute
            if ("UNPAID".equals(session.getStatus())) {
                 if (!session.isAlerted() && session.getStartTime().isBefore(now.minusMinutes(1))) {
                    logger.info(">>> UNPAID Alert: Session {}", session.getId());
                    sendAlert(session, "UNPAID_OVERSTAY", "Vehicle in spot " + session.getSensorId() + " unpaid for > 1 minutes.");
                    session.setAlerted(true);
                    sessionRepository.save(session);
                }
            }
            
            // 2. Check PAID but EXPIRED
            else if ("PAID".equals(session.getStatus())) {
                if (session.getPaidUntil() != null && now.isAfter(session.getPaidUntil())) {
                    // Only alert if not already alerted (or maybe we want repeated alerts? Let's stick to once for now)
                    if (!session.isAlerted()) {
                        logger.info(">>> EXPIRED Alert: Session {}", session.getId());
                        sendAlert(session, "PAID_EXPIRED", "Vehicle in spot " + session.getSensorId() + " expired at " + session.getPaidUntil());
                        
                        session.setAlerted(true);
                        session.setStatus("UNPAID"); // Reset status to UNPAID
                        sessionRepository.save(session);
                    }
                }
            }
        }
    }

    private void sendAlert(ParkingSession session, String type, String message) {
        try {
            AlertEvent alert = new AlertEvent(
                    type,
                    session.getSensorId(),
                    message,
                    System.currentTimeMillis()
            );

            String json = objectMapper.writeValueAsString(alert);
            kafkaTemplate.send("alert.incident", session.getSensorId(), json);
            
            logger.info(">>> Alert Sent: {}", alert.getMessage());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
