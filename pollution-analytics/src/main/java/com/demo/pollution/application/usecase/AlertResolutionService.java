package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.in.ResolveAlertUseCase;
import com.demo.pollution.application.port.out.AlertRepository;
import com.demo.pollution.application.port.out.SensorReadingRepository;
import com.demo.pollution.domain.model.Alert;
import com.demo.pollution.domain.model.ResolutionReason;
import com.demo.pollution.domain.model.SensorReading;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class AlertResolutionService implements ResolveAlertUseCase {

    /** Por debajo de este promedio se considera que la zona se normalizó. */
    private static final double RESOLUTION_THRESHOLD = 100.0;
    private static final int    WINDOW_MINUTES        = 5;
    private static final int    TIMEOUT_HOURS         = 6;

    private final AlertRepository         alertRepository;
    private final SensorReadingRepository readingRepository;

    public AlertResolutionService(AlertRepository alertRepository,
                                  SensorReadingRepository readingRepository) {
        this.alertRepository   = alertRepository;
        this.readingRepository = readingRepository;
    }

    /**
     * Se ejecuta cada 5 minutos.
     * Para cada alerta activa evalúa dos condiciones de resolución:
     *   NORMALIZED — el promedio de lecturas recientes bajó del umbral de resolución.
     *   TIMEOUT    — la alerta lleva más de 6 horas activa sin normalizarse.
     */
    @Override
    public void resolveStaleAlerts() {
        List<Alert> active = alertRepository.findAllActive();
        if (active.isEmpty()) return;

        LocalDateTime now         = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(WINDOW_MINUTES);
        LocalDateTime timeoutMark = now.minusHours(TIMEOUT_HOURS);

        for (Alert alert : active) {

            // Timeout: lleva más de 6 horas sin resolverse
            if (alert.getTriggeredAt().isBefore(timeoutMark)) {
                alert.resolve(ResolutionReason.TIMEOUT);
                alertRepository.save(alert);
                continue;
            }

            // Normalización: promedio reciente de la zona por debajo del umbral
            List<SensorReading> recent =
                    readingRepository.findByZoneIdAfter(alert.getZoneId(), windowStart);

            if (recent.isEmpty()) continue; // sin datos recientes — no resolver aún

            OptionalDouble avg = recent.stream()
                    .mapToDouble(SensorReading::getCo2Level)
                    .average();

            if (avg.isPresent() && avg.getAsDouble() < RESOLUTION_THRESHOLD) {
                alert.resolve(ResolutionReason.NORMALIZED);
                alertRepository.save(alert);
            }
        }
    }
}
