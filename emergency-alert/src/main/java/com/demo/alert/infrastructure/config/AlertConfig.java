package com.demo.alert.infrastructure.config;

import com.demo.alert.application.port.out.AlertNotificationRepository;
import com.demo.alert.application.port.out.EmergencyNotifierPort;
import com.demo.alert.application.port.out.MaintenanceEventRepository;
import com.demo.alert.infrastructure.adapter.out.GmailEmergencyNotifierAdapter;
import com.demo.alert.infrastructure.adapter.out.PostgresAlertNotificationAdapter;
import com.demo.alert.infrastructure.adapter.out.PostgresMaintenanceEventAdapter;
import com.demo.alert.infrastructure.adapter.out.persistence.JpaAlertNotificationRepository;
import com.demo.alert.infrastructure.adapter.out.persistence.JpaMaintenanceEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.demo.alert.application.port.out.SubscriberClientPort;
import com.demo.alert.infrastructure.adapter.out.AuthSubscriberAdapter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AlertConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public SubscriberClientPort subscriberClientPort(
            RestTemplate restTemplate,
            @Value("${auth.service.url}") String authServiceUrl) {
        return new AuthSubscriberAdapter(restTemplate, authServiceUrl);
    }

    @Bean
    public EmergencyNotifierPort emergencyNotifierPort(
            JavaMailSender mailSender,
            @Value("${alert.technical.email}") String technicalEmail) {
        return new GmailEmergencyNotifierAdapter(mailSender, technicalEmail);
    }

    @Bean
    public AlertNotificationRepository alertNotificationRepository(
            JpaAlertNotificationRepository jpa) {
        return new PostgresAlertNotificationAdapter(jpa);
    }

    @Bean
    public MaintenanceEventRepository maintenanceEventRepository(
            JpaMaintenanceEventRepository jpa) {
        return new PostgresMaintenanceEventAdapter(jpa);
    }

    @Bean
    public Queue authNotificationQueue(@Value("${auth.notification.queue}") String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    @Bean
    public Queue authNotificationDlq(@Value("${auth.notification.queue}") String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }

    @Bean
    public Queue alertQueue(@Value("${alert.queue.name}") String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    @Bean
    public Queue alertDlq(@Value("${alert.queue.name}") String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }

    @Bean
    public Queue maintenanceQueue(@Value("${maintenance.queue.name}") String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    @Bean
    public Queue maintenanceDlq(@Value("${maintenance.queue.name}") String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false); // mensaje fallido → DLQ, no reencolar infinito
        return factory;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
