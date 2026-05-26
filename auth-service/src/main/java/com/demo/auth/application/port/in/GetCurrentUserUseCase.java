package com.demo.auth.application.port.in;

public interface GetCurrentUserUseCase {
    record CurrentUser(String username, String role) {}
    
    CurrentUser getCurrentUser(String token);
}
