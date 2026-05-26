package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.SensorReadingRepository;
import com.demo.pollution.domain.model.SensorReading;
import com.demo.pollution.infrastructure.adapter.out.persistence.JpaSensorReadingRepository;
import com.demo.pollution.infrastructure.adapter.out.persistence.SensorReadingEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class PostgresSensorReadingAdapter implements SensorReadingRepository {

    private final JpaSensorReadingRepository jpa;

    public PostgresSensorReadingAdapter(JpaSensorReadingRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public SensorReading save(SensorReading reading) {
        SensorReadingEntity entity = Objects.requireNonNull(
                SensorReadingEntity.fromDomain(reading),
                "sensor reading entity must be provided");
        return jpa.save(entity).toDomain();
    }

    @Override
    public List<SensorReading> findBySensorIdAfter(String sensorId, LocalDateTime after) {
        return jpa.findBySensorIdAndRecordedAtAfter(sensorId, after)
                .stream().map(SensorReadingEntity::toDomain).toList();
    }

    @Override
    public List<SensorReading> findByZoneIdAfter(String zoneId, LocalDateTime after) {
        return jpa.findByZoneIdAndRecordedAtAfter(zoneId, after)
                .stream().map(SensorReadingEntity::toDomain).toList();
    }

    @Override
    public Optional<SensorReading> findLatestBySensorId(String sensorId) {
        return jpa.findLatestBySensorId(sensorId).map(SensorReadingEntity::toDomain);
    }
}
