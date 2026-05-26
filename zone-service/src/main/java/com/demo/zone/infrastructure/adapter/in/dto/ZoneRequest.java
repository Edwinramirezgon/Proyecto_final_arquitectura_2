package com.demo.zone.infrastructure.adapter.in.dto;

import com.demo.zone.domain.model.SensitiveType;

public class ZoneRequest {

    private String        name;
    private double        latitude;
    private double        longitude;
    private double        radiusKm;
    private SensitiveType sensitiveType;

    public ZoneRequest() {}

    public String        getName()          { return name; }
    public double        getLatitude()      { return latitude; }
    public double        getLongitude()     { return longitude; }
    public double        getRadiusKm()      { return radiusKm; }
    public SensitiveType getSensitiveType() { return sensitiveType; }

    public void setName(String name)                  { this.name = name; }
    public void setLatitude(double latitude)           { this.latitude = latitude; }
    public void setLongitude(double longitude)         { this.longitude = longitude; }
    public void setRadiusKm(double radiusKm)           { this.radiusKm = radiusKm; }
    public void setSensitiveType(SensitiveType type)   { this.sensitiveType = type; }
}
