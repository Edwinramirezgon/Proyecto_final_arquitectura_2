package com.demo.auth.application.port.out;

import com.demo.auth.domain.model.ZoneSubscription;
import java.util.List;

public interface SubscriptionRepository {
    ZoneSubscription       save(ZoneSubscription subscription);
    void                   deleteByUsernameAndZoneId(String username, String zoneId);
    List<ZoneSubscription> findByUsername(String username);
    List<ZoneSubscription> findByZoneId(String zoneId);
    boolean                existsByUsernameAndZoneId(String username, String zoneId);
}
