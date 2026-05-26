package com.demo.pollution.application.port.out;

import com.demo.pollution.domain.model.Sensor;
import java.util.List;
import java.util.Optional;

public interface SensorRepository {
    Sensor save(Sensor sensor);
    Optional<Sensor> findBySensorId(String sensorId);
    List<Sensor> findAll();
    List<Sensor> findByZoneId(String zoneId);
    List<Sensor> findByActive(boolean active);
    boolean existsBySensorId(String sensorId);
    void deleteBySensorId(String sensorId);
}
