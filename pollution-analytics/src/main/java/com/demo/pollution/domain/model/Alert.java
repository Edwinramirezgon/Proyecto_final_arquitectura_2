package com.demo.pollution.domain.model;

import java.time.LocalDateTime;

public class Alert {

    private Long          id;
    private String        zoneId;
    private double        averageCo2;
    private AlertLevel    level;
    private String        nearestZoneName;   // nombre de zona sensible cercana (puede ser null)
    private double        latitude;
    private double        longitude;
    private LocalDateTime triggeredAt;
    private boolean       active;
    private ResolutionReason resolvedReason;
    private LocalDateTime    resolvedAt;

    private Alert() {}

    public static Alert create(String zoneId, double averageCo2, AlertLevel level,
                               String nearestZoneName, double latitude, double longitude) {
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");

        Alert a          = new Alert();
        a.zoneId         = zoneId;
        a.averageCo2     = averageCo2;
        a.level          = level;
        a.nearestZoneName = nearestZoneName;
        a.latitude       = latitude;
        a.longitude      = longitude;
        a.triggeredAt    = LocalDateTime.now();
        a.active         = true;
        return a;
    }

    public Long          getId()                        { return id; }
    public void          setId(Long id)                 { this.id = id; }
    public String        getZoneId()                    { return zoneId; }
    public double        getAverageCo2()                { return averageCo2; }
    public AlertLevel    getLevel()                     { return level; }
    public String        getNearestZoneName()           { return nearestZoneName; }
    public double        getLatitude()                  { return latitude; }
    public double        getLongitude()                 { return longitude; }
    public LocalDateTime getTriggeredAt()               { return triggeredAt; }
    public boolean       isActive()                          { return active; }
    public void          setTriggeredAt(LocalDateTime t)     { this.triggeredAt = t; }
    public ResolutionReason getResolvedReason()                 { return resolvedReason; }
    public LocalDateTime getResolvedAt()                     { return resolvedAt; }

    public void resolve(ResolutionReason reason) {
        this.active         = false;
        this.resolvedReason = reason;
        this.resolvedAt     = LocalDateTime.now();
    }

    public void setResolvedAt(LocalDateTime t) { this.resolvedAt = t; }
}
