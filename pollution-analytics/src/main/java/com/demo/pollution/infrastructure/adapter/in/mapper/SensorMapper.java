package com.demo.pollution.infrastructure.adapter.in.mapper;

import com.demo.pollution.domain.model.Sensor;
import com.demo.pollution.infrastructure.adapter.in.dto.SensorResponse;

public class SensorMapper {

    public static SensorResponse toResponse(Sensor sensor) {
        return new SensorResponse(
            sensor.getId(),
            sensor.getSensorId(),
            sensor.getName(),
            sensor.getZoneId(),
            sensor.getLatitude(),
            sensor.getLongitude(),
            sensor.isActive(),
            sensor.getInstalledAt(),
            sensor.getLastReadingAt()
        );
    }
}
