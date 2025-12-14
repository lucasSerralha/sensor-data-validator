package com.smartparking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class PaymentEvent {

    private String plate;
    private String parkingSpot; // Novo campo: ex "A1", "B-05"
    private BigDecimal amount;

    @JsonProperty("timestamp")
    private long timestamp;

    // Construtor vazio (Obrigatório para o Jackson/Kafka Consumer)
    public PaymentEvent() {
    }

    // Construtor completo atualizado
    public PaymentEvent(String plate, String parkingSpot, BigDecimal amount, long timestamp) {
        this.plate = plate;
        this.parkingSpot = parkingSpot;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // --- Getters e Setters ---

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getParkingSpot() { return parkingSpot; }
    public void setParkingSpot(String parkingSpot) { this.parkingSpot = parkingSpot; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}