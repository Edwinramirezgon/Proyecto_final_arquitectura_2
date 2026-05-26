package com.demo.pollution.domain.exception;

public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException(String sensorId) {
        super("Sensor no encontrado: " + sensorId);
    }
}
