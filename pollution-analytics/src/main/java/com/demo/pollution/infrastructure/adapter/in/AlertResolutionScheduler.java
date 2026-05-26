package com.demo.pollution.infrastructure.adapter.in;

import com.demo.pollution.application.port.in.ResolveAlertUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertResolutionScheduler {

    private final ResolveAlertUseCase resolveAlertUseCase;

    public AlertResolutionScheduler(ResolveAlertUseCase resolveAlertUseCase) {
        this.resolveAlertUseCase = resolveAlertUseCase;
    }

    @Scheduled(fixedDelay = 300_000)
    public void run() {
        resolveAlertUseCase.resolveStaleAlerts();
    }
}
