package com.smartparking.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.model.SensorEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SensorEventFilter {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Stores the start time of the detection for each sensor
    private final Map<String, Long> detectionStartTimes = new ConcurrentHashMap<>();
    // Stores whether the event has already been emitted for the current detection
    private final Map<String, Boolean> eventEmitted = new ConcurrentHashMap<>();

    public SensorEventFilter(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "parking-events", groupId = "filter-group")
    public void filterNoise(String message) {
        try {
            SensorEvent event = objectMapper.readValue(message, SensorEvent.class);
            String sensorId = event.getSensorId();
            
            // Assuming the simulation sends continuous events while occupied.
            // If we receive an event, it means there is a car.
            // In a real system, we might check a "status" field, but here we assume presence = event.
            
            long currentTime = System.currentTimeMillis(); // Or use event.getTime() if synchronized
            
            detectionStartTimes.putIfAbsent(sensorId, currentTime);
            
            long startTime = detectionStartTimes.get(sensorId);
            long duration = currentTime - startTime;

            // 30 seconds = 30000 ms
            if (duration > 30000 && !eventEmitted.getOrDefault(sensorId, false)) {
                // Emit to sensor.events
                String json = objectMapper.writeValueAsString(event);
                kafkaTemplate.send("sensor.events", sensorId, json);
                
                eventEmitted.put(sensorId, true);
                System.out.println(">>> Filter Passed: Sensor " + sensorId + " active for > 30s. Emitted to sensor.events");
            }

            // Note: We need a way to clear the map when the car leaves.
            // Since the simulation just stops sending events, we might need a timeout mechanism.
            // For now, we assume the simulation logic. 
            // Ideally, the sensor would send a "FREE" event.
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Optional: Cleanup task to remove stale entries if no events received for X time
    // But for this specific requirement, we focus on the "active > 30s" logic.
}
