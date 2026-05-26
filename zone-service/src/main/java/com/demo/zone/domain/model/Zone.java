package com.demo.zone.domain.model;

public class Zone {

    private static final double LAT_MIN  = -4.23;
    private static final double LAT_MAX  = 12.45;
    private static final double LON_MIN  = -81.73;
    private static final double LON_MAX  = -66.87;

    private Long          id;
    private String        name;
    private double        latitude;
    private double        longitude;
    private double        radiusKm;
    private SensitiveType sensitiveType;
    private int           priority;

    private Zone() {}

    public static Zone create(String name, double latitude, double longitude,
                              double radiusKm, SensitiveType sensitiveType) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre de la zona es obligatorio.");
        if (radiusKm <= 0)
            throw new IllegalArgumentException("El radio debe ser mayor a cero.");
        if (latitude < LAT_MIN || latitude > LAT_MAX || longitude < LON_MIN || longitude > LON_MAX)
            throw new IllegalArgumentException(
                "Las coordenadas de la zona deben estar dentro del territorio colombiano "
                + "(lat " + LAT_MIN + "–" + LAT_MAX + ", lon " + LON_MIN + "–" + LON_MAX + ").");

        Zone z         = new Zone();
        z.name         = name;
        z.latitude     = latitude;
        z.longitude    = longitude;
        z.radiusKm     = radiusKm;
        z.sensitiveType = sensitiveType == null ? SensitiveType.NONE : sensitiveType;
        z.priority     = z.sensitiveType.getBasePriority();
        return z;
    }

    /**
     * Calcula si el punto (lat, lon) está dentro del radio de esta zona.
     * Usa la fórmula de Haversine para distancia en km.
     */
    public boolean isNear(double lat, double lon) {
        final double R    = 6371.0;
        double dLat       = Math.toRadians(lat - this.latitude);
        double dLon       = Math.toRadians(lon - this.longitude);
        double a          = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                          + Math.cos(Math.toRadians(this.latitude))
                          * Math.cos(Math.toRadians(lat))
                          * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double distanceKm = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return distanceKm <= this.radiusKm;
    }

    public Long          getId()                              { return id; }
    public void          setId(Long id)                       { this.id = id; }
    public String        getName()                            { return name; }
    public double        getLatitude()                        { return latitude; }
    public double        getLongitude()                       { return longitude; }
    public double        getRadiusKm()                        { return radiusKm; }
    public SensitiveType getSensitiveType()                   { return sensitiveType; }
    public int           getPriority()                        { return priority; }
    public void          setName(String name)                 { this.name = name; }
    public void          setLatitude(double latitude)         { this.latitude = latitude; }
    public void          setLongitude(double longitude)       { this.longitude = longitude; }
    public void          setRadiusKm(double radiusKm)         { this.radiusKm = radiusKm; }
    public void          setSensitiveType(SensitiveType type) { this.sensitiveType = type; }
    public void          setPriority(int priority)            { this.priority = priority; }
}
