package com.demo.auth.application.port.out;

import com.demo.auth.domain.model.User;

public interface EmailNotifierPort {
    void sendWelcome(User user, String rawPassword);
    void sendPasswordChanged(User user);
    void sendPasswordResetLink(User user, String resetToken);
}
