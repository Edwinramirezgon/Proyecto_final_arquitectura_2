package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.GetCurrentUserUseCase;
import com.demo.auth.application.port.out.TokenPort;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final TokenPort tokenPort;

    public GetCurrentUserService(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    public CurrentUser getCurrentUser(String token) {
        String username = tokenPort.extractUsername(token);
        if (!tokenPort.isAccessTokenActive(username, token)) {
            throw new InvalidCredentialsException("Token inválido o expirado");
        }
        String role = tokenPort.extractRole(token);
        return new CurrentUser(username, role);
    }
}
