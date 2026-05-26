package com.demo.pollution.application.port.in;

import com.demo.pollution.application.port.out.AirQualityClientPort;
import java.util.List;

public interface AirQualityQueryUseCase {
    List<AirQualityClientPort.StationReading> getColombiaReadings();
}
