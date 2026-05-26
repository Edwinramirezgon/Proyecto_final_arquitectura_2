package com.demo.pollution.infrastructure.config;

import com.demo.pollution.application.port.out.AirQualityClientPort;
import com.demo.pollution.infrastructure.adapter.out.OpenMeteoAdapter;
import com.demo.pollution.application.port.out.*;
import com.demo.pollution.infrastructure.adapter.out.*;
import com.demo.pollution.infrastructure.adapter.out.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;



@Configuration
public class PollutionConfig {

    @Bean
    public AirQualityClientPort airQualityClientPort(RestTemplate restTemplate,
                                                      ObjectMapper objectMapper) {
        return new OpenMeteoAdapter(restTemplate, objectMapper);
    }

    @Bean
    public SensorReadingRepository sensorReadingRepository(JpaSensorReadingRepository jpa) {
        return new PostgresSensorReadingAdapter(jpa);
    }

    @Bean
    public SensorRepository sensorRepository(JpaSensorRepository jpa) {
        return new PostgresSensorAdapter(jpa);
    }

    @Bean
    public AlertRepository alertRepository(JpaAlertRepository jpa) {
        return new PostgresAlertAdapter(jpa);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public AlertPublisherPort alertPublisherPort(RabbitTemplate rabbitTemplate,
                                                  ObjectMapper objectMapper,
                                                  MeterRegistry meterRegistry,
                                                  @Value("${alert.queue.name}") String queueName) {
        return new RabbitAlertPublisher(rabbitTemplate, objectMapper, queueName, meterRegistry);
    }

    @Bean
    public MaintenancePublisherPort maintenancePublisherPort(RabbitTemplate rabbitTemplate,
                                                              ObjectMapper objectMapper,
                                                              MeterRegistry meterRegistry,
                                                              @Value("${maintenance.queue.name}") String queueName) {
        return new RabbitMaintenancePublisher(rabbitTemplate, objectMapper, queueName, meterRegistry);
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
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
