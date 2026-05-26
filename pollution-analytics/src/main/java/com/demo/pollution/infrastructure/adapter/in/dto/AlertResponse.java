package com.demo.pollution.infrastructure.adapter.in.dto;

import com.demo.pollution.domain.model.AlertLevel;
import com.demo.pollution.domain.model.ResolutionReason;
import java.time.LocalDateTime;

public class AlertResponse {

    private Long             id;
    private String           zoneId;
    private double           averageCo2;
    private AlertLevel       level;
    private String           nearestZoneName;
    private double           latitude;
    private double           longitude;
    private LocalDateTime    triggeredAt;
    private boolean          active;
    private ResolutionReason resolvedReason;
    private LocalDateTime    resolvedAt;

    public AlertResponse() {}

    public AlertResponse(Long id, String zoneId, double averageCo2, AlertLevel level,
                         String nearestZoneName, double latitude, double longitude,
                         LocalDateTime triggeredAt, boolean active,
                         ResolutionReason resolvedReason, LocalDateTime resolvedAt) {
        this.id              = id;
        this.zoneId          = zoneId;
        this.averageCo2      = averageCo2;
        this.level           = level;
        this.nearestZoneName = nearestZoneName;
        this.latitude        = latitude;
        this.longitude       = longitude;
        this.triggeredAt     = triggeredAt;
        this.active          = active;
        this.resolvedReason  = resolvedReason;
        this.resolvedAt      = resolvedAt;
    }

    public Long             getId()              { return id; }
    public String           getZoneId()          { return zoneId; }
    public double           getAverageCo2()      { return averageCo2; }
    public AlertLevel       getLevel()           { return level; }
    public String           getNearestZoneName() { return nearestZoneName; }
    public double           getLatitude()        { return latitude; }
    public double           getLongitude()       { return longitude; }
    public LocalDateTime    getTriggeredAt()     { return triggeredAt; }
    public boolean          isActive()           { return active; }
    public ResolutionReason getResolvedReason()  { return resolvedReason; }
    public LocalDateTime    getResolvedAt()      { return resolvedAt; }
}
