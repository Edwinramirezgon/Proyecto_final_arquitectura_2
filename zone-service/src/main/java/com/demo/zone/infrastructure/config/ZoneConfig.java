package com.demo.zone.infrastructure.config;

import com.demo.zone.application.port.out.ZoneRepository;
import com.demo.zone.infrastructure.adapter.out.PostgresZoneAdapter;
import com.demo.zone.infrastructure.adapter.out.persistence.JpaZoneRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZoneConfig {

    @Bean
    public ZoneRepository zoneRepository(JpaZoneRepository jpa) {
        return new PostgresZoneAdapter(jpa);
    }
}
