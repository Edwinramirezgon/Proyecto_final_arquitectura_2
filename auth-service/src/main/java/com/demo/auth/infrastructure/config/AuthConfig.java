package com.demo.auth.infrastructure.config;

import com.demo.auth.application.port.out.TokenPort;
import com.demo.auth.application.port.out.UserRepository;
import com.demo.auth.application.port.out.EmailNotifierPort;
import com.demo.auth.application.port.out.PasswordResetTokenPort;
import com.demo.auth.infrastructure.adapter.out.JwtTokenAdapter;
import com.demo.auth.infrastructure.adapter.out.PostgresUserAdapter;
import com.demo.auth.infrastructure.adapter.out.GmailAuthNotifierAdapter;
import com.demo.auth.infrastructure.adapter.out.RedisPasswordResetTokenAdapter;
import com.demo.auth.application.port.out.SubscriptionRepository;
import com.demo.auth.infrastructure.adapter.out.PostgresSubscriptionAdapter;
import com.demo.auth.infrastructure.adapter.out.persistence.JpaZoneSubscriptionRepository;
import com.demo.auth.infrastructure.adapter.out.persistence.JpaUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Configuration
public class AuthConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                Objects.requireNonNull(host, "redis host must be provided"),
                port);
        if (password != null && !password.isBlank()) {
            config.setPassword(Objects.requireNonNull(password, "redis password must be provided"));
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(Objects.requireNonNull(factory, "redis connection factory must be provided"));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public UserRepository userRepository(JpaUserRepository jpa) {
        return new PostgresUserAdapter(jpa);
    }

    @Bean
    public TokenPort tokenPort(@Value("${jwt.secret}") String secret,
                               @Value("${jwt.expiration-ms}") long accessTtlMs,
                               @Value("${jwt.refresh-expiration-ms}") long refreshTtlMs,
                               StringRedisTemplate redisTemplate) {
        return new JwtTokenAdapter(Objects.requireNonNull(secret, "jwt secret must be provided"),
            accessTtlMs,
            refreshTtlMs,
            Objects.requireNonNull(redisTemplate, "redis template must be provided"));
    }

    @Bean
    public SubscriptionRepository subscriptionRepository(JpaZoneSubscriptionRepository jpa) {
        return new PostgresSubscriptionAdapter(jpa);
    }

    @Bean
    public EmailNotifierPort emailNotifierPort(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail,
            @Value("${app.base-url}") String appBaseUrl) {
        return new GmailAuthNotifierAdapter(
            Objects.requireNonNull(mailSender, "mailSender must be provided"),
            Objects.requireNonNull(fromEmail, "fromEmail must be provided"),
            Objects.requireNonNull(appBaseUrl, "appBaseUrl must be provided"));
    }

    @Bean
    public PasswordResetTokenPort passwordResetTokenPort(StringRedisTemplate redisTemplate) {
        return new RedisPasswordResetTokenAdapter(
                Objects.requireNonNull(redisTemplate, "redis template must be provided"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
