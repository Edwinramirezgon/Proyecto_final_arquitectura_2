package com.demo.pollution.infrastructure.config;

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
                        .title("MS-PollutionAnalytics API")
                        .version("1.0.0")
                        .description("""
                                Microservicio principal de EcoMonitor.
                                Procesa lecturas de sensores de CO2, aplica las 3 reglas de negocio,
                                actúa como API Gateway para el frontend y gestiona alertas ambientales.
                                
                                **Reglas de negocio:**
                                - Alerta si ≥3 sensores distintos en la misma zona superan 150 µg/m³
                                - Consulta síncrona a ZoneService para elevar prioridad (CRITICAL vs HIGH)
                                - Lecturas inconsistentes (salto >200 µg/m³) se ignoran y generan evento de mantenimiento
                                """)
                        .contact(new Contact()
                                .name("EcoMonitor")
                                .url("http://localhost:3000")));
    }
}
