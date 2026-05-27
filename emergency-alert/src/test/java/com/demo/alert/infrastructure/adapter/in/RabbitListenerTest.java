package com.demo.alert.infrastructure.adapter.in;

import com.demo.alert.application.port.in.ProcessAlertUseCase;
import com.demo.alert.application.port.in.ProcessMaintenanceUseCase;
import com.demo.alert.domain.model.AlertNotification;
import com.demo.alert.domain.model.MaintenanceEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitListenerTest {

    @Mock ProcessAlertUseCase       processAlertUseCase;
    @Mock ProcessMaintenanceUseCase processMaintenanceUseCase;

    RabbitAlertListener       alertListener;
    RabbitMaintenanceListener maintenanceListener;
    ObjectMapper              objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        alertListener       = new RabbitAlertListener(processAlertUseCase, objectMapper, registry);
        maintenanceListener = new RabbitMaintenanceListener(processMaintenanceUseCase, objectMapper, registry);
    }

    // ── RabbitAlertListener ───────────────────────────────────────────────────

    @Test
    void alertListener_payloadValido_invocaCasoDeUso() {
        String payload = """
                {
                  "alertId": 1,
                  "zoneId": "ZONA-1",
                  "averageCo2": 165.0,
                  "level": "HIGH",
                  "latitude": 4.71,
                  "longitude": -74.07,
                  "triggeredAt": "2025-01-01T10:00:00"
                }
                """;

        alertListener.onAlert(payload);

        ArgumentCaptor<AlertNotification> captor = ArgumentCaptor.forClass(AlertNotification.class);
        verify(processAlertUseCase).process(captor.capture());
        assertEquals("ZONA-1", captor.getValue().getZoneId());
        assertEquals("HIGH",   captor.getValue().getLevel());
    }

    @Test
    void alertListener_payloadInvalido_lanzaRuntimeExceptionParaDlq() {
        String payloadRoto = "{ esto no es json }";

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> alertListener.onAlert(payloadRoto));
        assertTrue(ex.getMessage().contains("Error procesando alerta"));
        verify(processAlertUseCase, never()).process(any());
    }

    @Test
    void alertListener_casoDeUsoLanzaExcepcion_relanzaParaDlq() {
        String payload = """
                {
                  "alertId": 1,
                  "zoneId": "ZONA-1",
                  "averageCo2": 165.0,
                  "level": "HIGH",
                  "latitude": 4.71,
                  "longitude": -74.07,
                  "triggeredAt": "2025-01-01T10:00:00"
                }
                """;
        doThrow(new RuntimeException("fallo de email")).when(processAlertUseCase).process(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> alertListener.onAlert(payload));
        assertTrue(ex.getMessage().contains("Error procesando alerta"));
    }

    // ── RabbitMaintenanceListener ─────────────────────────────────────────────

    @Test
    void maintenanceListener_payloadValido_invocaCasoDeUso() {
        String payload = """
                {
                  "sensorId": "S-01",
                  "zoneId": "ZONA-1",
                  "previousCo2": 100.0,
                  "currentCo2": 350.0,
                  "detectedAt": "2025-01-01T10:00:00"
                }
                """;

        maintenanceListener.onMaintenance(payload);

        ArgumentCaptor<MaintenanceEvent> captor = ArgumentCaptor.forClass(MaintenanceEvent.class);
        verify(processMaintenanceUseCase).process(captor.capture());
        assertEquals("S-01",   captor.getValue().getSensorId());
        assertEquals("ZONA-1", captor.getValue().getZoneId());
        assertEquals(250.0,    captor.getValue().jumpMagnitude(), 0.001);
    }

    @Test
    void maintenanceListener_payloadInvalido_lanzaRuntimeExceptionParaDlq() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> maintenanceListener.onMaintenance("no-json"));
        assertTrue(ex.getMessage().contains("Error procesando mantenimiento"));
        verify(processMaintenanceUseCase, never()).process(any());
    }
}
