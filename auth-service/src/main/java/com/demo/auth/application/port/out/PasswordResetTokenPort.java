package com.demo.auth.application.port.out;

import java.util.Optional;

public interface PasswordResetTokenPort {
    /** Genera un token único, lo almacena en Redis con TTL de 15 minutos y lo devuelve. */
    String  generate(String username);

    /** Devuelve el username asociado al token si es válido y no ha expirado. */
    Optional<String> validate(String token);

    /** Invalida el token tras su uso. */
    void    revoke(String token);
}
