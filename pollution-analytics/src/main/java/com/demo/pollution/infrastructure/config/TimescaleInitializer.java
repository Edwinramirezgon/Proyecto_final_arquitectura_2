package com.demo.pollution.infrastructure.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TimescaleInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public TimescaleInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM timescaledb_information.hypertables " +
                "WHERE hypertable_name = 'sensor_readings'",
                Integer.class);
            if (count != null && count > 0)
                System.out.println("[TimescaleDB] Hypertable sensor_readings activa correctamente.");
            else
                System.out.println("[TimescaleDB] Advertencia: sensor_readings no es hypertable.");
        } catch (Exception e) {
            System.out.println("[TimescaleDB] No se pudo verificar hypertable: " + e.getMessage());
        }
    }
}
