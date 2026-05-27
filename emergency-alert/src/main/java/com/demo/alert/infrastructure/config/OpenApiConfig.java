package com.demo.alert.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-EmergencyAlert API")
                        .version("1.0.0")
                        .description("""
                                Microservicio de alertas de emergencia de EcoMonitor.
                                Consumidor asíncrono de RabbitMQ — escucha alert_queue y maintenance_queue.
                                Notifica al equipo técnico y a suscriptores de la zona afectada por email.
                                
                                **Colas consumidas:**
                                - alert_queue: alertas ambientales declaradas
                                - maintenance_queue: sensores con lecturas inconsistentes
                                """)
                        .contact(new Contact()
                                .name("EcoMonitor")
                                .url("http://localhost:3000")));
    }
}
