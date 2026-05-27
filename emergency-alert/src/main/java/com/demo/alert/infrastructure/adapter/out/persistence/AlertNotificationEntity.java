package com.demo.alert.infrastructure.adapter.out.persistence;

import com.demo.alert.domain.model.AlertNotification;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_notifications")
public class AlertNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long          id;
    @Column(name = "alert_id")
    private Long          alertId;
    @Column(name = "zone_id",      nullable = false)
    private String        zoneId;
    @Column(name = "average_co2",  nullable = false)
    private double        averageCo2;
    @Column(nullable = false)
    private String        level;
    @Column(name = "nearest_zone_name")
    private String        nearestZoneName;
    @Column(nullable = false)
    private double        latitude;
    @Column(nullable = false)
    private double        longitude;
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;
    @Column(nullable = false)
    private String        status;

    public AlertNotificationEntity() {}

    public static AlertNotificationEntity fromDomain(AlertNotification n) {
        AlertNotificationEntity e = new AlertNotificationEntity();
        e.id              = n.getId();
        e.alertId         = n.getAlertId();
        e.zoneId          = n.getZoneId();
        e.averageCo2      = n.getAverageCo2();
        e.level           = n.getLevel();
        e.nearestZoneName = n.getNearestZoneName();
        e.latitude        = n.getLatitude();
        e.longitude       = n.getLongitude();
        e.triggeredAt     = n.getTriggeredAt();
        e.status          = n.getStatus();
        return e;
    }

    public Long          getId()             { return id; }
    public Long          getAlertId()        { return alertId; }
    public String        getZoneId()         { return zoneId; }
    public double        getAverageCo2()     { return averageCo2; }
    public String        getLevel()          { return level; }
    public String        getNearestZoneName(){ return nearestZoneName; }
    public double        getLatitude()       { return latitude; }
    public double        getLongitude()      { return longitude; }
    public LocalDateTime getTriggeredAt()    { return triggeredAt; }
    public String        getStatus()         { return status; }

    public AlertNotification toDomain() {
        AlertNotification n = AlertNotification.create(
                alertId, zoneId, averageCo2, level, nearestZoneName,
                latitude, longitude, triggeredAt);
        n.setId(id);
        n.setStatus(status);
        return n;
    }
}
