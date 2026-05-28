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

    @Override
    public void sendWelcome(String email, String username, String rawPassword, String appBaseUrl) {
        String subject = "¡Bienvenido a EcoMonitor, " + username + "!";
        String body = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;background:#f0f9ff;border-radius:12px">
                  <div style="text-align:center;margin-bottom:24px">
                    <h1 style="color:#1e3a5f;margin:0">🌿 EcoMonitor</h1>
                    <p style="color:#6b7280;margin:4px 0 0">Red de Alerta Ambiental</p>
                  </div>
                  <div style="background:#fff;border-radius:10px;padding:24px;box-shadow:0 1px 4px rgba(0,0,0,0.07)">
                    <h2 style="color:#1e3a5f;margin:0 0 12px">¡Bienvenido, %s! 👋</h2>
                    <p style="color:#374151;line-height:1.6">Tu cuenta ha sido creada exitosamente.</p>
                    <div style="background:#f0fdf4;border-radius:8px;padding:16px;margin:16px 0;border-left:4px solid #16a34a">
                      <p style="margin:0 0 4px;color:#374151"><strong>Usuario:</strong> %s</p>
                      <p style="margin:0 0 4px;color:#374151"><strong>Contraseña temporal:</strong> <code style="background:#e5e7eb;padding:2px 8px;border-radius:4px">%s</code></p>
                    </div>
                    <div style="margin:20px 0;text-align:center">
                      <a href="%s/login" style="background:#1e3a5f;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:bold">Iniciar sesión</a>
                    </div>
                  </div>
                  <p style="text-align:center;color:#9ca3af;font-size:0.78rem;margin-top:16px">EcoMonitor — Gobierno Local · Valle de Aburrá</p>
                </div>
                """.formatted(username, username, rawPassword, appBaseUrl);
        sendHtml(email, subject, body);
    }

    @Override
    public void sendPasswordChanged(String email, String username, String appBaseUrl) {
        String subject = "[EcoMonitor] Tu contraseña fue actualizada";
        String body = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;background:#f0f9ff;border-radius:12px">
                  <div style="background:#fff;border-radius:10px;padding:24px">
                    <h2 style="color:#1e3a5f;margin:0 0 12px">🔐 Contraseña actualizada</h2>
                    <p style="color:#374151">Hola <strong>%s</strong>, tu contraseña ha sido cambiada exitosamente.</p>
                    <div style="background:#fef3c7;border-radius:8px;padding:14px;margin:16px 0">
                      <p style="color:#92400e;margin:0;font-size:0.9rem">⚠️ Si no realizaste este cambio, contacta al administrador de inmediato.</p>
                    </div>
                    <div style="margin:20px 0;text-align:center">
                      <a href="%s/login" style="background:#1e3a5f;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:bold">Iniciar sesión</a>
                    </div>
                  </div>
                </div>
                """.formatted(username, appBaseUrl);
        sendHtml(email, subject, body);
    }

    @Override
    public void sendPasswordResetLink(String email, String username, String resetToken, String appBaseUrl) {
        String resetUrl = appBaseUrl + "/reset-password?token=" + resetToken;
        String subject  = "[EcoMonitor] Recuperación de contraseña";
        String body = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;background:#f0f9ff;border-radius:12px">
                  <div style="background:#fff;border-radius:10px;padding:24px">
                    <h2 style="color:#1e3a5f;margin:0 0 12px">🔑 Recuperar contraseña</h2>
                    <p style="color:#374151">Hola <strong>%s</strong>, haz clic para crear una nueva contraseña. Válido por <strong>15 minutos</strong>.</p>
                    <div style="margin:24px 0;text-align:center">
                      <a href="%s" style="background:#dc2626;color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold">Restablecer contraseña</a>
                    </div>
                    <p style="color:#9ca3af;font-size:0.78rem;word-break:break-all">Enlace: %s</p>
                  </div>
                </div>
                """.formatted(username, resetUrl, resetUrl);
        sendHtml(email, subject, body);
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(htmlBody);
            mailSender.send(mail);
        } catch (Exception e) {
            throw new NotificationDeliveryException(to, e.getMessage());
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
