package com.demo.alert.application.port.out;

import com.demo.alert.domain.model.AlertNotification;

public interface EmergencyNotifierPort {
    void notifyEmergency(AlertNotification notification);
    void notifySubscriber(String email, AlertNotification notification);
    void notifyMaintenance(String sensorId, String zoneId, double jump);
    void sendWelcome(String email, String username, String rawPassword, String appBaseUrl);
    void sendPasswordChanged(String email, String username, String appBaseUrl);
    void sendPasswordResetLink(String email, String username, String resetToken, String appBaseUrl);
}
