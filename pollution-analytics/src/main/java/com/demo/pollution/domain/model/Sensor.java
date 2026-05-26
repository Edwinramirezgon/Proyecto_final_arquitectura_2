package com.demo.pollution.domain.model;

import java.time.LocalDateTime;

public class Sensor {

    private static final double LAT_MIN = -4.23;
    private static final double LAT_MAX = 12.45;
    private static final double LON_MIN = -81.73;
    private static final double LON_MAX = -66.87;

    private Long id;
    private String sensorId;
    private String name;
    private String zoneId;
    private double latitude;
    private double longitude;
    private boolean active;
    private LocalDateTime installedAt;
    private LocalDateTime lastReadingAt;

    private Sensor() {}

    public static Sensor create(String sensorId, String name, String zoneId,
                                double latitude, double longitude) {
        if (sensorId == null || sensorId.isBlank())
            throw new IllegalArgumentException("El identificador del sensor es obligatorio.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre del sensor es obligatorio.");
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");
        if (latitude < LAT_MIN || latitude > LAT_MAX || longitude < LON_MIN || longitude > LON_MAX)
            throw new IllegalArgumentException(
                "Las coordenadas del sensor deben estar dentro del territorio colombiano "
                + "(lat " + LAT_MIN + "–" + LAT_MAX + ", lon " + LON_MIN + "–" + LON_MAX + ").");

        Sensor s = new Sensor();
        s.sensorId = sensorId;
        s.name = name;
        s.zoneId = zoneId;
        s.latitude = latitude;
        s.longitude = longitude;
        s.active = true;
        s.installedAt = LocalDateTime.now();
        return s;
    }

    public static Sensor reconstitute(Long id, String sensorId, String name, String zoneId,
                                      double latitude, double longitude, boolean active,
                                      LocalDateTime installedAt, LocalDateTime lastReadingAt) {
        Sensor s = new Sensor();
        s.id = id;
        s.sensorId = sensorId;
        s.name = name;
        s.zoneId = zoneId;
        s.latitude = latitude;
        s.longitude = longitude;
        s.active = active;
        s.installedAt = installedAt;
        s.lastReadingAt = lastReadingAt;
        return s;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateLastReading() {
        this.lastReadingAt = LocalDateTime.now();
    }

    public void updateLocation(double latitude, double longitude) {
        if (latitude < LAT_MIN || latitude > LAT_MAX || longitude < LON_MIN || longitude > LON_MAX)
            throw new IllegalArgumentException("Coordenadas fuera del territorio colombiano.");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre del sensor es obligatorio.");
        this.name = name;
    }

    public void updateZone(String zoneId) {
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");
        this.zoneId = zoneId;
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

    public void setId(Long id) { this.id = id; }
}
