package com.demo.zone.infrastructure.adapter.out.persistence;

import com.demo.zone.domain.model.SensitiveType;
import com.demo.zone.domain.model.Zone;
import jakarta.persistence.*;

@Entity
@Table(name = "zones")
public class ZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "radius_km", nullable = false)
    private double radiusKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitive_type", nullable = false)
    private SensitiveType sensitiveType;

    @Column(nullable = false)
    private int priority;

    public ZoneEntity() {}

    public static ZoneEntity fromDomain(Zone z) {
        ZoneEntity e    = new ZoneEntity();
        e.id            = z.getId();
        e.name          = z.getName();
        e.latitude      = z.getLatitude();
        e.longitude     = z.getLongitude();
        e.radiusKm      = z.getRadiusKm();
        e.sensitiveType = z.getSensitiveType();
        e.priority      = z.getPriority();
        return e;
    }

    public Zone toDomain() {
        Zone z = Zone.create(name, latitude, longitude, radiusKm, sensitiveType);
        z.setId(id);
        z.setPriority(priority);
        return z;
    }

    public Long          getId()            { return id; }
    public String        getName()          { return name; }
    public double        getLatitude()      { return latitude; }
    public double        getLongitude()     { return longitude; }
    public double        getRadiusKm()      { return radiusKm; }
    public SensitiveType getSensitiveType() { return sensitiveType; }
    public int           getPriority()      { return priority; }
}
