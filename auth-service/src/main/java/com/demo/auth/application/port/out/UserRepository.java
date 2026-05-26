package com.demo.auth.application.port.out;

import com.demo.auth.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    User             save(User user);
    Optional<User>   findByUsername(String username);
    Optional<User>   findByEmail(String email);
    boolean          existsByUsername(String username);
}
