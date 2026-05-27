package com.demo.alert.infrastructure.adapter.in;

import com.demo.alert.application.port.in.ProcessMaintenanceUseCase;
import com.demo.alert.domain.model.MaintenanceEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RabbitMaintenanceListener {

    private final ProcessMaintenanceUseCase processMaintenanceUseCase;
    private final ObjectMapper              objectMapper;
    private final Counter                   consumedCounter;

    public RabbitMaintenanceListener(ProcessMaintenanceUseCase processMaintenanceUseCase,
                                     ObjectMapper objectMapper,
                                     MeterRegistry meterRegistry) {
        this.processMaintenanceUseCase = processMaintenanceUseCase;
        this.objectMapper              = objectMapper;
        this.consumedCounter           = Counter.builder("rabbitmq.messages.consumed")
                .tag("queue", "maintenance_queue")
                .description("Mensajes consumidos de maintenance_queue")
                .register(meterRegistry);
    }

    @RabbitListener(queues = "${maintenance.queue.name}")
    public void onMaintenance(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            MaintenanceEvent event = MaintenanceEvent.create(
                    node.get("sensorId").asText(),
                    node.get("zoneId").asText(),
                    node.get("previousCo2").asDouble(),
                    node.get("currentCo2").asDouble(),
                    LocalDateTime.parse(node.get("detectedAt").asText())
            );
            processMaintenanceUseCase.process(event);
            consumedCounter.increment();
        } catch (Exception e) {
            // Relanzar para que RabbitMQ envíe el mensaje a la DLQ
            throw new RuntimeException("Error procesando mantenimiento: " + e.getMessage(), e);
        }
    }
}
