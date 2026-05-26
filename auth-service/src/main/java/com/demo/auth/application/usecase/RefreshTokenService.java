package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.RefreshTokenUseCase;
import com.demo.auth.application.port.out.TokenPort;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import com.demo.auth.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenPort    tokenPort;
    private final UserRepository userRepository;

    public RefreshTokenService(TokenPort tokenPort, UserRepository userRepository) {
        this.tokenPort      = tokenPort;
        this.userRepository = userRepository;
    }

    @Override
    public RefreshResult refresh(String refreshToken) {
        if (!tokenPort.validateRefreshToken(refreshToken))
            throw new InvalidCredentialsException();

        String username = tokenPort.extractUsername(refreshToken);

        if (!tokenPort.isRefreshTokenActive(username, refreshToken))
            throw new InvalidCredentialsException();

        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        String newAccess  = tokenPort.generateAccessToken(user.getUsername(), user.getRole());
        String newRefresh = tokenPort.generateRefreshToken(user.getUsername());

        // rotación: reemplaza ambos tokens — el refresh anterior queda inválido
        tokenPort.storeTokens(user.getUsername(), newAccess, newRefresh);

        return new RefreshResult(newAccess, newRefresh);
    }
}
