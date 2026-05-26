package com.demo.auth.infrastructure.adapter.out;

import com.demo.auth.application.port.out.PasswordResetTokenPort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisPasswordResetTokenAdapter implements PasswordResetTokenPort {

    private static final String PREFIX  = "pwd_reset:";
    private static final long   TTL_MIN = 15;

    private final StringRedisTemplate redis;

    public RedisPasswordResetTokenAdapter(StringRedisTemplate redis) {
        this.redis = Objects.requireNonNull(redis, "redis must be provided");
    }

    @Override
    public String generate(String username) {
        String user = Objects.requireNonNull(username, "username must be provided");
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(PREFIX + token, user, TTL_MIN, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public Optional<String> validate(String token) {
        String value = redis.opsForValue().get(PREFIX + Objects.requireNonNull(token, "token must be provided"));
        return Optional.ofNullable(value);
    }

    @Override
    public void revoke(String token) {
        redis.delete(PREFIX + Objects.requireNonNull(token, "token must be provided"));
    }
}
