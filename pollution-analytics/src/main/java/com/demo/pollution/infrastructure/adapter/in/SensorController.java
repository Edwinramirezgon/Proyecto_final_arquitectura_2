package com.demo.pollution.infrastructure.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.demo.pollution.application.port.in.ProcessReadingUseCase;
import com.demo.pollution.application.port.in.SensorQueryUseCase;
import com.demo.pollution.domain.exception.InconsistentReadingException;
import com.demo.pollution.infrastructure.adapter.in.dto.SensorReadingRequest;
import com.demo.pollution.infrastructure.adapter.in.mapper.PollutionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sensors")
@CrossOrigin(origins = "*")
@Tag(name = "Sensores", description = "Ingesta y consulta de lecturas de sensores de CO2")
public class SensorController {

    private final ProcessReadingUseCase processReadingUseCase;
    private final SensorQueryUseCase    sensorQueryUseCase;

    public SensorController(ProcessReadingUseCase processReadingUseCase,
                             SensorQueryUseCase sensorQueryUseCase) {
        this.processReadingUseCase = processReadingUseCase;
        this.sensorQueryUseCase    = sensorQueryUseCase;
    }

    @Operation(summary = "Ingestar lectura de sensor",
               description = "Procesa una lectura de CO2. Aplica las 3 reglas de negocio: consistencia, umbral de emergencia y consulta a ZoneService.")
    @PostMapping("/readings")
    public ResponseEntity<?> ingest(@RequestBody SensorReadingRequest request) {
        try {
            processReadingUseCase.process(PollutionMapper.toDomain(request));
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("mensaje", "Lectura procesada correctamente."));
        } catch (InconsistentReadingException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Consultar lecturas recientes de un sensor",
               description = "Devuelve las lecturas de un sensor en los últimos N minutos (default 60).")
    @GetMapping("/{sensorId}/readings")
    public ResponseEntity<List<Map<String, Object>>> getReadings(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "60") int minutes) {
        List<Map<String, Object>> result = sensorQueryUseCase
                .findRecentBySensor(sensorId, minutes)
                .stream()
                .sorted((a, b) -> a.getRecordedAt().compareTo(b.getRecordedAt()))
                .map(r -> Map.<String, Object>of(
                        "sensorId",   r.getSensorId(),
                        "zoneId",     r.getZoneId(),
                        "co2Level",   r.getCo2Level(),
                        "recordedAt", r.getRecordedAt().toString()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
