package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.in.SensorQueryUseCase;
import com.demo.pollution.application.port.out.SensorReadingRepository;
import com.demo.pollution.domain.model.SensorReading;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorQueryService implements SensorQueryUseCase {

    private final SensorReadingRepository readingRepository;

    public SensorQueryService(SensorReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    @Override
    public List<SensorReading> findRecentBySensor(String sensorId, int minutes) {
        return readingRepository.findBySensorIdAfter(
                sensorId, LocalDateTime.now().minusMinutes(minutes));
    }
}
