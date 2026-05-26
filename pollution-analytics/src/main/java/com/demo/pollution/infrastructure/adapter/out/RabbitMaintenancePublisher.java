package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.MaintenancePublisherPort;
import com.demo.pollution.domain.model.SensorReading;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

public class RabbitMaintenancePublisher implements MaintenancePublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper   objectMapper;
    private final String         maintenanceQueueName;
    private final Counter        publishedCounter;
    private final Counter        errorCounter;

    public RabbitMaintenancePublisher(RabbitTemplate rabbitTemplate,
                                      ObjectMapper objectMapper,
                                      String maintenanceQueueName,
                                      MeterRegistry meterRegistry) {
        this.rabbitTemplate       = rabbitTemplate;
        this.objectMapper         = objectMapper;
        this.maintenanceQueueName = maintenanceQueueName;
        this.publishedCounter     = Counter.builder("rabbitmq.messages.published")
                .tag("queue", maintenanceQueueName)
                .description("Mensajes publicados en maintenance_queue")
                .register(meterRegistry);
        this.errorCounter         = Counter.builder("rabbitmq.messages.errors")
                .tag("queue", maintenanceQueueName)
                .description("Errores al publicar en maintenance_queue")
                .register(meterRegistry);
    }

    @Override
    public void publish(SensorReading reading, double previousCo2) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "sensorId",    reading.getSensorId(),
                    "zoneId",      reading.getZoneId(),
                    "previousCo2", previousCo2,
                    "currentCo2",  reading.getCo2Level(),
                    "detectedAt",  reading.getRecordedAt().toString()
            ));
            rabbitTemplate.convertAndSend(maintenanceQueueName, payload, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
            publishedCounter.increment();
        } catch (Exception e) {
            errorCounter.increment();
            throw new RuntimeException("Error al publicar evento de mantenimiento.", e);
        }
    }
}
