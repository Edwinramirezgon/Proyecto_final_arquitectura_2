package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.SubscriptionUseCase;
import com.demo.auth.application.port.out.SubscriptionRepository;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import com.demo.auth.domain.model.ZoneSubscription;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository         userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository         = userRepository;
    }

    @Override
    public ZoneSubscription subscribe(String username, String zoneId) {
        userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (subscriptionRepository.existsByUsernameAndZoneId(username, zoneId))
            throw new IllegalStateException(
                    String.format("Ya estás suscrito a la zona '%s'.", zoneId));

        return subscriptionRepository.save(ZoneSubscription.create(username, zoneId));
    }

    @Override
    public void unsubscribe(String username, String zoneId) {
        if (!subscriptionRepository.existsByUsernameAndZoneId(username, zoneId))
            throw new IllegalStateException(
                    String.format("No tienes una suscripción activa para la zona '%s'.", zoneId));

        subscriptionRepository.deleteByUsernameAndZoneId(username, zoneId);
    }

    @Override
    public List<ZoneSubscription> findByUsername(String username) {
        return subscriptionRepository.findByUsername(username);
    }

    @Override
    public List<String> findSubscriberEmailsByZone(String zoneId) {
        return subscriptionRepository.findByZoneId(zoneId).stream()
                .map(s -> userRepository.findByUsername(s.getUsername())
                        .map(u -> u.getEmail())
                        .orElse(null))
                .filter(email -> email != null)
                .toList();
    }
}
