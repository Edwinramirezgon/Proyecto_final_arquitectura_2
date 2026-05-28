package com.demo.alert.infrastructure.adapter.in;

import com.demo.alert.application.port.in.ProcessAuthNotificationUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitAuthNotificationListener {

    private final ProcessAuthNotificationUseCase useCase;
    private final ObjectMapper                   objectMapper;

    public RabbitAuthNotificationListener(ProcessAuthNotificationUseCase useCase,
                                          ObjectMapper objectMapper) {
        this.useCase      = useCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${auth.notification.queue}")
    public void onAuthNotification(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            useCase.process(
                    node.get("type").asText(),
                    node.get("email").asText(),
                    node.get("username").asText(),
                    node.has("rawPassword")  ? node.get("rawPassword").asText()  : null,
                    node.has("resetToken")   ? node.get("resetToken").asText()   : null,
                    node.has("appBaseUrl")   ? node.get("appBaseUrl").asText()   : ""
            );
        } catch (Exception e) {
            throw new RuntimeException("Error procesando notificación auth: " + e.getMessage(), e);
        }
    }
}
