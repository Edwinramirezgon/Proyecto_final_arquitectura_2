package com.demo.zone.infrastructure.config;

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
                        .title("MS-ZoneService API")
                        .version("1.0.0")
                        .description("""
                                Microservicio de zonas sensibles de EcoMonitor.
                                Gestiona hospitales, escuelas y parques con sus coordenadas geográficas.
                                Expone endpoint de proximidad con fórmula Haversine para detectar
                                zonas sensibles cercanas a un punto de contaminación.
                                
                                **Datos semilla:** 5 zonas reales de Bogotá D.C.
                                """)
                        .contact(new Contact()
                                .name("EcoMonitor")
                                .url("http://localhost:3000")));
    }
}
