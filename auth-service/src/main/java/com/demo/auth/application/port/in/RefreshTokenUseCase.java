package com.demo.auth.application.port.in;

public interface RefreshTokenUseCase {
    RefreshResult refresh(String refreshToken);

    record RefreshResult(String accessToken, String refreshToken) {}
}
