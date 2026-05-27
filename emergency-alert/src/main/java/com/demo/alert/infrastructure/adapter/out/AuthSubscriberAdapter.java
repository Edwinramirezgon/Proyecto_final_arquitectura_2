package com.demo.alert.infrastructure.adapter.out;

import com.demo.alert.application.port.out.SubscriberClientPort;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class AuthSubscriberAdapter implements SubscriberClientPort {

    private final RestTemplate restTemplate;
    private final String       authServiceUrl;

    public AuthSubscriberAdapter(RestTemplate restTemplate, String authServiceUrl) {
        this.restTemplate   = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    @Override
    public List<String> findEmailsByZone(String zoneId) {
        try {
            String   url    = authServiceUrl + "/users/subscriptions/zone/" + zoneId + "/emails";
            String[] emails = restTemplate.getForObject(url, String[].class);
            return emails != null ? Arrays.asList(emails) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
