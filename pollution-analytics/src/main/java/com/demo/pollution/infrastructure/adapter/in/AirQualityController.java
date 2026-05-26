package com.demo.pollution.infrastructure.adapter.in;

import com.demo.pollution.application.port.in.AirQualityQueryUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/air-quality")
@CrossOrigin(origins = "*")
public class AirQualityController {

    private final AirQualityQueryUseCase airQualityQueryUseCase;

    public AirQualityController(AirQualityQueryUseCase airQualityQueryUseCase) {
        this.airQualityQueryUseCase = airQualityQueryUseCase;
    }

    @GetMapping("/colombia")
    public ResponseEntity<?> getColombiaReadings() {
        try {
            var readings = airQualityQueryUseCase.getColombiaReadings();
            return ResponseEntity.ok(readings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "No se pudieron obtener datos de calidad del aire en este momento."));
        }
    }
}
