package com.demo.alert.infrastructure.adapter.out;

import com.demo.alert.application.port.out.EmergencyNotifierPort;
import com.demo.alert.domain.exception.NotificationDeliveryException;
import com.demo.alert.domain.model.AlertNotification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class GmailEmergencyNotifierAdapter implements EmergencyNotifierPort {

    private final JavaMailSender mailSender;
    private final String         technicalEmail;

    public GmailEmergencyNotifierAdapter(JavaMailSender mailSender, String technicalEmail) {
        this.mailSender     = mailSender;
        this.technicalEmail = technicalEmail;
    }

    @Override
    public void notifyEmergency(AlertNotification notification) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(technicalEmail);
            mail.setSubject(buildAlertSubject(notification));
            mail.setText(buildAlertBody(notification));
            mailSender.send(mail);
        } catch (Exception e) {
            throw new NotificationDeliveryException(technicalEmail, e.getMessage());
        }
    }

    @Override
    public void notifySubscriber(String email, AlertNotification notification) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject(String.format("[EcoMonitor] Alerta en tu zona %s — %s",
                    notification.getZoneId(), notification.getLevel()));
            mail.setText(buildSubscriberBody(notification));
            mailSender.send(mail);
        } catch (Exception e) {
            throw new NotificationDeliveryException(email, e.getMessage());
        }
    }

    private String buildSubscriberBody(AlertNotification n) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hola, te informamos que se ha detectado una alerta ambiental en una zona a la que estás suscrito.\n\n");
        sb.append(String.format("Zona:         %s%n", n.getZoneId()));
        sb.append(String.format("Nivel:        %s%n", n.getLevel()));
        sb.append(String.format("CO2 promedio: %.1f µg/m³%n", n.getAverageCo2()));
        if (n.getNearestZoneName() != null && !n.getNearestZoneName().isBlank())
            sb.append(String.format("Zona sensible cercana: %s%n", n.getNearestZoneName()));
        sb.append(String.format("Detectada:    %s%n", n.getTriggeredAt()));
        if (n.isCritical())
            sb.append("\n🚨 Nivel CRÍTICO — Se recomienda evitar salir y mantener ventanas cerradas.");
        else if (n.isHigh())
            sb.append("\n⚠️ Nivel ALTO — Se recomienda reducir actividad al aire libre.");
        sb.append("\n\nPuedes desuscribirte de esta zona desde tu panel en EcoMonitor.");
        return sb.toString();
    }

    @Override
    public void notifyMaintenance(String sensorId, String zoneId, double jump) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(technicalEmail);
            mail.setSubject(String.format("[MANTENIMIENTO] Sensor fallido: %s", sensorId));
            mail.setText(String.format(
                "Se detectó un sensor con lectura inconsistente.%n%n" +
                "Sensor ID: %s%nZona: %s%nSalto detectado: %.1f µg/m³%n%n" +
                "Por favor revisar el sensor a la brevedad.",
                sensorId, zoneId, jump));
            mailSender.send(mail);
        } catch (Exception e) {
            throw new NotificationDeliveryException(technicalEmail, e.getMessage());
        }
    }

    private String buildAlertSubject(AlertNotification n) {
        return String.format("[%s] Alerta Ambiental — Zona %s — CO2: %.1f µg/m³",
                n.getLevel(), n.getZoneId(), n.getAverageCo2());
    }

    private String buildAlertBody(AlertNotification n) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ ALERTA AMBIENTAL DECLARADA\n\n");
        sb.append(String.format("Nivel:        %s%n", n.getLevel()));
        sb.append(String.format("Zona:         %s%n", n.getZoneId()));
        sb.append(String.format("CO2 promedio: %.1f µg/m³%n", n.getAverageCo2()));
        sb.append(String.format("Coordenadas:  %.4f, %.4f%n", n.getLatitude(), n.getLongitude()));
        if (n.getNearestZoneName() != null && !n.getNearestZoneName().isBlank())
            sb.append(String.format("Zona sensible cercana: %s%n", n.getNearestZoneName()));
        sb.append(String.format("Detectada:    %s%n", n.getTriggeredAt()));
        if (n.isCritical())
            sb.append("\n🚨 ZONA SENSIBLE AFECTADA — Se requiere restricción vehicular inmediata.");
        return sb.toString();
    }
}
