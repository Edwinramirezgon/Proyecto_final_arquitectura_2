package com.demo.auth.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0.0")
                        .description("""
                                Servicio de autenticación de EcoMonitor.
                                Gestiona registro, login, refresh tokens y suscripciones de usuarios a zonas.
                                
                                **Tokens:** JWT con access token (15 min) y refresh token (7 días).
                                Los tokens se almacenan en Redis — revocar = eliminar la clave.
                                """)
                        .contact(new Contact()
                                .name("EcoMonitor")
                                .url("http://localhost:3000")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT obtenido en /auth/login")));
    }
}
