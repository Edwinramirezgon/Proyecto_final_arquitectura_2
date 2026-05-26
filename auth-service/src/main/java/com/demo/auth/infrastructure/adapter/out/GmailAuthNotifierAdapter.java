package com.demo.auth.infrastructure.adapter.out;

import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.domain.model.User;
import jakarta.mail.internet.MimeMessage;
import java.util.Objects;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class GmailAuthNotifierAdapter implements EmailNotifierPort {

    private final JavaMailSender mailSender;
    private final String         fromEmail;
    private final String         appBaseUrl;

    public GmailAuthNotifierAdapter(JavaMailSender mailSender,
                                    String fromEmail,
                                    String appBaseUrl) {
        this.mailSender = mailSender;
        this.fromEmail  = fromEmail;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public void sendWelcome(User user, String rawPassword) {
        String subject = "¡Bienvenido a EcoMonitor, " + user.getUsername() + "!";
        String body = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;background:#f0f9ff;border-radius:12px">
                  <div style="text-align:center;margin-bottom:24px">
                    <h1 style="color:#1e3a5f;margin:0">🌿 EcoMonitor</h1>
                    <p style="color:#6b7280;margin:4px 0 0">Red de Alerta Ambiental</p>
                  </div>
                  <div style="background:#fff;border-radius:10px;padding:24px;box-shadow:0 1px 4px rgba(0,0,0,0.07)">
                    <h2 style="color:#1e3a5f;margin:0 0 12px">¡Bienvenido, %s! 👋</h2>
                    <p style="color:#374151;line-height:1.6">Tu cuenta ha sido creada exitosamente en el sistema de monitoreo ambiental del Valle de Aburrá.</p>
                    <div style="background:#f0fdf4;border-radius:8px;padding:16px;margin:16px 0;border-left:4px solid #16a34a">
                      <p style="margin:0 0 6px;color:#374151;font-size:0.9rem"><strong>Tus credenciales de acceso:</strong></p>
                      <p style="margin:0 0 4px;color:#374151"><strong>Usuario:</strong> %s</p>
                      <p style="margin:0 0 4px;color:#374151"><strong>Contraseña temporal:</strong> <code style="background:#e5e7eb;padding:2px 8px;border-radius:4px;font-size:1rem">%s</code></p>
                    </div>
                    <div style="background:#fef3c7;border-radius:8px;padding:12px;margin:0 0 16px">
                      <p style="color:#92400e;margin:0;font-size:0.85rem">⚠️ Te recomendamos cambiar tu contraseña después de iniciar sesión por primera vez.</p>
                    </div>
                    <div style="margin:20px 0;text-align:center">
                      <a href="%s/login" style="background:#1e3a5f;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:bold">Iniciar sesión</a>
                    </div>
                    <p style="color:#6b7280;font-size:0.85rem">Puedes suscribirte a zonas de tu interés para recibir alertas de emergencia ambiental por email.</p>
                  </div>
                  <p style="text-align:center;color:#9ca3af;font-size:0.78rem;margin-top:16px">EcoMonitor — Gobierno Local · Valle de Aburrá</p>
                </div>
                """.formatted(user.getUsername(), user.getUsername(), rawPassword, appBaseUrl);
        send(user.getEmail(), subject, body);
    }

    @Override
    public void sendPasswordChanged(User user) {
        String subject = "[EcoMonitor] Tu contraseña fue actualizada";
        String body = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;background:#f0f9ff;border-radius:12px">
                  <div style="text-align:center;margin-bottom:24px">
                    <h1 style="color:#1e3a5f;margin:0">🌿 EcoMonitor</h1>
                  </div>
                  <div style="background:#fff;border-radius:10px;padding:24px;box-shadow:0 1px 4px rgba(0,0,0,0.07)">
                    <h2 style="color:#1e3a5f;margin:0 0 12px">🔐 Contraseña actualizada</h2>
                    <p style="color:#374151;line-height:1.6">Hola <strong>%s</strong>, tu contraseña ha sido cambiada exitosamente.</p>
                    <div style="background:#fef3c7;border-radius:8px;padding:14px;margin:16px 0">
                      <p style="color:#92400e;margin:0;font-size:0.9rem">⚠️ Si no realizaste este cambio, contacta al administrador de inmediato.</p>
                    </div>
                    <div style="margin:20px 0;text-align:center">
                      <a href="%s/login" style="background:#1e3a5f;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:bold">Iniciar sesión</a>
                    </div>
                  </div>
                  <p style="text-align:center;color:#9ca3af;font-size:0.78rem;margin-top:16px">EcoMonitor — Gobierno Local · Valle de Aburrá</p>
                </div>
                """.formatted(user.getUsername(), appBaseUrl);
        send(user.getEmail(), subject, body);
    }

    @Override
    public void sendPasswordResetLink(User user, String resetToken) {
        String resetUrl = appBaseUrl + "/reset-password?token=" + resetToken;
        String subject  = "[EcoMonitor] Recuperación de contraseña";
        String body = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;background:#f0f9ff;border-radius:12px">
                  <div style="text-align:center;margin-bottom:24px">
                    <h1 style="color:#1e3a5f;margin:0">🌿 EcoMonitor</h1>
                  </div>
                  <div style="background:#fff;border-radius:10px;padding:24px;box-shadow:0 1px 4px rgba(0,0,0,0.07)">
                    <h2 style="color:#1e3a5f;margin:0 0 12px">🔑 Recuperar contraseña</h2>
                    <p style="color:#374151;line-height:1.6">Hola <strong>%s</strong>, recibimos una solicitud para restablecer tu contraseña.</p>
                    <p style="color:#374151;line-height:1.6">Haz clic en el botón para crear una nueva contraseña. Este enlace es válido por <strong>15 minutos</strong>.</p>
                    <div style="margin:24px 0;text-align:center">
                      <a href="%s" style="background:#dc2626;color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:1rem">Restablecer contraseña</a>
                    </div>
                    <p style="color:#6b7280;font-size:0.82rem">Si no solicitaste este cambio, ignora este correo. Tu contraseña no será modificada.</p>
                    <p style="color:#9ca3af;font-size:0.78rem;word-break:break-all">Enlace: %s</p>
                  </div>
                  <p style="text-align:center;color:#9ca3af;font-size:0.78rem;margin-top:16px">EcoMonitor — Gobierno Local · Valle de Aburrá</p>
                </div>
                """.formatted(user.getUsername(), resetUrl, resetUrl);
        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail must be configured"));
            helper.setTo(Objects.requireNonNull(to, "to email must be provided"));
            helper.setSubject(Objects.requireNonNull(subject, "email subject must be provided"));
            helper.setText(Objects.requireNonNull(htmlBody, "email body must be provided"), true);
            mailSender.send(msg);
        } catch (Exception e) {
            // fallo de email no debe interrumpir el flujo principal
        }
    }
}
