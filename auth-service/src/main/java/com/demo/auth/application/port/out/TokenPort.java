package com.demo.auth.application.port.out;

public interface TokenPort {

    String  generateAccessToken(String username, String role);
    String  generateRefreshToken(String username);

    boolean validateAccessToken(String token);
    boolean validateRefreshToken(String token);

    String  extractUsername(String token);
    String  extractRole(String token);

    /** Almacena el par access+refresh en Redis con sus TTLs respectivos. */
    void    storeTokens(String username, String accessToken, String refreshToken);

    /** Invalida ambos tokens del usuario (logout). */
    void    revokeTokens(String username);

    /** Verifica que el access token esté activo en Redis (no revocado). */
    boolean isAccessTokenActive(String username, String token);

    /** Verifica que el refresh token esté activo en Redis. */
    boolean isRefreshTokenActive(String username, String token);
}
