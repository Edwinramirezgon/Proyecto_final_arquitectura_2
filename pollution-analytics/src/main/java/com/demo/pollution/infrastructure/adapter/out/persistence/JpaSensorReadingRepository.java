package com.demo.pollution.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaSensorReadingRepository extends JpaRepository<SensorReadingEntity, Long> {

    List<SensorReadingEntity> findBySensorIdAndRecordedAtAfter(String sensorId, LocalDateTime after);

    List<SensorReadingEntity> findByZoneIdAndRecordedAtAfter(String zoneId, LocalDateTime after);

    @Query("SELECT r FROM SensorReadingEntity r WHERE r.sensorId = :sensorId ORDER BY r.recordedAt DESC LIMIT 1")
    Optional<SensorReadingEntity> findLatestBySensorId(@Param("sensorId") String sensorId);
}
