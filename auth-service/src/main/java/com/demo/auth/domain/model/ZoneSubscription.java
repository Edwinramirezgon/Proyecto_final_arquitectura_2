package com.demo.auth.domain.model;

import java.time.LocalDateTime;

public class ZoneSubscription {

    private Long          id;
    private String        username;
    private String        zoneId;
    private LocalDateTime subscribedAt;

    private ZoneSubscription() {}

    public static ZoneSubscription create(String username, String zoneId) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (zoneId == null || zoneId.isBlank())
            throw new IllegalArgumentException("El identificador de zona es obligatorio.");

        ZoneSubscription s = new ZoneSubscription();
        s.username         = username;
        s.zoneId           = zoneId;
        s.subscribedAt     = LocalDateTime.now();
        return s;
    }

    public Long          getId()                        { return id; }
    public void          setId(Long id)                 { this.id = id; }
    public String        getUsername()                  { return username; }
    public String        getZoneId()                    { return zoneId; }
    public LocalDateTime getSubscribedAt()              { return subscribedAt; }
    public void          setSubscribedAt(LocalDateTime t){ this.subscribedAt = t; }
}
