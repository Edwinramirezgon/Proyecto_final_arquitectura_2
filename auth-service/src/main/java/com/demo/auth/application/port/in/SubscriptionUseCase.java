package com.demo.auth.application.port.in;

import com.demo.auth.domain.model.ZoneSubscription;
import java.util.List;

public interface SubscriptionUseCase {
    ZoneSubscription subscribe(String username, String zoneId);
    void             unsubscribe(String username, String zoneId);
    List<ZoneSubscription> findByUsername(String username);
    List<String>     findSubscriberEmailsByZone(String zoneId);
}
