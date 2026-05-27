package com.demo.alert.infrastructure.adapter.in;

import com.demo.alert.application.port.in.ProcessAlertUseCase;
import com.demo.alert.domain.model.AlertNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RabbitAlertListener {

    private final ProcessAlertUseCase processAlertUseCase;
    private final ObjectMapper        objectMapper;
    private final Counter             consumedCounter;

    public RabbitAlertListener(ProcessAlertUseCase processAlertUseCase,
                               ObjectMapper objectMapper,
                               MeterRegistry meterRegistry) {
        this.processAlertUseCase = processAlertUseCase;
        this.objectMapper        = objectMapper;
        this.consumedCounter     = Counter.builder("rabbitmq.messages.consumed")
                .tag("queue", "alert_queue")
                .description("Mensajes consumidos de alert_queue")
                .register(meterRegistry);
    }

    @RabbitListener(queues = "${alert.queue.name}")
    public void onAlert(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            AlertNotification notification = AlertNotification.create(
                    node.get("alertId").asLong(),
                    node.get("zoneId").asText(),
                    node.get("averageCo2").asDouble(),
                    node.get("level").asText(),
                    node.has("nearestZoneName") ? node.get("nearestZoneName").asText() : null,
                    node.get("latitude").asDouble(),
                    node.get("longitude").asDouble(),
                    LocalDateTime.parse(node.get("triggeredAt").asText())
            );
            processAlertUseCase.process(notification);
            consumedCounter.increment();
        } catch (Exception e) {
            // Relanzar para que RabbitMQ envíe el mensaje a la DLQ
            // en lugar de perderlo silenciosamente
            throw new RuntimeException("Error procesando alerta: " + e.getMessage(), e);
        }
    }
}
