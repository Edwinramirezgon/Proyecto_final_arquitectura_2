package com.demo.alert.infrastructure.adapter.out;

import com.demo.alert.application.port.out.AlertNotificationRepository;
import com.demo.alert.domain.model.AlertNotification;
import com.demo.alert.infrastructure.adapter.out.persistence.AlertNotificationEntity;
import com.demo.alert.infrastructure.adapter.out.persistence.JpaAlertNotificationRepository;

import java.util.List;
import java.util.Objects;

public class PostgresAlertNotificationAdapter implements AlertNotificationRepository {

    private final JpaAlertNotificationRepository jpa;

    public PostgresAlertNotificationAdapter(JpaAlertNotificationRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public void save(AlertNotification notification) {
        AlertNotificationEntity entity = Objects.requireNonNull(
                AlertNotificationEntity.fromDomain(notification),
                "alert notification entity must be provided");
        jpa.save(entity);
    }

    @Override
    public List<AlertNotification> findAll() {
        return jpa.findAll().stream()
                .map(AlertNotificationEntity::toDomain)
                .toList();
    }
}
