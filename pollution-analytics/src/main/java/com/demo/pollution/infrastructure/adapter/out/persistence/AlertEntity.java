package com.demo.pollution.infrastructure.adapter.out.persistence;

import com.demo.pollution.domain.model.Alert;
import com.demo.pollution.domain.model.AlertLevel;
import com.demo.pollution.domain.model.ResolutionReason;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long          id;
    @Column(name = "zone_id",     nullable = false)
    private String        zoneId;
    @Column(name = "average_co2", nullable = false)
    private double        averageCo2;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertLevel    level;
    @Column(name = "nearest_zone_name")
    private String        nearestZoneName;
    @Column(nullable = false)
    private double        latitude;
    @Column(nullable = false)
    private double        longitude;
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;
    @Column(nullable = false)
    private boolean       active;
    @Enumerated(EnumType.STRING)
    @Column(name = "resolved_reason")
    private ResolutionReason resolvedReason;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public AlertEntity() {}

    public static AlertEntity fromDomain(Alert a) {
        AlertEntity e      = new AlertEntity();
        e.id               = a.getId();
        e.zoneId           = a.getZoneId();
        e.averageCo2       = a.getAverageCo2();
        e.level            = a.getLevel();
        e.nearestZoneName  = a.getNearestZoneName();
        e.latitude         = a.getLatitude();
        e.longitude        = a.getLongitude();
        e.triggeredAt      = a.getTriggeredAt();
        e.active           = a.isActive();
        e.resolvedReason   = a.getResolvedReason();
        e.resolvedAt       = a.getResolvedAt();
        return e;
    }

    public Alert toDomain() {
        Alert a = Alert.create(zoneId, averageCo2, level, nearestZoneName, latitude, longitude);
        a.setId(id);
        a.setTriggeredAt(triggeredAt);
        if (resolvedAt != null) {
            a.resolve(resolvedReason);
            a.setResolvedAt(resolvedAt);
        }
        return a;
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
