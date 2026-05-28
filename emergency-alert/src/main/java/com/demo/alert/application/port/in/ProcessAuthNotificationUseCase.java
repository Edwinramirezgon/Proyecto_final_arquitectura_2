package com.demo.alert.application.port.in;

public interface ProcessAuthNotificationUseCase {
    void process(String type, String email, String username,
                 String rawPassword, String resetToken, String appBaseUrl);
}
