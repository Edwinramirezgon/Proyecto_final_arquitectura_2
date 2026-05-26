package com.demo.auth.infrastructure.adapter.out.persistence;

import com.demo.auth.domain.model.ZoneSubscription;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "zone_subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "zone_id"}))
public class ZoneSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long          id;

    @Column(nullable = false)
    private String        username;

    @Column(name = "zone_id", nullable = false)
    private String        zoneId;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    public ZoneSubscriptionEntity() {}

    public static ZoneSubscriptionEntity fromDomain(ZoneSubscription s) {
        ZoneSubscriptionEntity e = new ZoneSubscriptionEntity();
        e.id           = s.getId();
        e.username     = s.getUsername();
        e.zoneId       = s.getZoneId();
        e.subscribedAt = s.getSubscribedAt();
        return e;
    }

    public ZoneSubscription toDomain() {
        ZoneSubscription s = ZoneSubscription.create(username, zoneId);
        s.setId(id);
        s.setSubscribedAt(subscribedAt);
        return s;
    }

    public Long          getId()           { return id; }
    public String        getUsername()     { return username; }
    public String        getZoneId()       { return zoneId; }
    public LocalDateTime getSubscribedAt() { return subscribedAt; }
}
