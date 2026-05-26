package com.demo.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaZoneSubscriptionRepository extends JpaRepository<ZoneSubscriptionEntity, Long> {
    List<ZoneSubscriptionEntity> findByUsername(String username);
    List<ZoneSubscriptionEntity> findByZoneId(String zoneId);
    boolean                      existsByUsernameAndZoneId(String username, String zoneId);
    void                         deleteByUsernameAndZoneId(String username, String zoneId);
}
