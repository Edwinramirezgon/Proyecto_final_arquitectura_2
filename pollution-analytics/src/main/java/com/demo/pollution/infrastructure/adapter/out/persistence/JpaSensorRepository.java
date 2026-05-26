package com.demo.pollution.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaSensorRepository extends JpaRepository<SensorEntity, Long> {
    Optional<SensorEntity> findBySensorId(String sensorId);
    List<SensorEntity> findByZoneId(String zoneId);
    List<SensorEntity> findByActive(boolean active);
    boolean existsBySensorId(String sensorId);
    void deleteBySensorId(String sensorId);
}
