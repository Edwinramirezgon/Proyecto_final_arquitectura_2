package com.demo.alert.application.usecase;

import com.demo.alert.application.port.in.ProcessAuthNotificationUseCase;
import com.demo.alert.application.port.out.EmergencyNotifierPort;
import org.springframework.stereotype.Service;

@Service
public class AuthNotificationService implements ProcessAuthNotificationUseCase {

    private final EmergencyNotifierPort notifier;

    public AuthNotificationService(EmergencyNotifierPort notifier) {
        this.notifier = notifier;
    }

    @Override
    public void process(String type, String email, String username,
                        String rawPassword, String resetToken, String appBaseUrl) {
        try {
            switch (type) {
                case "WELCOME"          -> notifier.sendWelcome(email, username, rawPassword, appBaseUrl);
                case "PASSWORD_CHANGED" -> notifier.sendPasswordChanged(email, username, appBaseUrl);
                case "PASSWORD_RESET"   -> notifier.sendPasswordResetLink(email, username, resetToken, appBaseUrl);
            }
        } catch (Exception ignored) {
            // fallo de email no interrumpe el flujo
        }
    }
}
