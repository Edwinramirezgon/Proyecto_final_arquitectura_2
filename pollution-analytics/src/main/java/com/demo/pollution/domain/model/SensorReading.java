package com.demo.pollution.domain.model;

import java.time.LocalDateTime;

public class SensorReading {

    private static final double LAT_MIN  = -4.23;
    private static final double LAT_MAX  = 12.45;
    private static final double LON_MIN  = -81.73;
    private static final double LON_MAX  = -66.87;

    private Long          id;
    private String        sensorId;
    private String        zoneId;
    private double        latitude;
    private double        longitude;
    private double        co2Level;
    private LocalDateTime recordedAt;

    private SensorReading() {}

    public static SensorReading create(String sensorId, String zoneId,
                                       double latitude, double longitude,
                                       double co2Level) {
        if (sensorId == null || sensorId.isBlank())
            throw new IllegalArgumentException("El identificador del sensor es obligatorio.");
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");
        if (co2Level < 0)
            throw new IllegalArgumentException("El nivel de CO2 no puede ser negativo.");
        if (latitude < LAT_MIN || latitude > LAT_MAX || longitude < LON_MIN || longitude > LON_MAX)
            throw new IllegalArgumentException(
                "Las coordenadas del sensor deben estar dentro del territorio colombiano "
                + "(lat " + LAT_MIN + "–" + LAT_MAX + ", lon " + LON_MIN + "–" + LON_MAX + ").");

        SensorReading r = new SensorReading();
        r.sensorId   = sensorId;
        r.zoneId     = zoneId;
        r.latitude   = latitude;
        r.longitude  = longitude;
        r.co2Level   = co2Level;
        r.recordedAt = LocalDateTime.now();
        return r;
    }

    /**
     * Regla de negocio: salto mayor a 200 µg/m³ en una misma lectura
     * respecto a la anterior se considera inconsistente.
     */
    public boolean isInconsistentWith(double previousCo2Level) {
        return Math.abs(this.co2Level - previousCo2Level) > 200.0;
    }

    public Long          getId()                    { return id; }
    public void          setId(Long id)             { this.id = id; }
    public String        getSensorId()              { return sensorId; }
    public String        getZoneId()                { return zoneId; }
    public double        getLatitude()              { return latitude; }
    public double        getLongitude()             { return longitude; }
    public double        getCo2Level()              { return co2Level; }
    public LocalDateTime getRecordedAt()            { return recordedAt; }
    public void          setRecordedAt(LocalDateTime t) { this.recordedAt = t; }
}
