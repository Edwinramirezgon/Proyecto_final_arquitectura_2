package com.demo.zone.application.port.in;

import com.demo.zone.domain.model.Zone;
import java.util.List;
import java.util.Optional;

public interface ZoneUseCase {
    Zone             create(Zone zone);
    List<Zone>       findAll();
    Zone             findById(Long id);
    Zone             update(Long id, Zone zone);
    void             deleteById(Long id);
    Optional<Zone>   findNearest(double latitude, double longitude, double radiusKm);
}
