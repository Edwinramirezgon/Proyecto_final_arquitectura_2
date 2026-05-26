package com.demo.zone.infrastructure.adapter.out;

import com.demo.zone.application.port.out.ZoneRepository;
import com.demo.zone.domain.model.Zone;
import com.demo.zone.infrastructure.adapter.out.persistence.JpaZoneRepository;
import com.demo.zone.infrastructure.adapter.out.persistence.ZoneEntity;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class PostgresZoneAdapter implements ZoneRepository {

    private final JpaZoneRepository jpa;

    public PostgresZoneAdapter(JpaZoneRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public Zone save(Zone zone) {
        ZoneEntity entity = Objects.requireNonNull(ZoneEntity.fromDomain(zone), "zone entity must be provided");
        return jpa.save(entity).toDomain();
    }

    @Override
    public List<Zone> findAll() {
        return jpa.findAll().stream().map(ZoneEntity::toDomain).toList();
    }

    @Override
    public Optional<Zone> findById(Long id) {
        return jpa.findById(Objects.requireNonNull(id, "zone id must be provided"))
                .map(ZoneEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(Objects.requireNonNull(id, "zone id must be provided"));
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(Objects.requireNonNull(id, "zone id must be provided"));
    }
}
