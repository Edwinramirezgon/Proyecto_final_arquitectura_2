package com.demo.alert.infrastructure.adapter.in;

import com.demo.alert.application.port.in.NotificationQueryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;

    public NotificationController(NotificationQueryUseCase notificationQueryUseCase) {
        this.notificationQueryUseCase = notificationQueryUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> findAll() {
        List<Map<String, Object>> result = notificationQueryUseCase.findAll()
                .stream()
                .map(n -> Map.<String, Object>of(
                        "id",              n.getId(),
                        "alertId",         n.getAlertId(),
                        "zoneId",          n.getZoneId(),
                        "level",           n.getLevel(),
                        "averageCo2",      n.getAverageCo2(),
                        "nearestZoneName", n.getNearestZoneName() != null ? n.getNearestZoneName() : "",
                        "triggeredAt",     n.getTriggeredAt().toString(),
                        "status",          n.getStatus()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
