package com.demo.alert.application.port.out;

import java.util.List;

public interface SubscriberClientPort {
    List<String> findEmailsByZone(String zoneId);
}
