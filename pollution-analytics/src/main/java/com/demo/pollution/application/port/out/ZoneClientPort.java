package com.demo.pollution.application.port.out;

import java.util.Optional;

public interface ZoneClientPort {

    record NearestZone(String name, int priority) {}

    Optional<NearestZone> findNearest(double latitude, double longitude, double radiusKm);
}
