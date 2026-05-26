package com.demo.pollution.infrastructure.adapter.in;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationsProxyController {

    private final RestTemplate restTemplate;
    private final String       emergencyAlertUrl;

    public NotificationsProxyController(RestTemplate restTemplate,
                                        @Value("${emergency.alert.url}") String emergencyAlertUrl) {
        this.restTemplate      = restTemplate;
        this.emergencyAlertUrl = emergencyAlertUrl;
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Objects.requireNonNull(
                Collections.singletonList(MediaType.APPLICATION_JSON),
                "accept media types must be provided"));
            ResponseEntity<Object> upstream = restTemplate.exchange(
                    emergencyAlertUrl + "/notifications",
                Objects.requireNonNull(HttpMethod.GET, "http method must be provided"),
                    new HttpEntity<>(headers),
                    Object.class);
            return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(List.of());
        }
    }
}
