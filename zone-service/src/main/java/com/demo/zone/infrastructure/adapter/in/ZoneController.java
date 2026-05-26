package com.demo.zone.infrastructure.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.demo.zone.application.port.in.ZoneUseCase;
import com.demo.zone.domain.exception.ZoneNotFoundException;
import com.demo.zone.infrastructure.adapter.in.dto.ZoneRequest;
import com.demo.zone.infrastructure.adapter.in.dto.ZoneResponse;
import com.demo.zone.infrastructure.adapter.in.mapper.ZoneMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/zones")
@CrossOrigin(origins = "*")
@Tag(name = "Zonas Sensibles", description = "CRUD de zonas sensibles y consulta de proximidad geográfica (Haversine)")
public class ZoneController {

    private final ZoneUseCase zoneUseCase;

    public ZoneController(ZoneUseCase zoneUseCase) {
        this.zoneUseCase = zoneUseCase;
    }

    @Operation(summary = "Crear zona sensible")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ZoneRequest request) {
        try {
            ZoneResponse response = ZoneMapper.toResponse(zoneUseCase.create(ZoneMapper.toDomain(request)));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Listar todas las zonas sensibles")
    @GetMapping
    public ResponseEntity<List<ZoneResponse>> findAll() {
        List<ZoneResponse> response = zoneUseCase.findAll().stream()
                .map(ZoneMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ZoneMapper.toResponse(zoneUseCase.findById(id)));
        } catch (ZoneNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ZoneRequest request) {
        try {
            ZoneResponse response = ZoneMapper.toResponse(
                    zoneUseCase.update(id, ZoneMapper.toDomain(request)));
            return ResponseEntity.ok(response);
        } catch (ZoneNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            zoneUseCase.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ZoneNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Zona más cercana a un punto",
               description = "Consumido síncronamente por MS-PollutionAnalytics para elevar prioridad de alerta. Usa fórmula Haversine.")
    @GetMapping("/nearest")
    public ResponseEntity<?> findNearest(@RequestParam double lat,
                                         @RequestParam double lon,
                                         @RequestParam(defaultValue = "2.0") double radiusKm) {
        return zoneUseCase.findNearest(lat, lon, radiusKm)
                .map(z -> ResponseEntity.ok(ZoneMapper.toResponse(z)))
                .orElse(ResponseEntity.noContent().build());
    }
}
