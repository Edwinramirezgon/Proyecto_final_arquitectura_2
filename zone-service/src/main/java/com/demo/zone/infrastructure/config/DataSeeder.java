package com.demo.zone.infrastructure.config;

import com.demo.zone.application.port.out.ZoneRepository;
import com.demo.zone.domain.model.SensitiveType;
import com.demo.zone.domain.model.Zone;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private final ZoneRepository zoneRepository;

    public DataSeeder(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!zoneRepository.findAll().isEmpty()) return;

        // ── Hospitales ────────────────────────────────────────────────────────
        zoneRepository.save(Zone.create("Hospital General de Medellín",      6.2526,  -75.5696, 0.5, SensitiveType.HOSPITAL));
        zoneRepository.save(Zone.create("Hospital Pablo Tobón Uribe",        6.2372,  -75.5610, 0.5, SensitiveType.HOSPITAL));
        zoneRepository.save(Zone.create("Hospital Universitario San Vicente", 6.2905,  -75.5555, 0.5, SensitiveType.HOSPITAL));
        zoneRepository.save(Zone.create("Clínica Las Américas",              6.2219,  -75.6106, 0.5, SensitiveType.HOSPITAL));
        zoneRepository.save(Zone.create("Hospital Manuel Uribe Ángel",       6.1687,  -75.5820, 0.5, SensitiveType.HOSPITAL));

        // ── Escuelas / Universidades ──────────────────────────────────────────
        zoneRepository.save(Zone.create("I.E. INEM José Félix de Restrepo",  6.1999,  -75.5610, 0.3, SensitiveType.SCHOOL));
        zoneRepository.save(Zone.create("I.E. Pedro Justo Berrío",           6.2372,  -75.6105, 0.3, SensitiveType.SCHOOL));
        zoneRepository.save(Zone.create("I.E. Ciro Mendía",                  6.2905,  -75.5555, 0.3, SensitiveType.SCHOOL));
        zoneRepository.save(Zone.create("Universidad de Antioquia",          6.2679,  -75.5673, 0.4, SensitiveType.SCHOOL));
        zoneRepository.save(Zone.create("I.E. Fernando Vélez Bello",         6.3376,  -75.5678, 0.3, SensitiveType.SCHOOL));

        // ── Parques ───────────────────────────────────────────────────────────
        zoneRepository.save(Zone.create("Parque Arví",                       6.2779,  -75.4833, 1.5, SensitiveType.PARK));
        zoneRepository.save(Zone.create("Parque de los Deseos",              6.2679,  -75.5673, 0.3, SensitiveType.PARK));
        zoneRepository.save(Zone.create("Parque Biblioteca España",          6.2905,  -75.5555, 0.3, SensitiveType.PARK));
    }
}
