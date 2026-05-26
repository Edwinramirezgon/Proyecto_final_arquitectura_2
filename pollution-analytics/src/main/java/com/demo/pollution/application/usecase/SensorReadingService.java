package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.in.ProcessReadingUseCase;
import com.demo.pollution.application.port.out.*;
import com.demo.pollution.domain.exception.InconsistentReadingException;
import com.demo.pollution.domain.model.Alert;
import com.demo.pollution.domain.model.AlertLevel;
import com.demo.pollution.domain.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@Service
public class SensorReadingService implements ProcessReadingUseCase {

    private static final Logger log = LoggerFactory.getLogger(SensorReadingService.class);

    private static final double EMERGENCY_THRESHOLD  = 150.0;
    private static final double WARNING_THRESHOLD    = 100.0;
    private static final int    MIN_SENSORS_REQUIRED = 3;
    private static final int    WINDOW_MINUTES       = 5;
    private static final double ZONE_RADIUS_KM       = 2.0;

    private final SensorReadingRepository  readingRepository;
    private final AlertRepository          alertRepository;
    private final AlertPublisherPort       alertPublisher;
    private final MaintenancePublisherPort maintenancePublisher;
    private final ZoneClientPort           zoneClient;
    private final SensorRepository         sensorRepository;

    public SensorReadingService(SensorReadingRepository readingRepository,
                                AlertRepository alertRepository,
                                AlertPublisherPort alertPublisher,
                                MaintenancePublisherPort maintenancePublisher,
                                ZoneClientPort zoneClient,
                                SensorRepository sensorRepository) {
        this.readingRepository   = readingRepository;
        this.alertRepository     = alertRepository;
        this.alertPublisher      = alertPublisher;
        this.maintenancePublisher = maintenancePublisher;
        this.zoneClient          = zoneClient;
        this.sensorRepository    = sensorRepository;
    }

    @Override
    public void process(SensorReading reading) {

        // ── VALIDACIÓN: El sensor debe existir y estar activo ────────────────────
        var sensor = sensorRepository.findBySensorId(reading.getSensorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sensor no registrado: " + reading.getSensorId()));
        
        if (!sensor.isActive()) {
            log.warn("Lectura rechazada de sensor inactivo: {}", reading.getSensorId());
            throw new IllegalStateException(
                    "El sensor " + reading.getSensorId() + " está desactivado");
        }

        // ── REGLA 1: Validación de consistencia (síncrona) ────────────────────
        log.info("Procesando lectura sensor={} zona={} co2={}",
                reading.getSensorId(), reading.getZoneId(), reading.getCo2Level());
        Optional<SensorReading> latest = readingRepository.findLatestBySensorId(reading.getSensorId());
        if (latest.isPresent() && reading.isInconsistentWith(latest.get().getCo2Level())) {
            log.warn("Lectura inconsistente sensor={} anterior={} actual={}",
                    reading.getSensorId(), latest.get().getCo2Level(), reading.getCo2Level());
            maintenancePublisher.publish(reading, latest.get().getCo2Level());
            throw new InconsistentReadingException(
                    reading.getSensorId(),
                    latest.get().getCo2Level(),
                    reading.getCo2Level());
        }

        // lectura válida — persistir
        readingRepository.save(reading);

        // actualizar lastReadingAt del sensor
        sensor.updateLastReading();
        sensorRepository.save(sensor);

        // ── REGLA 2: Evaluación de emergencia con ≥3 sensores (síncrona) ──────
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        List<SensorReading> recentReadings = readingRepository
                .findByZoneIdAfter(reading.getZoneId(), windowStart);

        long distinctSensors = recentReadings.stream()
                .map(SensorReading::getSensorId)
                .distinct()
                .count();

        if (distinctSensors < MIN_SENSORS_REQUIRED) {
            log.info("Sensores insuficientes zona={} sensores={}/{}",
                    reading.getZoneId(), distinctSensors, MIN_SENSORS_REQUIRED);
            return;
        }

        OptionalDouble avg = recentReadings.stream()
                .mapToDouble(SensorReading::getCo2Level)
                .average();

        if (avg.isEmpty()) return;

        double average = avg.getAsDouble();

        if (average < WARNING_THRESHOLD) return;

        // ── REGLA 3: Consulta síncrona a ZoneService ──────────────────────────
        Optional<ZoneClientPort.NearestZone> nearestZone = zoneClient
                .findNearest(reading.getLatitude(), reading.getLongitude(), ZONE_RADIUS_KM);

        AlertLevel level = determineLevel(average, nearestZone);

        log.info("Alerta generada zona={} promedio={} nivel={} zonaSensible={}",
                reading.getZoneId(), String.format("%.1f", average), level,
                nearestZone.map(ZoneClientPort.NearestZone::name).orElse("ninguna"));

        String zoneName = nearestZone.map(ZoneClientPort.NearestZone::name).orElse(null);

        Alert alert = Alert.create(
                reading.getZoneId(), average, level,
                zoneName, reading.getLatitude(), reading.getLongitude());

        Alert saved = alertRepository.save(alert);

        // ── Publicación asíncrona a MS-EmergencyAlert ─────────────────────────
        alertPublisher.publish(saved);
    }

    private AlertLevel determineLevel(double average,
                                      Optional<ZoneClientPort.NearestZone> nearestZone) {
        if (average >= EMERGENCY_THRESHOLD) {
            // zona sensible cercana → CRITICAL
            if (nearestZone.isPresent() && nearestZone.get().priority() >= 8)
                return AlertLevel.CRITICAL;
            return AlertLevel.HIGH;
        }
        return AlertLevel.MEDIUM;
    }
}
