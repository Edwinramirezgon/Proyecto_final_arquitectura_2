package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.out.*;
import com.demo.pollution.domain.exception.InconsistentReadingException;
import com.demo.pollution.domain.model.Alert;
import com.demo.pollution.domain.model.AlertLevel;
import com.demo.pollution.domain.model.SensorReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

    @Mock SensorReadingRepository  readingRepository;
    @Mock AlertRepository          alertRepository;
    @Mock AlertPublisherPort       alertPublisher;
    @Mock MaintenancePublisherPort maintenancePublisher;
    @Mock ZoneClientPort           zoneClient;
    @Mock SensorRepository         sensorRepository;

    SensorReadingService service;

    // Coordenadas válidas (Bogotá)
    private static final double LAT = 4.71;
    private static final double LON = -74.07;

    @BeforeEach
    void setUp() {
        service = new SensorReadingService(
                readingRepository, alertRepository,
                alertPublisher, maintenancePublisher, zoneClient, sensorRepository);
    }

    // ── REGLA 1: Lectura inconsistente ────────────────────────────────────────

    @Test
    void regla1_lecturaInconsistente_publicaMantenimientoYLanzaExcepcion() {
        SensorReading anterior = SensorReading.create("S-01", "ZONA-1", LAT, LON, 100.0);
        SensorReading nueva    = SensorReading.create("S-01", "ZONA-1", LAT, LON, 350.0); // salto 250

        // Mock sensor activo
        com.demo.pollution.domain.model.Sensor sensor = com.demo.pollution.domain.model.Sensor.create(
                "S-01", "Sensor Test", "ZONA-1", LAT, LON);
        when(sensorRepository.findBySensorId("S-01")).thenReturn(Optional.of(sensor));
        when(readingRepository.findLatestBySensorId("S-01")).thenReturn(Optional.of(anterior));

        assertThrows(InconsistentReadingException.class, () -> service.process(nueva));

        verify(maintenancePublisher).publish(nueva, 100.0);
        verify(readingRepository, never()).save(any());
        verify(alertPublisher,    never()).publish(any());
    }

    @Test
    void regla1_lecturaConsistente_persisteYNoPubicaMantenimiento() {
        SensorReading anterior = SensorReading.create("S-01", "ZONA-1", LAT, LON, 100.0);
        SensorReading nueva    = SensorReading.create("S-01", "ZONA-1", LAT, LON, 130.0); // salto 30

        // Mock sensor activo
        com.demo.pollution.domain.model.Sensor sensor = com.demo.pollution.domain.model.Sensor.create(
                "S-01", "Sensor Test", "ZONA-1", LAT, LON);
        when(sensorRepository.findBySensorId("S-01")).thenReturn(Optional.of(sensor));
        when(sensorRepository.save(any())).thenReturn(sensor);
        when(readingRepository.findLatestBySensorId("S-01")).thenReturn(Optional.of(anterior));
        when(readingRepository.save(nueva)).thenReturn(nueva);
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(List.of(nueva));

        service.process(nueva);

        verify(readingRepository).save(nueva);
        verify(maintenancePublisher, never()).publish(any(), anyDouble());
    }

    @Test
    void regla1_sinLecturaAnterior_procesaNormalmente() {
        SensorReading nueva = SensorReading.create("S-NUEVO", "ZONA-1", LAT, LON, 120.0);

        // Mock sensor activo
        com.demo.pollution.domain.model.Sensor sensor = com.demo.pollution.domain.model.Sensor.create(
                "S-NUEVO", "Sensor Nuevo", "ZONA-1", LAT, LON);
        when(sensorRepository.findBySensorId("S-NUEVO")).thenReturn(Optional.of(sensor));
        when(sensorRepository.save(any())).thenReturn(sensor);
        when(readingRepository.findLatestBySensorId("S-NUEVO")).thenReturn(Optional.empty());
        when(readingRepository.save(nueva)).thenReturn(nueva);
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(List.of(nueva));

        assertDoesNotThrow(() -> service.process(nueva));
        verify(readingRepository).save(nueva);
    }

    // ── REGLA 2: Umbral de emergencia ─────────────────────────────────────────

    @Test
    void regla2_menosDe3Sensores_noGeneraAlerta() {
        SensorReading r1 = leerCon("S-01", "ZONA-1", 200.0);
        SensorReading r2 = leerCon("S-02", "ZONA-1", 200.0);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(List.of(r1, r2));

        service.process(r2);

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void regla2_tresOmasSensoresPromedioMenorA100_noGeneraAlerta() {
        List<SensorReading> lecturas = List.of(
                leerCon("S-01", "ZONA-1", 80.0),
                leerCon("S-02", "ZONA-1", 90.0),
                leerCon("S-03", "ZONA-1", 85.0)
        );
        SensorReading nueva = lecturas.get(2);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(lecturas);

        service.process(nueva);

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void regla2_tresOmasSensoresPromedioEntre100y149_generaAlertaMedium() {
        List<SensorReading> lecturas = List.of(
                leerCon("S-01", "ZONA-1", 110.0),
                leerCon("S-02", "ZONA-1", 120.0),
                leerCon("S-03", "ZONA-1", 115.0)
        );
        SensorReading nueva = lecturas.get(2);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(lecturas);
        when(zoneClient.findNearest(anyDouble(), anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.process(nueva);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertPublisher).publish(captor.capture());
        assertEquals(AlertLevel.MEDIUM, captor.getValue().getLevel());
    }

    @Test
    void regla2_tresOmasSensoresPromedioMayorA150_generaAlertaHighSinZona() {
        List<SensorReading> lecturas = List.of(
                leerCon("S-01", "ZONA-1", 160.0),
                leerCon("S-02", "ZONA-1", 170.0),
                leerCon("S-03", "ZONA-1", 165.0)
        );
        SensorReading nueva = lecturas.get(2);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(lecturas);
        when(zoneClient.findNearest(anyDouble(), anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.process(nueva);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertPublisher).publish(captor.capture());
        assertEquals(AlertLevel.HIGH, captor.getValue().getLevel());
    }

    // ── REGLA 3: Elevación de prioridad ──────────────────────────────────────

    @Test
    void regla3_zonaSensibleConPrioridad8_generaAlertaCritical() {
        List<SensorReading> lecturas = List.of(
                leerCon("S-01", "ZONA-1", 160.0),
                leerCon("S-02", "ZONA-1", 170.0),
                leerCon("S-03", "ZONA-1", 165.0)
        );
        SensorReading nueva = lecturas.get(2);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(lecturas);
        when(zoneClient.findNearest(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Optional.of(new ZoneClientPort.NearestZone("Hospital San Vicente", 9)));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.process(nueva);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertPublisher).publish(captor.capture());
        assertEquals(AlertLevel.CRITICAL,        captor.getValue().getLevel());
        assertEquals("Hospital San Vicente",     captor.getValue().getNearestZoneName());
    }

    @Test
    void regla3_zonaSensibleConPrioridad7_generaAlertaHighNoElevada() {
        List<SensorReading> lecturas = List.of(
                leerCon("S-01", "ZONA-1", 160.0),
                leerCon("S-02", "ZONA-1", 170.0),
                leerCon("S-03", "ZONA-1", 165.0)
        );
        SensorReading nueva = lecturas.get(2);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(lecturas);
        when(zoneClient.findNearest(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Optional.of(new ZoneClientPort.NearestZone("Parque Simón Bolívar", 7)));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.process(nueva);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertPublisher).publish(captor.capture());
        assertEquals(AlertLevel.HIGH, captor.getValue().getLevel());
    }

    @Test
    void regla3_zoneServiceCaido_fallbackGeneraAlertaHigh() {
        List<SensorReading> lecturas = List.of(
                leerCon("S-01", "ZONA-1", 160.0),
                leerCon("S-02", "ZONA-1", 170.0),
                leerCon("S-03", "ZONA-1", 165.0)
        );
        SensorReading nueva = lecturas.get(2);

        when(readingRepository.findLatestBySensorId(any())).thenReturn(Optional.empty());
        when(readingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(readingRepository.findByZoneIdAfter(eq("ZONA-1"), any())).thenReturn(lecturas);
        when(zoneClient.findNearest(anyDouble(), anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.process(nueva);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertPublisher).publish(captor.capture());
        assertEquals(AlertLevel.HIGH, captor.getValue().getLevel());
        assertNull(captor.getValue().getNearestZoneName());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private SensorReading leerCon(String sensorId, String zoneId, double co2) {
        // Mock sensor activo para cada lectura (lenient para evitar UnnecessaryStubbingException)
        com.demo.pollution.domain.model.Sensor sensor = com.demo.pollution.domain.model.Sensor.create(
                sensorId, "Sensor " + sensorId, zoneId, LAT, LON);
        lenient().when(sensorRepository.findBySensorId(sensorId)).thenReturn(Optional.of(sensor));
        lenient().when(sensorRepository.save(any())).thenReturn(sensor);
        
        SensorReading r = SensorReading.create(sensorId, zoneId, LAT, LON, co2);
        r.setRecordedAt(LocalDateTime.now());
        return r;
    }
}
