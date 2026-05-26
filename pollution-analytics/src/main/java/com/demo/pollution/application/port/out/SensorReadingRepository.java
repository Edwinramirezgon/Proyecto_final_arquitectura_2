package com.demo.pollution.application.port.out;

import com.demo.pollution.domain.model.SensorReading;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorReadingRepository {
    SensorReading          save(SensorReading reading);
    List<SensorReading>    findBySensorIdAfter(String sensorId, LocalDateTime after);
    List<SensorReading>    findByZoneIdAfter(String zoneId, LocalDateTime after);
    Optional<SensorReading> findLatestBySensorId(String sensorId);
}
