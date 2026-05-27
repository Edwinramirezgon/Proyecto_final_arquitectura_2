package com.demo.alert.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlertNotificationTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void create_notificacionValida_retornaInstanciaEnPending() {
        AlertNotification n = AlertNotification.create(
                1L, "ZONA-1", 160.0, "HIGH", null, 4.71, -74.07, NOW);
        assertEquals("ZONA-1", n.getZoneId());
        assertEquals("HIGH",   n.getLevel());
        assertEquals("PENDING", n.getStatus());
        assertFalse(n.isCritical());
        assertTrue(n.isHigh());
    }

    @Test
    void create_nivelCritical_isCriticalRetornaTrue() {
        AlertNotification n = AlertNotification.create(
                1L, "ZONA-1", 180.0, "CRITICAL", "Hospital", 4.71, -74.07, NOW);
        assertTrue(n.isCritical());
        assertFalse(n.isHigh());
    }

    @Test
    void create_zoneIdNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> AlertNotification.create(1L, null, 160.0, "HIGH", null, 4.71, -74.07, NOW));
    }

    @Test
    void create_levelNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> AlertNotification.create(1L, "ZONA-1", 160.0, null, null, 4.71, -74.07, NOW));
    }
}

class MaintenanceEventTest {

    @Test
    void create_eventoValido_retornaInstanciaEnPending() {
        MaintenanceEvent e = MaintenanceEvent.create(
                "S-01", "ZONA-1", 100.0, 350.0, LocalDateTime.now());
        assertEquals("S-01",    e.getSensorId());
        assertEquals("ZONA-1",  e.getZoneId());
        assertEquals("PENDING", e.getStatus());
        assertEquals(250.0,     e.jumpMagnitude(), 0.001);
    }

    @Test
    void jumpMagnitude_calculaValorAbsoluto() {
        MaintenanceEvent e = MaintenanceEvent.create(
                "S-01", "ZONA-1", 350.0, 100.0, LocalDateTime.now());
        assertEquals(250.0, e.jumpMagnitude(), 0.001); // caída también es 250
    }

    @Test
    void create_sensorIdNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> MaintenanceEvent.create(null, "ZONA-1", 100.0, 350.0, LocalDateTime.now()));
    }

    @Test
    void create_zoneIdVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> MaintenanceEvent.create("S-01", "  ", 100.0, 350.0, LocalDateTime.now()));
    }
}
