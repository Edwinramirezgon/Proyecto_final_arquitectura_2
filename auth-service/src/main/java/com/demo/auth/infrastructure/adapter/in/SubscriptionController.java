package com.demo.auth.infrastructure.adapter.in;

import com.demo.auth.application.port.in.SubscriptionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;

    public SubscriptionController(SubscriptionUseCase subscriptionUseCase) {
        this.subscriptionUseCase = subscriptionUseCase;
    }

    @PostMapping("/{username}/subscriptions")
    public ResponseEntity<?> subscribe(@PathVariable String username,
                                       @RequestBody Map<String, String> body) {
        try {
            var sub = subscriptionUseCase.subscribe(username, body.get("zoneId"));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "username",     sub.getUsername(),
                    "zoneId",       sub.getZoneId(),
                    "subscribedAt", sub.getSubscribedAt().toString()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{username}/subscriptions/{zoneId}")
    public ResponseEntity<?> unsubscribe(@PathVariable String username,
                                         @PathVariable String zoneId) {
        try {
            subscriptionUseCase.unsubscribe(username, zoneId);
            return ResponseEntity.ok(Map.of(
                    "mensaje", String.format("Te has desuscrito de la zona '%s' correctamente.", zoneId)
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{username}/subscriptions")
    public ResponseEntity<?> findByUsername(@PathVariable String username) {
        var subs = subscriptionUseCase.findByUsername(username).stream()
                .map(s -> Map.of(
                        "zoneId",       s.getZoneId(),
                        "subscribedAt", s.getSubscribedAt().toString()
                ))
                .toList();
        return ResponseEntity.ok(subs);
    }

    /** Usado internamente por emergency-alert para obtener emails de suscriptores de una zona. */
    @GetMapping("/subscriptions/zone/{zoneId}/emails")
    public ResponseEntity<?> getEmailsByZone(@PathVariable String zoneId) {
        return ResponseEntity.ok(subscriptionUseCase.findSubscriberEmailsByZone(zoneId));
    }
}
