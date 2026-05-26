package com.demo.auth.infrastructure.config;

import com.demo.auth.infrastructure.adapter.out.persistence.JpaUserRepository;
import com.demo.auth.infrastructure.adapter.out.persistence.UserEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private final JpaUserRepository jpa;
    private final PasswordEncoder   passwordEncoder;

    public DataSeeder(JpaUserRepository jpa, PasswordEncoder passwordEncoder) {
        this.jpa            = jpa;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!jpa.existsByUsername("admin")) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setEmail("admin@ecomonitor.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setRole("ADMIN");
            jpa.save(admin);
        }
    }
}
