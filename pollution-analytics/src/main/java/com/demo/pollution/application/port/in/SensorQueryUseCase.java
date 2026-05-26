package com.demo.pollution.application.port.in;

import com.demo.pollution.domain.model.SensorReading;
import java.util.List;

public interface SensorQueryUseCase {
    List<SensorReading> findRecentBySensor(String sensorId, int minutes);
}
