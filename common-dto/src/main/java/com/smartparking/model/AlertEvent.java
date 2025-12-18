package com.smartparking.model;

public class AlertEvent {

    private String type; // "OVERSTAY", "UNPAID", etc.
    private String spot;
    private String message;
    private long timestamp;

    public AlertEvent() {
    }

    public AlertEvent(String type, String spot, String message, long timestamp) {
        this.type = type;
        this.spot = spot;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
