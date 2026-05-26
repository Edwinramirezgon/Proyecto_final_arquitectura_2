package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.SensorRepository;
import com.demo.pollution.domain.model.Sensor;
import com.demo.pollution.infrastructure.adapter.out.persistence.JpaSensorRepository;
import com.demo.pollution.infrastructure.adapter.out.persistence.SensorEntity;

import java.util.List;
import java.util.Optional;

public class PostgresSensorAdapter implements SensorRepository {

    private final JpaSensorRepository jpa;

    public PostgresSensorAdapter(JpaSensorRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Sensor save(Sensor sensor) {
        SensorEntity entity = toEntity(sensor);
        SensorEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Sensor> findBySensorId(String sensorId) {
        return jpa.findBySensorId(sensorId).map(this::toDomain);
    }

    @Override
    public List<Sensor> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Sensor> findByZoneId(String zoneId) {
        return jpa.findByZoneId(zoneId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Sensor> findByActive(boolean active) {
        return jpa.findByActive(active).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsBySensorId(String sensorId) {
        return jpa.existsBySensorId(sensorId);
    }

    @Override
    public void deleteBySensorId(String sensorId) {
        jpa.deleteBySensorId(sensorId);
    }

    private SensorEntity toEntity(Sensor sensor) {
        SensorEntity e = new SensorEntity();
        e.setId(sensor.getId());
        e.setSensorId(sensor.getSensorId());
        e.setName(sensor.getName());
        e.setZoneId(sensor.getZoneId());
        e.setLatitude(sensor.getLatitude());
        e.setLongitude(sensor.getLongitude());
        e.setActive(sensor.isActive());
        e.setInstalledAt(sensor.getInstalledAt());
        e.setLastReadingAt(sensor.getLastReadingAt());
        return e;
    }

    private Sensor toDomain(SensorEntity e) {
        return Sensor.reconstitute(
            e.getId(),
            e.getSensorId(),
            e.getName(),
            e.getZoneId(),
            e.getLatitude(),
            e.getLongitude(),
            e.isActive(),
            e.getInstalledAt(),
            e.getLastReadingAt()
        );
    }
}
