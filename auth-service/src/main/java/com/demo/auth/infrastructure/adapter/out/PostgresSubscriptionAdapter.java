package com.demo.auth.infrastructure.adapter.out;

import com.demo.auth.application.port.out.SubscriptionRepository;
import com.demo.auth.domain.model.ZoneSubscription;
import com.demo.auth.infrastructure.adapter.out.persistence.JpaZoneSubscriptionRepository;
import com.demo.auth.infrastructure.adapter.out.persistence.ZoneSubscriptionEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

public class PostgresSubscriptionAdapter implements SubscriptionRepository {

    private final JpaZoneSubscriptionRepository jpa;

    public PostgresSubscriptionAdapter(JpaZoneSubscriptionRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public ZoneSubscription save(ZoneSubscription subscription) {
        ZoneSubscriptionEntity entity = Objects.requireNonNull(
                ZoneSubscriptionEntity.fromDomain(subscription),
                "zone subscription entity must be provided");
        return jpa.save(entity).toDomain();
    }

    @Override
    @Transactional
    public void deleteByUsernameAndZoneId(String username, String zoneId) {
        jpa.deleteByUsernameAndZoneId(username, zoneId);
    }

    @Override
    public List<ZoneSubscription> findByUsername(String username) {
        return jpa.findByUsername(username).stream()
                .map(ZoneSubscriptionEntity::toDomain).toList();
    }

    @Override
    public List<ZoneSubscription> findByZoneId(String zoneId) {
        return jpa.findByZoneId(zoneId).stream()
                .map(ZoneSubscriptionEntity::toDomain).toList();
    }

    @Override
    public boolean existsByUsernameAndZoneId(String username, String zoneId) {
        return jpa.existsByUsernameAndZoneId(username, zoneId);
    }
}
