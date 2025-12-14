package com.smartparking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.model.SensorEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SimulationController(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Adicionámos @RequestParam para ler o "id" e "time" do curl
    @PostMapping("/trigger")
    public String triggerSimulation(
            @RequestParam(value = "id", defaultValue = "sensor-teste-01") String sensorId,
            @RequestParam(value = "time", defaultValue = "10") int timeSeconds
    ) {
        // Criamos uma Thread nova para o loop não bloquear a resposta HTTP
        new Thread(() -> {
            System.out.println(">>> A iniciar simulação para: " + sensorId + " (" + timeSeconds + "s)");

            for (int i = 0; i < timeSeconds; i++) {
                try {
                    // NOTA: Aqui assumo que o construtor é (String id, long value).
                    // Para simular "Ocupado" (carro na vaga), talvez precises de enviar 1 em vez do timestamp.
                    // Se o teu sistema usa timestamp para detetar presença, mantém assim.
                    // Se usa 0/1 (Livre/Ocupado), muda o segundo argumento para 1L.

                    long valorSimulado = System.currentTimeMillis();
                    // Se quiseres forçar "ocupado", experimenta: long valorSimulado = 1L;

                    SensorEvent evento = new SensorEvent(sensorId, valorSimulado);

                    String jsonMessage = objectMapper.writeValueAsString(evento);

                    // Envia para o Kafka
                    kafkaTemplate.send("parking-events", evento.getSensorId(), jsonMessage);

                    // Espera 1 segundo (1000 ms) antes de enviar o próximo
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(">>> Simulação terminada para: " + sensorId);
        }).start();

        return "Simulação iniciada para o sensor: " + sensorId + ". O carro vai ocupar a vaga por " + timeSeconds + " segundos.";
    }
}