package com.demo.zone.infrastructure.adapter.in.mapper;

import com.demo.zone.domain.model.Zone;
import com.demo.zone.infrastructure.adapter.in.dto.ZoneRequest;
import com.demo.zone.infrastructure.adapter.in.dto.ZoneResponse;

public class ZoneMapper {

    private ZoneMapper() {}

    public static Zone toDomain(ZoneRequest req) {
        return Zone.create(
                req.getName(),
                req.getLatitude(),
                req.getLongitude(),
                req.getRadiusKm(),
                req.getSensitiveType()
        );
    }

    public static ZoneResponse toResponse(Zone z) {
        return new ZoneResponse(
                z.getId(),
                z.getName(),
                z.getLatitude(),
                z.getLongitude(),
                z.getRadiusKm(),
                z.getSensitiveType(),
                z.getPriority()
        );
    }
}
