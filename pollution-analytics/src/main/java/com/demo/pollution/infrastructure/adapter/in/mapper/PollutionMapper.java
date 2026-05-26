package com.demo.pollution.infrastructure.adapter.in.mapper;

import com.demo.pollution.domain.model.Alert;
import com.demo.pollution.domain.model.SensorReading;
import com.demo.pollution.infrastructure.adapter.in.dto.AlertResponse;
import com.demo.pollution.infrastructure.adapter.in.dto.SensorReadingRequest;

public class PollutionMapper {

    private PollutionMapper() {}

    public static SensorReading toDomain(SensorReadingRequest req) {
        return SensorReading.create(
                req.getSensorId(),
                req.getZoneId(),
                req.getLatitude(),
                req.getLongitude(),
                req.getCo2Level()
        );
    }

    public static AlertResponse toResponse(Alert a) {
        return new AlertResponse(
                a.getId(),
                a.getZoneId(),
                a.getAverageCo2(),
                a.getLevel(),
                a.getNearestZoneName(),
                a.getLatitude(),
                a.getLongitude(),
                a.getTriggeredAt(),
                a.isActive(),
                a.getResolvedReason(),
                a.getResolvedAt()
        );
    }
}
