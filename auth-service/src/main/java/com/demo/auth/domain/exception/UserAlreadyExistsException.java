package com.demo.auth.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super(String.format("El usuario '%s' ya existe.", username));
    }
}
