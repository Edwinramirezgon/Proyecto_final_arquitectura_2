package com.demo.auth.application.port.in;

public interface LoginUseCase {
    LoginResult login(String username, String rawPassword);

    record LoginResult(String accessToken, String refreshToken, String username, String email, String role) {}
}
