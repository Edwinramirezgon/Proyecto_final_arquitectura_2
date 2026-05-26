package com.demo.auth.application.usecase;

import com.demo.auth.application.port.in.RegisterUseCase;
import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.exception.UserAlreadyExistsException;
import com.demo.auth.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class RegisterService implements RegisterUseCase {

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SPECIAL = "!@#$%&*?";
    private static final String ALL     = UPPER + LOWER + DIGITS + SPECIAL;

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final EmailNotifierPort emailNotifier;

    public RegisterService(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmailNotifierPort emailNotifier) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotifier   = emailNotifier;
    }

    @Override
    public User register(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio.");

        // username = parte del email antes del @
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        if (userRepository.existsByUsername(username))
            throw new UserAlreadyExistsException(username);

        if (userRepository.findByEmail(email).isPresent())
            throw new UserAlreadyExistsException(username);

        String rawPassword = generatePassword();
        User user  = User.create(username, email, passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);
        emailNotifier.sendWelcome(saved, rawPassword);
        return saved;
    }

    /**
     * Genera una contraseña de 12 caracteres garantizando al menos
     * 1 mayúscula, 1 minúscula, 1 dígito y 1 especial (4 tipos).
     */
    private String generatePassword() {
        SecureRandom rng  = new SecureRandom();
        char[] password   = new char[12];

        // garantizar un carácter de cada tipo
        password[0] = UPPER.charAt(rng.nextInt(UPPER.length()));
        password[1] = LOWER.charAt(rng.nextInt(LOWER.length()));
        password[2] = DIGITS.charAt(rng.nextInt(DIGITS.length()));
        password[3] = SPECIAL.charAt(rng.nextInt(SPECIAL.length()));

        for (int i = 4; i < 12; i++)
            password[i] = ALL.charAt(rng.nextInt(ALL.length()));

        // mezclar para que los tipos no estén siempre al inicio
        for (int i = 11; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            char tmp = password[i]; password[i] = password[j]; password[j] = tmp;
        }
        return new String(password);
    }
}
