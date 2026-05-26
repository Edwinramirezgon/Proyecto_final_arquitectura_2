package com.demo.pollution.infrastructure.adapter.in;

import com.demo.pollution.application.port.in.AlertQueryUseCase;
import com.demo.pollution.infrastructure.adapter.in.dto.AlertResponse;
import com.demo.pollution.infrastructure.adapter.in.mapper.PollutionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertQueryUseCase alertQueryUseCase;

    public AlertController(AlertQueryUseCase alertQueryUseCase) {
        this.alertQueryUseCase = alertQueryUseCase;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> findAll() {
        return ResponseEntity.ok(
                alertQueryUseCase.findAll().stream().map(PollutionMapper::toResponse).toList());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertResponse>> findAllActive() {
        return ResponseEntity.ok(
                alertQueryUseCase.findAllActive().stream().map(PollutionMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(PollutionMapper.toResponse(alertQueryUseCase.findById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
