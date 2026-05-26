package com.demo.pollution.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    @Test
    void create_alertaValida_retornaInstanciaActiva() {
        Alert a = Alert.create("ZONA-1", 160.0, AlertLevel.HIGH, null, 4.71, -74.07);
        assertEquals("ZONA-1",      a.getZoneId());
        assertEquals(160.0,         a.getAverageCo2());
        assertEquals(AlertLevel.HIGH, a.getLevel());
        assertTrue(a.isActive());
        assertNotNull(a.getTriggeredAt());
        assertNull(a.getResolvedAt());
    }

    @Test
    void create_zoneIdNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> Alert.create(null, 160.0, AlertLevel.HIGH, null, 4.71, -74.07));
    }

    @Test
    void create_zoneIdVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> Alert.create("  ", 160.0, AlertLevel.HIGH, null, 4.71, -74.07));
    }

    @Test
    void resolve_marcaAlertaComoInactiva() {
        Alert a = Alert.create("ZONA-1", 160.0, AlertLevel.HIGH, null, 4.71, -74.07);
        a.resolve(ResolutionReason.NORMALIZED);
        assertFalse(a.isActive());
        assertEquals(ResolutionReason.NORMALIZED, a.getResolvedReason());
        assertNotNull(a.getResolvedAt());
    }

    @Test
    void create_alertaCriticalConZonaSensible_guardaNombreZona() {
        Alert a = Alert.create("ZONA-1", 180.0, AlertLevel.CRITICAL, "Hospital San Vicente", 4.71, -74.07);
        assertEquals(AlertLevel.CRITICAL,       a.getLevel());
        assertEquals("Hospital San Vicente",    a.getNearestZoneName());
    }
}
