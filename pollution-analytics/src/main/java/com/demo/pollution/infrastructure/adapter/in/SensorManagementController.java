package com.demo.pollution.infrastructure.adapter.in;

import com.demo.pollution.application.port.in.SensorManagementUseCase;
import com.demo.pollution.domain.exception.SensorNotFoundException;
import com.demo.pollution.infrastructure.adapter.in.dto.SensorRequest;
import com.demo.pollution.infrastructure.adapter.in.dto.SensorResponse;
import com.demo.pollution.infrastructure.adapter.in.mapper.SensorMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sensors/management")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Sensores", description = "CRUD de sensores de CO2")
public class SensorManagementController {

    private final SensorManagementUseCase sensorManagementUseCase;

    public SensorManagementController(SensorManagementUseCase sensorManagementUseCase) {
        this.sensorManagementUseCase = sensorManagementUseCase;
    }

    @Operation(summary = "Registrar nuevo sensor")
    @PostMapping
    public ResponseEntity<?> registerSensor(@RequestBody SensorRequest request) {
        try {
            var sensor = sensorManagementUseCase.registerSensor(
                request.getSensorId(),
                request.getName(),
                request.getZoneId(),
                request.getLatitude(),
                request.getLongitude()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SensorMapper.toResponse(sensor));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar sensor existente")
    @PutMapping("/{sensorId}")
    public ResponseEntity<?> updateSensor(@PathVariable String sensorId,
                                          @RequestBody SensorRequest request) {
        try {
            var sensor = sensorManagementUseCase.updateSensor(
                sensorId,
                request.getName(),
                request.getZoneId(),
                request.getLatitude(),
                request.getLongitude()
            );
            return ResponseEntity.ok(SensorMapper.toResponse(sensor));
        } catch (SensorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Desactivar sensor")
    @PostMapping("/{sensorId}/deactivate")
    public ResponseEntity<?> deactivateSensor(@PathVariable String sensorId) {
        try {
            sensorManagementUseCase.deactivateSensor(sensorId);
            return ResponseEntity.ok(Map.of("mensaje", "Sensor desactivado correctamente"));
        } catch (SensorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Activar sensor")
    @PostMapping("/{sensorId}/activate")
    public ResponseEntity<?> activateSensor(@PathVariable String sensorId) {
        try {
            sensorManagementUseCase.activateSensor(sensorId);
            return ResponseEntity.ok(Map.of("mensaje", "Sensor activado correctamente"));
        } catch (SensorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar sensor")
    @DeleteMapping("/{sensorId}")
    public ResponseEntity<?> deleteSensor(@PathVariable String sensorId) {
        try {
            sensorManagementUseCase.deleteSensor(sensorId);
            return ResponseEntity.noContent().build();
        } catch (SensorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Obtener sensor por ID")
    @GetMapping("/{sensorId}")
    public ResponseEntity<?> getSensor(@PathVariable String sensorId) {
        try {
            var sensor = sensorManagementUseCase.getSensor(sensorId);
            return ResponseEntity.ok(SensorMapper.toResponse(sensor));
        } catch (SensorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Listar todos los sensores")
    @GetMapping
    public ResponseEntity<List<SensorResponse>> getAllSensors() {
        var sensors = sensorManagementUseCase.getAllSensors();
        return ResponseEntity.ok(sensors.stream()
                .map(SensorMapper::toResponse)
                .toList());
    }

    @Operation(summary = "Listar sensores por zona")
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<SensorResponse>> getSensorsByZone(@PathVariable String zoneId) {
        var sensors = sensorManagementUseCase.getSensorsByZone(zoneId);
        return ResponseEntity.ok(sensors.stream()
                .map(SensorMapper::toResponse)
                .toList());
    }

    @Operation(summary = "Listar sensores activos")
    @GetMapping("/active")
    public ResponseEntity<List<SensorResponse>> getActiveSensors() {
        var sensors = sensorManagementUseCase.getActiveSensors();
        return ResponseEntity.ok(sensors.stream()
                .map(SensorMapper::toResponse)
                .toList());
    }
}
