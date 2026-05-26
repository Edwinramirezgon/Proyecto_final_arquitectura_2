package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.RequestPasswordResetUseCase;
import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.application.port.out.PasswordResetTokenPort;
import com.demo.auth.application.port.out.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserRepository        userRepository;
    private final PasswordResetTokenPort resetTokenPort;
    private final EmailNotifierPort      emailNotifier;

    public RequestPasswordResetService(UserRepository userRepository,
                                       PasswordResetTokenPort resetTokenPort,
                                       EmailNotifierPort emailNotifier) {
        this.userRepository  = userRepository;
        this.resetTokenPort  = resetTokenPort;
        this.emailNotifier   = emailNotifier;
    }

    /**
     * Si el email no existe no lanzamos error — evita enumeración de usuarios.
     * El llamador siempre recibe la misma respuesta genérica.
     */
    @Override
    public void request(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = resetTokenPort.generate(user.getUsername());
            emailNotifier.sendPasswordResetLink(user, token);
        });
    }
}
