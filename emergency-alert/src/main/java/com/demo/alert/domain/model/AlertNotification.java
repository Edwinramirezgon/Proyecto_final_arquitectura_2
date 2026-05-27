package com.demo.alert.domain.model;

import java.time.LocalDateTime;

public class AlertNotification {

    private Long          id;
    private Long          alertId;
    private String        zoneId;
    private double        averageCo2;
    private String        level;
    private String        nearestZoneName;
    private double        latitude;
    private double        longitude;
    private LocalDateTime triggeredAt;
    private String        status;          // SENT | FAILED

    private AlertNotification() {}

    public static AlertNotification create(Long alertId, String zoneId, double averageCo2,
                                           String level, String nearestZoneName,
                                           double latitude, double longitude,
                                           LocalDateTime triggeredAt) {
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");
        if (level == null || level.isBlank())
            throw new IllegalArgumentException("El nivel de alerta es obligatorio.");

        AlertNotification n  = new AlertNotification();
        n.alertId            = alertId;
        n.zoneId             = zoneId;
        n.averageCo2         = averageCo2;
        n.level              = level;
        n.nearestZoneName    = nearestZoneName;
        n.latitude           = latitude;
        n.longitude          = longitude;
        n.triggeredAt        = triggeredAt;
        n.status             = "PENDING";
        return n;
    }

    public boolean isCritical() { return "CRITICAL".equals(level); }
    public boolean isHigh()     { return "HIGH".equals(level); }

    public Long          getId()                        { return id; }
    public void          setId(Long id)                 { this.id = id; }
    public Long          getAlertId()                   { return alertId; }
    public String        getZoneId()                    { return zoneId; }
    public double        getAverageCo2()                { return averageCo2; }
    public String        getLevel()                     { return level; }
    public String        getNearestZoneName()           { return nearestZoneName; }
    public double        getLatitude()                  { return latitude; }
    public double        getLongitude()                 { return longitude; }
    public LocalDateTime getTriggeredAt()               { return triggeredAt; }
    public String        getStatus()                    { return status; }
    public void          setStatus(String status)       { this.status = status; }
}
