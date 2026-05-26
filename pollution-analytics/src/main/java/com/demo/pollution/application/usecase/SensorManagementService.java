package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.in.SensorManagementUseCase;
import com.demo.pollution.application.port.out.SensorRepository;
import com.demo.pollution.domain.exception.SensorNotFoundException;
import com.demo.pollution.domain.model.Sensor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SensorManagementService implements SensorManagementUseCase {

    private final SensorRepository sensorRepository;

    public SensorManagementService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @Override
    public Sensor registerSensor(String sensorId, String name, String zoneId, double latitude, double longitude) {
        if (sensorRepository.existsBySensorId(sensorId)) {
            throw new IllegalStateException("Ya existe un sensor con el ID: " + sensorId);
        }
        Sensor sensor = Sensor.create(sensorId, name, zoneId, latitude, longitude);
        return sensorRepository.save(sensor);
    }

    @Override
    public Sensor updateSensor(String sensorId, String name, String zoneId, double latitude, double longitude) {
        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new SensorNotFoundException(sensorId));
        
        sensor.updateName(name);
        sensor.updateZone(zoneId);
        sensor.updateLocation(latitude, longitude);
        
        return sensorRepository.save(sensor);
    }

    @Override
    public void deactivateSensor(String sensorId) {
        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new SensorNotFoundException(sensorId));
        sensor.deactivate();
        sensorRepository.save(sensor);
    }

    @Override
    public void activateSensor(String sensorId) {
        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new SensorNotFoundException(sensorId));
        sensor.activate();
        sensorRepository.save(sensor);
    }

    @Override
    public void deleteSensor(String sensorId) {
        if (!sensorRepository.existsBySensorId(sensorId)) {
            throw new SensorNotFoundException(sensorId);
        }
        sensorRepository.deleteBySensorId(sensorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Sensor getSensor(String sensorId) {
        return sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new SensorNotFoundException(sensorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> getSensorsByZone(String zoneId) {
        return sensorRepository.findByZoneId(zoneId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> getActiveSensors() {
        return sensorRepository.findByActive(true);
    }
}
