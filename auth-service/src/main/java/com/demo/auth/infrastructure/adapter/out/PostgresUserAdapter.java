package com.demo.auth.infrastructure.adapter.out;

import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.domain.model.User;
import com.demo.auth.infrastructure.adapter.out.persistence.JpaUserRepository;
import com.demo.auth.infrastructure.adapter.out.persistence.UserEntity;

import java.util.Optional;
import java.util.Objects;

public class PostgresUserAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    public PostgresUserAdapter(JpaUserRepository jpa) {
        this.jpa = Objects.requireNonNull(jpa, "jpa repository must be provided");
    }

    @Override
    public User save(User user) {
        UserEntity entity = Objects.requireNonNull(toEntity(user), "user entity must be provided");
        UserEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    private UserEntity toEntity(User user) {
        Objects.requireNonNull(user, "user must be provided");
        UserEntity e = new UserEntity();
        e.setId(user.getId());
        e.setUsername(user.getUsername());
        e.setEmail(user.getEmail());
        e.setPasswordHash(user.getPasswordHash());
        e.setRole(user.getRole());
        return e;
    }

    private User toDomain(UserEntity e) {
        UserEntity entity = Objects.requireNonNull(e, "user entity must be provided");
        return User.reconstitute(entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getPasswordHash(), entity.getRole());
    }
}
