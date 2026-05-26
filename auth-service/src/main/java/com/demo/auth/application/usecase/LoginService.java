package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.LoginUseCase;
import com.demo.auth.application.port.out.TokenPort;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import com.demo.auth.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenPort       tokenPort;

    public LoginService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        TokenPort tokenPort) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenPort       = tokenPort;
    }

    @Override
    public LoginUseCase.LoginResult login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()))
            throw new InvalidCredentialsException();

        String accessToken  = tokenPort.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = tokenPort.generateRefreshToken(user.getUsername());
        tokenPort.storeTokens(user.getUsername(), accessToken, refreshToken);

        return new LoginUseCase.LoginResult(accessToken, refreshToken, user.getUsername(), user.getEmail(), user.getRole());
    }
}
