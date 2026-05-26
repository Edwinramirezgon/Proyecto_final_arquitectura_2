package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.ResetPasswordUseCase;
import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.application.port.out.PasswordResetTokenPort;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import com.demo.auth.domain.exception.InvalidResetTokenException;
import com.demo.auth.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepository         userRepository;
    private final PasswordEncoder        passwordEncoder;
    private final PasswordResetTokenPort resetTokenPort;
    private final EmailNotifierPort      emailNotifier;

    public ResetPasswordService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetTokenPort resetTokenPort,
                                EmailNotifierPort emailNotifier) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetTokenPort  = resetTokenPort;
        this.emailNotifier   = emailNotifier;
    }

    @Override
    public void reset(String token, String newPassword) {
        String username = resetTokenPort.validate(token)
                .orElseThrow(InvalidResetTokenException::new);

        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        User.validatePasswordPolicy(newPassword);
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenPort.revoke(token);
        emailNotifier.sendPasswordChanged(user);
    }
}
