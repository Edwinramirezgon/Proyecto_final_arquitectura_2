package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.AlertRepository;
import com.demo.pollution.domain.model.Alert;
import com.demo.pollution.infrastructure.adapter.out.persistence.AlertEntity;
import com.demo.pollution.infrastructure.adapter.out.persistence.JpaAlertRepository;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class PostgresAlertAdapter implements AlertRepository {

    private final JpaAlertRepository jpa;

    public PostgresAlertAdapter(JpaAlertRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public Alert save(Alert alert) {
        AlertEntity entity = Objects.requireNonNull(AlertEntity.fromDomain(alert), "alert entity must be provided");
        return jpa.save(entity).toDomain();
    }

    @Override
    public List<Alert> findAll() {
        return jpa.findAll().stream().map(AlertEntity::toDomain).toList();
    }

    @Override
    public List<Alert> findAllActive() {
        return jpa.findByActiveTrue().stream().map(AlertEntity::toDomain).toList();
    }

    @Override
    public Optional<Alert> findById(Long id) {
        return jpa.findById(Objects.requireNonNull(id, "alert id must be provided"))
                .map(AlertEntity::toDomain);
    }
}
