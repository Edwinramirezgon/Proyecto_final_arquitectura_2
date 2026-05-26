package com.demo.pollution.infrastructure.adapter.in.dto;

import java.time.LocalDateTime;

public class SensorResponse {
    private Long id;
    private String sensorId;
    private String name;
    private String zoneId;
    private double latitude;
    private double longitude;
    private boolean active;
    private LocalDateTime installedAt;
    private LocalDateTime lastReadingAt;

    public SensorResponse(Long id, String sensorId, String name, String zoneId,
                         double latitude, double longitude, boolean active,
                         LocalDateTime installedAt, LocalDateTime lastReadingAt) {
        this.id = id;
        this.sensorId = sensorId;
        this.name = name;
        this.zoneId = zoneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        this.installedAt = installedAt;
        this.lastReadingAt = lastReadingAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getSensorId() { return sensorId; }
    public String getName() { return name; }
    public String getZoneId() { return zoneId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isActive() { return active; }
    public LocalDateTime getInstalledAt() { return installedAt; }
    public LocalDateTime getLastReadingAt() { return lastReadingAt; }
}
