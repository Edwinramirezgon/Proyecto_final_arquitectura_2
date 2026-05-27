package com.demo.alert.application.usecase;

import com.demo.alert.application.port.in.ProcessAlertUseCase;
import com.demo.alert.application.port.out.AlertNotificationRepository;
import com.demo.alert.application.port.out.EmergencyNotifierPort;
import com.demo.alert.application.port.out.SubscriberClientPort;
import com.demo.alert.domain.model.AlertNotification;
import org.springframework.stereotype.Service;

@Service
public class AlertProcessingService implements ProcessAlertUseCase {

    private final EmergencyNotifierPort       emergencyNotifierPort;
    private final AlertNotificationRepository alertNotificationRepository;
    private final SubscriberClientPort        subscriberClientPort;

    public AlertProcessingService(EmergencyNotifierPort emergencyNotifierPort,
                                  AlertNotificationRepository alertNotificationRepository,
                                  SubscriberClientPort subscriberClientPort) {
        this.emergencyNotifierPort       = emergencyNotifierPort;
        this.alertNotificationRepository = alertNotificationRepository;
        this.subscriberClientPort        = subscriberClientPort;
    }

    @Override
    public void process(AlertNotification notification) {
        // 1. Notificar al equipo técnico siempre
        try {
            emergencyNotifierPort.notifyEmergency(notification);
            notification.setStatus("SENT");
        } catch (Exception e) {
            notification.setStatus("FAILED");
        } finally {
            alertNotificationRepository.save(notification);
        }

        // 2. Notificar a suscriptores de la zona afectada
        subscriberClientPort.findEmailsByZone(notification.getZoneId())
                .forEach(email -> {
                    try {
                        emergencyNotifierPort.notifySubscriber(email, notification);
                    } catch (Exception ignored) {
                        // fallo en un suscriptor no detiene los demás
                    }
                });
    }
}
