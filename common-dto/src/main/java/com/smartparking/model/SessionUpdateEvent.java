package com.smartparking.model;

import java.math.BigDecimal;

public class SessionUpdateEvent {

    private String sessionId;
    private String status; // "PAID", "UNPAID"
    private String plate;
    private String parkingSpot;
    private BigDecimal amount;
    private long timestamp;

    public SessionUpdateEvent() {
    }

    public SessionUpdateEvent(String sessionId, String status, String plate, String parkingSpot, BigDecimal amount,
            long timestamp) {
        this.sessionId = sessionId;
        this.status = status;
        this.plate = plate;
        this.parkingSpot = parkingSpot;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(String parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
