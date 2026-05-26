package com.demo.pollution.infrastructure.adapter.in;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/zones")
@CrossOrigin(origins = "*")
public class ZoneProxyController {

    private static final String CB_ZONE = "zone-service";

    private final RestTemplate restTemplate;
    private final String       zoneServiceUrl;

    public ZoneProxyController(RestTemplate restTemplate,
                               @Value("${zone.service.url}") String zoneServiceUrl) {
        this.restTemplate   = restTemplate;
        this.zoneServiceUrl = zoneServiceUrl;
    }

    @CircuitBreaker(name = CB_ZONE, fallbackMethod = "findAllFallback")
    @GetMapping
    public ResponseEntity<?> findAll() {
        return forward(HttpMethod.GET, "/zones", null);
    }

    @CircuitBreaker(name = CB_ZONE, fallbackMethod = "findByIdFallback")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return forward(HttpMethod.GET, "/zones/" + id, null);
    }

    @CircuitBreaker(name = CB_ZONE, fallbackMethod = "mutateFallback")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return forward(HttpMethod.POST, "/zones", body);
    }

    @CircuitBreaker(name = CB_ZONE, fallbackMethod = "updateFallback")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return forward(HttpMethod.PUT, "/zones/" + id, body);
    }

    @CircuitBreaker(name = CB_ZONE, fallbackMethod = "deleteFallback")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return forward(HttpMethod.DELETE, "/zones/" + id, null);
    }

    @CircuitBreaker(name = CB_ZONE, fallbackMethod = "nearestFallback")
    @GetMapping("/nearest")
    public ResponseEntity<?> nearest(@RequestParam double lat,
                                     @RequestParam double lon,
                                     @RequestParam(defaultValue = "2.0") double radiusKm) {
        return forward(HttpMethod.GET,
                String.format("/zones/nearest?lat=%s&lon=%s&radiusKm=%s", lat, lon, radiusKm), null);
    }

    // ── fallbacks con mensajes claros ─────────────────────────────────────────

    public ResponseEntity<?> findAllFallback(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(List.of());
    }

    public ResponseEntity<?> findByIdFallback(Long id, Exception e) {
        return unavailable("La información de la zona no está disponible en este momento.");
    }

    public ResponseEntity<?> mutateFallback(Map<String, Object> body, Exception e) {
        return unavailable("No es posible registrar la zona en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> updateFallback(Long id, Map<String, Object> body, Exception e) {
        return unavailable("No es posible actualizar la zona en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> deleteFallback(Long id, Exception e) {
        return unavailable("No es posible eliminar la zona en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> nearestFallback(double lat, double lon, double radiusKm, Exception e) {
        return ResponseEntity.noContent().build();
    }

    // ── privado ───────────────────────────────────────────────────────────────

    private ResponseEntity<?> forward(HttpMethod method, String path, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Objects.requireNonNull(
                    Collections.singletonList(MediaType.APPLICATION_JSON),
                    "accept media types must be provided"));
            ResponseEntity<Object> upstream = restTemplate.exchange(
                    zoneServiceUrl + path, Objects.requireNonNull(method, "http method must be provided"),
                    new HttpEntity<>(body, headers), Object.class);
            return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
        }
    }

    private ResponseEntity<?> unavailable(String mensaje) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", mensaje));
    }
}
