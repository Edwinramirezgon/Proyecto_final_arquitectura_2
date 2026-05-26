package com.demo.pollution.infrastructure.adapter.in;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthProxyController {

    private static final Logger log    = LoggerFactory.getLogger(AuthProxyController.class);
    private static final String CB_AUTH = "auth-service";

    private final RestTemplate restTemplate;
    private final String       authServiceUrl;
    private final Tracer       tracer;

    public AuthProxyController(RestTemplate restTemplate,
                               @Value("${auth.service.url}") String authServiceUrl,
                               Tracer tracer) {
        this.restTemplate   = restTemplate;
        this.authServiceUrl = authServiceUrl;
        this.tracer         = tracer;
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "loginFallback")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        reqHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        try {
            ResponseEntity<Object> upstream = restTemplate.exchange(
                    authServiceUrl + "/auth/login", HttpMethod.POST,
                    new HttpEntity<>(body, reqHeaders), Object.class);
            log.info("Proxy auth-service path=/auth/login status={}", upstream.getStatusCode().value());
            return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
        } catch (HttpClientErrorException e) {
            log.warn("Proxy auth-service error path=/auth/login status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
        }
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "registerFallback")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        return forward(HttpMethod.POST, "/auth/register", body, null);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "refreshFallback")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        return forward(HttpMethod.POST, "/auth/refresh", body, null);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "logoutFallback")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        return forward(HttpMethod.POST, "/auth/logout", null, authHeader);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "validateFallback")
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        return forward(HttpMethod.GET, "/auth/validate", null, authHeader);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "meFallback")
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        return forward(HttpMethod.GET, "/auth/me", null, authHeader);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "changePasswordFallback")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody Map<String, String> body) {
        return forward(HttpMethod.POST, "/auth/change-password", body, authHeader);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "genericFallback")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        return forward(HttpMethod.POST, "/auth/forgot-password", body, null);
    }

    @CircuitBreaker(name = CB_AUTH, fallbackMethod = "genericFallback")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        return forward(HttpMethod.POST, "/auth/reset-password", body, null);
    }

    // ── fallbacks con mensajes claros ─────────────────────────────────────────

    public ResponseEntity<?> loginFallback(Map<String, String> body, Exception e) {
        log.error("Circuit Breaker ABIERTO para login - auth-service no disponible: {}", e.getMessage());
        return unavailable("No es posible iniciar sesión en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> registerFallback(Map<String, String> body, Exception e) {
        log.error("Circuit Breaker ABIERTO para register - auth-service no disponible: {}", e.getMessage());
        return unavailable("No es posible crear la cuenta en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> refreshFallback(Map<String, String> body, Exception e) {
        log.error("Circuit Breaker ABIERTO para refresh - auth-service no disponible: {}", e.getMessage());
        return unavailable("Su sesión no pudo renovarse. Por favor inicie sesión nuevamente.");
    }

    public ResponseEntity<?> logoutFallback(String authHeader, Exception e) {
        log.warn("Circuit Breaker ABIERTO para logout - auth-service no disponible: {}", e.getMessage());
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada localmente."));
    }

    public ResponseEntity<?> validateFallback(String authHeader, Exception e) {
        log.error("Circuit Breaker ABIERTO para validate - auth-service no disponible: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "No se puede verificar su sesión en este momento."));
    }

    public ResponseEntity<?> meFallback(String authHeader, Exception e) {
        log.error("Circuit Breaker ABIERTO para me - auth-service no disponible: {}", e.getMessage());
        return unavailable("No se puede obtener la información del usuario en este momento.");
    }

    public ResponseEntity<?> changePasswordFallback(String authHeader, Map<String, String> body, Exception e) {
        log.error("Circuit Breaker ABIERTO para changePassword - auth-service no disponible: {}", e.getMessage());
        return unavailable("No es posible cambiar la contraseña en este momento. Por favor intente en unos minutos.");
    }

    public ResponseEntity<?> genericFallback(Map<String, String> body, Exception e) {
        log.error("Circuit Breaker ABIERTO - auth-service no disponible: {}", e.getMessage());
        return unavailable("Servicio no disponible en este momento. Por favor intente en unos minutos.");
    }

    // ── privado ───────────────────────────────────────────────────────────────

    private ResponseEntity<?> forward(HttpMethod method, String path,
                                      Object body, String authHeader) {
        try {
            HttpHeaders reqHeaders = new HttpHeaders();
            reqHeaders.setContentType(MediaType.APPLICATION_JSON);
            reqHeaders.setAccept(Objects.requireNonNull(
                    Collections.singletonList(MediaType.APPLICATION_JSON),
                    "accept media types must be provided"));
            if (authHeader != null) reqHeaders.set("Authorization", authHeader);
            ResponseEntity<Object> upstream = restTemplate.exchange(
                    authServiceUrl + path, Objects.requireNonNull(method, "http method must be provided"),
                    new HttpEntity<>(body, reqHeaders), Object.class);

            log.info("Proxy auth-service path={} status={}", path, upstream.getStatusCode().value());

            // ── Span manual: confirmación de respuesta de auth-service ────────
            Span span = tracer.nextSpan()
                    .name("auth-service.respuesta")
                    .tag("http.status", String.valueOf(upstream.getStatusCode().value()))
                    .tag("http.path", path)
                    .tag("resultado", upstream.getStatusCode().is2xxSuccessful() ? "ok" : "error")
                    .start();
            span.end();

            return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
        } catch (HttpClientErrorException e) {
            log.warn("Proxy auth-service error path={} status={} body={}", path, e.getStatusCode().value(), e.getResponseBodyAsString());
            Span span = tracer.nextSpan()
                    .name("auth-service.respuesta")
                    .tag("http.status", String.valueOf(e.getStatusCode().value()))
                    .tag("http.path", path)
                    .tag("resultado", "error-cliente")
                    .start();
            span.end();
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
        } catch (Exception e) {
            log.error("Proxy auth-service fallo de conexión path={} error={}", path, e.getMessage());
            Span span = tracer.nextSpan()
                    .name("auth-service.respuesta")
                    .tag("http.path", path)
                    .tag("resultado", "error-conexion")
                    .tag("error.message", e.getMessage())
                    .start();
            span.end();
            throw new RuntimeException("Error de conexión con auth-service: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<?> unavailable(String mensaje) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", mensaje));
    }
}
