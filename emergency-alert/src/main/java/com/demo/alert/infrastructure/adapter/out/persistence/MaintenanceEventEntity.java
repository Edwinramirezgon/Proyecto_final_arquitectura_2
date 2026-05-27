package com.demo.alert.infrastructure.adapter.out.persistence;

import com.demo.alert.domain.model.MaintenanceEvent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_events")
public class MaintenanceEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long          id;
    @Column(name = "sensor_id",    nullable = false)
    private String        sensorId;
    @Column(name = "zone_id",      nullable = false)
    private String        zoneId;
    @Column(name = "previous_co2", nullable = false)
    private double        previousCo2;
    @Column(name = "current_co2",  nullable = false)
    private double        currentCo2;
    @Column(name = "detected_at",  nullable = false)
    private LocalDateTime detectedAt;
    @Column(nullable = false)
    private String        status;

    public MaintenanceEventEntity() {}

    public static MaintenanceEventEntity fromDomain(MaintenanceEvent e) {
        MaintenanceEventEntity entity = new MaintenanceEventEntity();
        entity.id          = e.getId();
        entity.sensorId    = e.getSensorId();
        entity.zoneId      = e.getZoneId();
        entity.previousCo2 = e.getPreviousCo2();
        entity.currentCo2  = e.getCurrentCo2();
        entity.detectedAt  = e.getDetectedAt();
        entity.status      = e.getStatus();
        return entity;
    }

    public Long          getId()          { return id; }
    public String        getSensorId()    { return sensorId; }
    public String        getZoneId()      { return zoneId; }
    public double        getPreviousCo2() { return previousCo2; }
    public double        getCurrentCo2()  { return currentCo2; }
    public LocalDateTime getDetectedAt()  { return detectedAt; }
    public String        getStatus()      { return status; }
}
