package com.demo.auth.application.port.in;

import com.demo.auth.domain.model.User;

public interface RegisterUseCase {
    User register(String email);
}
