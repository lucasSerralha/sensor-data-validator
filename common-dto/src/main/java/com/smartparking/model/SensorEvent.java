package com.smartparking.model;

public class SensorEvent {

    private String sensorId;
    private long time;

    // --- 1. Construtor Vazio (OBRIGATÓRIO para o JSON/Jackson funcionar) ---
    public SensorEvent() {
    }

    // --- 2. Construtor Completo ---
    public SensorEvent(String sensorId, long time) {
        this.sensorId = sensorId;
        this.time = time;
    }

    // --- 3. Getters e Setters Manuais (Resolve o erro "cannot find symbol") ---

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}