package com.demo.pollution.infrastructure.adapter.in.dto;

public class SensorRequest {
    private String sensorId;
    private String name;
    private String zoneId;
    private double latitude;
    private double longitude;

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
