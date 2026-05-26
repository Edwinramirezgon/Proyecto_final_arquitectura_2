package com.demo.auth.application.port.in;

public interface ResetPasswordUseCase {
    void reset(String token, String newPassword);
}
