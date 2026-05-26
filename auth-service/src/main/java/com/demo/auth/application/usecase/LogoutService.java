package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.LogoutUseCase;
import com.demo.auth.application.port.out.TokenPort;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutUseCase {

    private final TokenPort tokenPort;

    public LogoutService(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    public void logout(String token) {
        String username = tokenPort.extractUsername(token);
        tokenPort.revokeTokens(username);
    }
}
