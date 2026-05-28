package com.demo.auth.infrastructure.adapter.out;

import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

public class RabbitAuthNotifierAdapter implements EmailNotifierPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper   objectMapper;
    private final String         queueName;
    private final String         appBaseUrl;

    public RabbitAuthNotifierAdapter(RabbitTemplate rabbitTemplate,
                                     ObjectMapper objectMapper,
                                     String queueName,
                                     String appBaseUrl) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper   = objectMapper;
        this.queueName      = queueName;
        this.appBaseUrl     = appBaseUrl;
    }

    @Override
    public void sendWelcome(User user, String rawPassword) {
        publish(Map.of(
                "type",        "WELCOME",
                "email",       user.getEmail(),
                "username",    user.getUsername(),
                "rawPassword", rawPassword,
                "appBaseUrl",  appBaseUrl
        ));
    }

    @Override
    public void sendPasswordChanged(User user) {
        publish(Map.of(
                "type",       "PASSWORD_CHANGED",
                "email",      user.getEmail(),
                "username",   user.getUsername(),
                "appBaseUrl", appBaseUrl
        ));
    }

    @Override
    public void sendPasswordResetLink(User user, String resetToken) {
        publish(Map.of(
                "type",       "PASSWORD_RESET",
                "email",      user.getEmail(),
                "username",   user.getUsername(),
                "resetToken", resetToken,
                "appBaseUrl", appBaseUrl
        ));
    }

    private void publish(Map<String, String> payload) {
        try {
            rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            // fallo de mensajería no interrumpe el flujo principal
        }
    }
}
