package com.demo.pollution.infrastructure.adapter.out.persistence;

import com.demo.pollution.domain.model.SensorReading;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "sensor_readings")
@IdClass(SensorReadingEntity.SensorReadingId.class)
public class SensorReadingEntity {

    public static class SensorReadingId implements Serializable {
        private Long          id;
        private LocalDateTime recordedAt;
        public SensorReadingId() {}
        public SensorReadingId(Long id, LocalDateTime recordedAt) {
            this.id = id; this.recordedAt = recordedAt;
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof SensorReadingId that)) return false;
            return Objects.equals(id, that.id) && Objects.equals(recordedAt, that.recordedAt);
        }
        @Override public int hashCode() { return Objects.hash(id, recordedAt); }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sensor_readings_seq")
    @SequenceGenerator(name = "sensor_readings_seq", sequenceName = "sensor_readings_id_seq", allocationSize = 1)
    private Long          id;

    @Id
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "sensor_id",  nullable = false)
    private String        sensorId;
    @Column(name = "zone_id",    nullable = false)
    private String        zoneId;
    @Column(nullable = false)
    private double        latitude;
    @Column(nullable = false)
    private double        longitude;
    @Column(name = "co2_level",  nullable = false)
    private double        co2Level;

    public SensorReadingEntity() {}

    public static SensorReadingEntity fromDomain(SensorReading r) {
        SensorReadingEntity e = new SensorReadingEntity();
        e.id         = r.getId();
        e.sensorId   = r.getSensorId();
        e.zoneId     = r.getZoneId();
        e.latitude   = r.getLatitude();
        e.longitude  = r.getLongitude();
        e.co2Level   = r.getCo2Level();
        e.recordedAt = r.getRecordedAt();
        return e;
    }

    public SensorReading toDomain() {
        SensorReading r = SensorReading.create(sensorId, zoneId, latitude, longitude, co2Level);
        r.setId(id);
        r.setRecordedAt(recordedAt);
        return r;
    }

    public Long          getId()         { return id; }
    public String        getSensorId()   { return sensorId; }
    public String        getZoneId()     { return zoneId; }
    public double        getLatitude()   { return latitude; }
    public double        getLongitude()  { return longitude; }
    public double        getCo2Level()   { return co2Level; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
