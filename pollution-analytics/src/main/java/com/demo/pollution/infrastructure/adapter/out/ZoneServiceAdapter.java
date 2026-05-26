package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.ZoneClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@Component
public class ZoneServiceAdapter implements ZoneClientPort {

    private final RestTemplate restTemplate;
    private final String       zoneServiceUrl;
    private final Tracer       tracer;

    public ZoneServiceAdapter(RestTemplate restTemplate,
                              @Value("${zone.service.url}") String zoneServiceUrl,
                              Tracer tracer) {
        this.restTemplate   = Objects.requireNonNull(restTemplate);
        this.zoneServiceUrl = Objects.requireNonNull(zoneServiceUrl);
        this.tracer         = tracer;
    }

    @CircuitBreaker(name = "zone-service", fallbackMethod = "findNearestFallback")
    @Override
    public Optional<NearestZone> findNearest(double latitude, double longitude, double radiusKm) {
        String url = String.format("%s/zones/nearest?lat=%s&lon=%s&radiusKm=%s",
                zoneServiceUrl, latitude, longitude, radiusKm);
        Map<?, ?> response = restTemplate.getForObject(
            Objects.requireNonNull(url), Map.class);

        // ── Span manual: confirmación de respuesta de zone-service ───────────
        boolean encontrada = response != null;
        Span span = tracer.nextSpan()
                .name("zone-service.respuesta")
                .tag("zona.encontrada", String.valueOf(encontrada))
                .tag("lat", String.valueOf(latitude))
                .tag("lon", String.valueOf(longitude))
                .start();
        span.end();

        if (!encontrada) return Optional.empty();
        String name = (String) response.get("name");
        int priority = ((Number) Objects.requireNonNull(response.get("priority"))).intValue();
        return Optional.of(new NearestZone(name, priority));
    }

    /**
     * Fallback: zone-service no disponible.
     * La alerta se genera igual pero como HIGH (sin zona sensible).
     * El sistema de alertas NO se detiene por una dependencia caída.
     */
    public Optional<NearestZone> findNearestFallback(double latitude, double longitude,
                                                      double radiusKm, Exception e) {
        return Optional.empty();
    }
}
