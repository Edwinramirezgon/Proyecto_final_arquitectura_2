package com.demo.auth.infrastructure.adapter.in;

import com.demo.auth.application.port.in.*;
import com.demo.auth.domain.exception.InvalidCredentialsException;
import com.demo.auth.domain.exception.InvalidResetTokenException;
import com.demo.auth.domain.exception.UserAlreadyExistsException;
import com.demo.auth.infrastructure.adapter.in.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final RegisterUseCase            registerUseCase;
    private final LoginUseCase               loginUseCase;
    private final RefreshTokenUseCase        refreshTokenUseCase;
    private final ChangePasswordUseCase      changePasswordUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase       resetPasswordUseCase;
    private final LogoutUseCase              logoutUseCase;
    private final ValidateTokenUseCase       validateTokenUseCase;
    private final GetCurrentUserUseCase      getCurrentUserUseCase;

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          ChangePasswordUseCase changePasswordUseCase,
                          RequestPasswordResetUseCase requestPasswordResetUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          LogoutUseCase logoutUseCase,
                          ValidateTokenUseCase validateTokenUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase) {
        this.registerUseCase             = registerUseCase;
        this.loginUseCase                = loginUseCase;
        this.refreshTokenUseCase         = refreshTokenUseCase;
        this.changePasswordUseCase       = changePasswordUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase        = resetPasswordUseCase;
        this.logoutUseCase               = logoutUseCase;
        this.validateTokenUseCase        = validateTokenUseCase;
        this.getCurrentUserUseCase       = getCurrentUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            var user = registerUseCase.register(request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginUseCase.LoginResult result = loginUseCase.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(
                    result.accessToken(), result.refreshToken(),
                    result.username(), result.email(), result.role()));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            RefreshTokenUseCase.RefreshResult result = refreshTokenUseCase.refresh(request.getRefreshToken());
            return ResponseEntity.ok(Map.of(
                    "token",        result.accessToken(),
                    "refreshToken", result.refreshToken()));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            logoutUseCase.logout(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (validateTokenUseCase.validate(token))
                return ResponseEntity.ok().build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            GetCurrentUserUseCase.CurrentUser user = getCurrentUserUseCase.getCurrentUser(token);
            return ResponseEntity.ok(Map.of(
                    "username", user.username(),
                    "role",     user.role()));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /** Cambio de contraseña — requiere contraseña actual. */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody Map<String, String> body) {
        try {
            String token = authHeader.replace("Bearer ", "");
            GetCurrentUserUseCase.CurrentUser user = getCurrentUserUseCase.getCurrentUser(token);
            changePasswordUseCase.change(user.username(), body.get("currentPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "La contraseña actual es incorrecta."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /** Solicitud de recuperación — envía email con enlace. Siempre responde 200 para evitar enumeración. */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        requestPasswordResetUseCase.request(body.get("email"));
        return ResponseEntity.ok(Map.of("mensaje",
                "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."));
    }

    /** Restablecimiento con token del email. */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            resetPasswordUseCase.reset(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida correctamente."));
        } catch (InvalidResetTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
