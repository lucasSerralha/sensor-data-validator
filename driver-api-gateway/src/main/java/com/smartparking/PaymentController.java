package com.smartparking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.model.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment:payment-events}")
    private String paymentTopic;

    public PaymentController(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/pay")
    public ResponseEntity<String> processPayment(
            @RequestParam String plate,
            @RequestParam String parkingSpot,
            @RequestParam BigDecimal amount) {

        logger.info("Received payment request for plate: {} at spot: {}", plate, parkingSpot);

        PaymentEvent event = new PaymentEvent(plate, parkingSpot, amount, System.currentTimeMillis());

        try {
            String jsonMessage = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(paymentTopic, event.getPlate(), jsonMessage);

            logger.info("Payment event sent to Kafka: {}", jsonMessage);

            return ResponseEntity.ok("Payment processed for plate " + plate + " at spot " + parkingSpot);

        } catch (JsonProcessingException e) {
            logger.error("Error serializing payment event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing payment request");
        }
    }
}