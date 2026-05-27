package com.demo.alert.infrastructure.adapter.out;

import com.demo.alert.application.port.out.MaintenanceEventRepository;
import com.demo.alert.domain.model.MaintenanceEvent;
import com.demo.alert.infrastructure.adapter.out.persistence.JpaMaintenanceEventRepository;
import com.demo.alert.infrastructure.adapter.out.persistence.MaintenanceEventEntity;

import java.util.Objects;

public class PostgresMaintenanceEventAdapter implements MaintenanceEventRepository {

    private final JpaMaintenanceEventRepository jpa;

    public PostgresMaintenanceEventAdapter(JpaMaintenanceEventRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public void save(MaintenanceEvent event) {
        MaintenanceEventEntity entity = Objects.requireNonNull(
                MaintenanceEventEntity.fromDomain(event),
                "maintenance event entity must be provided");
        jpa.save(entity);
    }
}
