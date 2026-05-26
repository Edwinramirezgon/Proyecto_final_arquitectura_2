package com.demo.pollution.infrastructure.adapter.in.dto;

public class SensorReadingRequest {

    private String sensorId;
    private String zoneId;
    private double latitude;
    private double longitude;
    private double co2Level;

    public SensorReadingRequest() {}

    public String getSensorId()  { return sensorId; }
    public String getZoneId()    { return zoneId; }
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public double getCo2Level()  { return co2Level; }

    public void setSensorId(String sensorId)   { this.sensorId = sensorId; }
    public void setZoneId(String zoneId)       { this.zoneId = zoneId; }
    public void setLatitude(double latitude)   { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setCo2Level(double co2Level)   { this.co2Level = co2Level; }
}
