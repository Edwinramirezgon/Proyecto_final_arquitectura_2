package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.in.AirQualityQueryUseCase;
import com.demo.pollution.application.port.out.AirQualityClientPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirQualityQueryService implements AirQualityQueryUseCase {

    private final AirQualityClientPort airQualityClientPort;

    public AirQualityQueryService(AirQualityClientPort airQualityClientPort) {
        this.airQualityClientPort = airQualityClientPort;
    }

    @Override
    public List<AirQualityClientPort.StationReading> getColombiaReadings() {
        return airQualityClientPort.fetchColombiaReadings();
    }
}
