package com.demo.auth.application.port.in;

public interface RequestPasswordResetUseCase {
    void request(String email);
}
