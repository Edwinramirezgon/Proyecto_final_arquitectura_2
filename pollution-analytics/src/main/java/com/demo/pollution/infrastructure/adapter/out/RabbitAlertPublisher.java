package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.AlertPublisherPort;
import com.demo.pollution.domain.model.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

public class RabbitAlertPublisher implements AlertPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper   objectMapper;
    private final String         alertQueueName;
    private final Counter        publishedCounter;
    private final Counter        errorCounter;

    public RabbitAlertPublisher(RabbitTemplate rabbitTemplate,
                                ObjectMapper objectMapper,
                                String alertQueueName,
                                MeterRegistry meterRegistry) {
        this.rabbitTemplate   = rabbitTemplate;
        this.objectMapper     = objectMapper;
        this.alertQueueName   = alertQueueName;
        this.publishedCounter = Counter.builder("rabbitmq.messages.published")
                .tag("queue", alertQueueName)
                .description("Mensajes publicados en alert_queue")
                .register(meterRegistry);
        this.errorCounter     = Counter.builder("rabbitmq.messages.errors")
                .tag("queue", alertQueueName)
                .description("Errores al publicar en alert_queue")
                .register(meterRegistry);
    }

    @Override
    public void publish(Alert alert) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "alertId",          alert.getId(),
                    "zoneId",           alert.getZoneId(),
                    "averageCo2",       alert.getAverageCo2(),
                    "level",            alert.getLevel().name(),
                    "nearestZoneName",  alert.getNearestZoneName() != null ? alert.getNearestZoneName() : "",
                    "latitude",         alert.getLatitude(),
                    "longitude",        alert.getLongitude(),
                    "triggeredAt",      alert.getTriggeredAt().toString()
            ));
            rabbitTemplate.convertAndSend(alertQueueName, payload, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
            publishedCounter.increment();
        } catch (Exception e) {
            errorCounter.increment();
            throw new RuntimeException("Error al publicar alerta en RabbitMQ.", e);
        }
    }
}
