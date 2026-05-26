package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.ValidateTokenUseCase;
import com.demo.auth.application.port.out.TokenPort;
import org.springframework.stereotype.Service;

@Service
public class ValidateTokenService implements ValidateTokenUseCase {

    private final TokenPort tokenPort;

    public ValidateTokenService(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    public boolean validate(String token) {
        try {
            String username = tokenPort.extractUsername(token);
            return tokenPort.validateAccessToken(token) && tokenPort.isAccessTokenActive(username, token);
        } catch (Exception e) {
            return false;
        }
    }
}
