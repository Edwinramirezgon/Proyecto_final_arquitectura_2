package com.demo.zone.infrastructure.adapter.in.dto;

import com.demo.zone.domain.model.SensitiveType;

public class ZoneResponse {

    private Long          id;
    private String        name;
    private double        latitude;
    private double        longitude;
    private double        radiusKm;
    private SensitiveType sensitiveType;
    private int           priority;

    public ZoneResponse() {}

    public ZoneResponse(Long id, String name, double latitude, double longitude,
                        double radiusKm, SensitiveType sensitiveType, int priority) {
        this.id            = id;
        this.name          = name;
        this.latitude      = latitude;
        this.longitude     = longitude;
        this.radiusKm      = radiusKm;
        this.sensitiveType = sensitiveType;
        this.priority      = priority;
    }

    public Long          getId()            { return id; }
    public String        getName()          { return name; }
    public double        getLatitude()      { return latitude; }
    public double        getLongitude()     { return longitude; }
    public double        getRadiusKm()      { return radiusKm; }
    public SensitiveType getSensitiveType() { return sensitiveType; }
    public int           getPriority()      { return priority; }
}
