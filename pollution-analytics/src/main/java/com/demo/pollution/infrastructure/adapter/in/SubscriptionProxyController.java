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
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class SubscriptionProxyController {

    private static final String CB_AUTH = "auth-service";

    private final RestTemplate restTemplate;
    private final String       authServiceUrl;

    public SubscriptionProxyController(RestTemplate restTemplate,
                                       @Value("${auth.service.url}") String authServiceUrl) {
        this.restTemplate   = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "subscribeFallback")
    @PostMapping("/{username}/subscriptions")
    public ResponseEntity<?> subscribe(@PathVariable String username,
                                       @RequestBody Map<String, String> body) {
        return forward(HttpMethod.POST, "/users/" + username + "/subscriptions", body);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "unsubscribeFallback")
    @DeleteMapping("/{username}/subscriptions/{zoneId}")
    public ResponseEntity<?> unsubscribe(@PathVariable String username,
                                         @PathVariable String zoneId) {
        return forward(HttpMethod.DELETE, "/users/" + username + "/subscriptions/" + zoneId, null);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "listFallback")
    @GetMapping("/{username}/subscriptions")
    public ResponseEntity<?> findByUsername(@PathVariable String username) {
        return forward(HttpMethod.GET, "/users/" + username + "/subscriptions", null);
    }

    // ── fallbacks ─────────────────────────────────────────────────────────────

    public ResponseEntity<?> subscribeFallback(String username, Map<String, String> body, Exception e) {
        return unavailable("No es posible suscribirse en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> unsubscribeFallback(String username, String zoneId, Exception e) {
        return unavailable("No es posible desuscribirse en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> listFallback(String username, Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(List.of());
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
                    authServiceUrl + path, Objects.requireNonNull(method, "http method must be provided"),
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
