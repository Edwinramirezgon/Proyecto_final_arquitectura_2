package com.demo.auth.application.port.in;

public interface ValidateTokenUseCase {
    boolean validate(String token);
}
