package com.demo.pollution.application.port.out;

import com.demo.pollution.domain.model.SensorReading;

public interface MaintenancePublisherPort {
    void publish(SensorReading reading, double previousCo2);
}
