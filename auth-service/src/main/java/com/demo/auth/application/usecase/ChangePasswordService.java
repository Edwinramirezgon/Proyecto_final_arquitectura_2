package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.ChangePasswordUseCase;
import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import com.demo.auth.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final EmailNotifierPort emailNotifier;

    public ChangePasswordService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailNotifierPort emailNotifier) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotifier   = emailNotifier;
    }

    @Override
    public void change(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            throw new InvalidCredentialsException();

        User.validatePasswordPolicy(newPassword);
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        emailNotifier.sendPasswordChanged(user);
    }
}
