package com.demo.pollution.application.port.out;

import java.util.List;

public interface AirQualityClientPort {
    record StationReading(String stationId, String city, double latitude,
                          double longitude, double aqiUs, double pm25) {}
    List<StationReading> fetchColombiaReadings();
}
