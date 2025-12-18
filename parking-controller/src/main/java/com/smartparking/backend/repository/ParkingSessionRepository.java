package com.smartparking.backend.repository;

import com.smartparking.backend.model.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, UUID> {
    
    // Find active session for a sensor (where endTime is null)
    Optional<ParkingSession> findBySensorIdAndEndTimeIsNull(String sensorId);
    
    List<ParkingSession> findByStatus(String status);
}
