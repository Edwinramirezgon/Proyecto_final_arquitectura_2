package com.demo.alert.domain.model;

import java.time.LocalDateTime;

public class MaintenanceEvent {

    private Long          id;
    private String        sensorId;
    private String        zoneId;
    private double        previousCo2;
    private double        currentCo2;
    private LocalDateTime detectedAt;
    private String        status;          // NOTIFIED | FAILED

    private MaintenanceEvent() {}

    public static MaintenanceEvent create(String sensorId, String zoneId,
                                          double previousCo2, double currentCo2,
                                          LocalDateTime detectedAt) {
        if (sensorId == null || sensorId.isBlank())
            throw new IllegalArgumentException("El identificador del sensor es obligatorio.");
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");

        MaintenanceEvent e = new MaintenanceEvent();
        e.sensorId         = sensorId;
        e.zoneId           = zoneId;
        e.previousCo2      = previousCo2;
        e.currentCo2       = currentCo2;
        e.detectedAt       = detectedAt;
        e.status           = "PENDING";
        return e;
    }

    public double jumpMagnitude() { return Math.abs(currentCo2 - previousCo2); }

    public Long          getId()                    { return id; }
    public void          setId(Long id)             { this.id = id; }
    public String        getSensorId()              { return sensorId; }
    public String        getZoneId()                { return zoneId; }
    public double        getPreviousCo2()           { return previousCo2; }
    public double        getCurrentCo2()            { return currentCo2; }
    public LocalDateTime getDetectedAt()            { return detectedAt; }
    public String        getStatus()                { return status; }
    public void          setStatus(String status)   { this.status = status; }
}
