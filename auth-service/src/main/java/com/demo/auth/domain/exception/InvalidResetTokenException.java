package com.demo.auth.domain.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("El token de recuperación es inválido o ha expirado.");
    }
}
