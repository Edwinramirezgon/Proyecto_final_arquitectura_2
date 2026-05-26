package com.demo.auth.application.port.in;

public interface ChangePasswordUseCase {
    void change(String username, String currentPassword, String newPassword);
}
