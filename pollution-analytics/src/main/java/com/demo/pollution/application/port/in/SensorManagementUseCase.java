package com.demo.pollution.application.port.in;

import com.demo.pollution.domain.model.Sensor;
import java.util.List;

public interface SensorManagementUseCase {
    Sensor registerSensor(String sensorId, String name, String zoneId, double latitude, double longitude);
    Sensor updateSensor(String sensorId, String name, String zoneId, double latitude, double longitude);
    void deactivateSensor(String sensorId);
    void activateSensor(String sensorId);
    void deleteSensor(String sensorId);
    Sensor getSensor(String sensorId);
    List<Sensor> getAllSensors();
    List<Sensor> getSensorsByZone(String zoneId);
    List<Sensor> getActiveSensors();
}
