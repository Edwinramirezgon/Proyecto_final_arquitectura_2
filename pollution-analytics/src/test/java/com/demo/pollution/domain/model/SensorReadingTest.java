package com.demo.pollution.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorReadingTest {

    // ── Coordenadas válidas (Bogotá) ──────────────────────────────────────────
    private static final double LAT = 4.71;
    private static final double LON = -74.07;

    @Test
    void create_lecturaValida_retornaInstancia() {
        SensorReading r = SensorReading.create("S-01", "ZONA-1", LAT, LON, 120.0);
        assertEquals("S-01",   r.getSensorId());
        assertEquals("ZONA-1", r.getZoneId());
        assertEquals(120.0,    r.getCo2Level());
        assertNotNull(r.getRecordedAt());
    }

    @Test
    void create_sensorIdNulo_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SensorReading.create(null, "ZONA-1", LAT, LON, 100.0));
        assertTrue(ex.getMessage().contains("sensor"));
    }

    @Test
    void create_sensorIdVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> SensorReading.create("  ", "ZONA-1", LAT, LON, 100.0));
    }

    @Test
    void create_zoneIdNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> SensorReading.create("S-01", null, LAT, LON, 100.0));
    }

    @Test
    void create_co2Negativo_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SensorReading.create("S-01", "ZONA-1", LAT, LON, -1.0));
        assertTrue(ex.getMessage().contains("negativo"));
    }

    @Test
    void create_coordenadasFueraDeRango_lanzaExcepcion() {
        // latitud fuera de Colombia
        assertThrows(IllegalArgumentException.class,
                () -> SensorReading.create("S-01", "ZONA-1", 50.0, LON, 100.0));
    }

    // ── Regla 1: isInconsistentWith ───────────────────────────────────────────

    @Test
    void isInconsistentWith_saltoMayorA200_retornaTrue() {
        SensorReading r = SensorReading.create("S-01", "ZONA-1", LAT, LON, 350.0);
        assertTrue(r.isInconsistentWith(100.0)); // salto = 250
    }

    @Test
    void isInconsistentWith_saltoExacto200_retornaFalse() {
        SensorReading r = SensorReading.create("S-01", "ZONA-1", LAT, LON, 300.0);
        assertFalse(r.isInconsistentWith(100.0)); // salto = 200, no supera
    }

    @Test
    void isInconsistentWith_saltoMenorA200_retornaFalse() {
        SensorReading r = SensorReading.create("S-01", "ZONA-1", LAT, LON, 150.0);
        assertFalse(r.isInconsistentWith(100.0)); // salto = 50
    }

    @Test
    void isInconsistentWith_caida_tambienDetectaInconsistencia() {
        SensorReading r = SensorReading.create("S-01", "ZONA-1", LAT, LON, 50.0);
        assertTrue(r.isInconsistentWith(300.0)); // salto = 250 (caída)
    }
}
