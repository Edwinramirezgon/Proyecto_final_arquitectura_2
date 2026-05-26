package com.demo.auth.domain.model;

public class User {

    private Long   id;
    private String username;
    private String email;
    private String passwordHash;
    private String role;

    private User() {}

    public static User reconstitute(Long id, String username, String email,
                                    String passwordHash, String role) {
        User u         = new User();
        u.id           = id;
        u.username     = username;
        u.email        = email;
        u.passwordHash = passwordHash;
        u.role         = role;
        return u;
    }

    public static User create(String username, String email, String passwordHash) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio.");
        if (passwordHash == null || passwordHash.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");

        User u         = new User();
        u.username     = username;
        u.email        = email;
        u.passwordHash = passwordHash;
        u.role         = "USER";
        return u;
    }

    /**
     * Valida que la contraseña en texto plano cumpla la política:
     * mínimo 8 caracteres y al menos 3 de los 4 tipos:
     * mayúscula, minúscula, dígito, carácter especial.
     */
    public static void validatePasswordPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8)
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres.");

        int types = 0;
        if (rawPassword.chars().anyMatch(Character::isUpperCase))  types++;
        if (rawPassword.chars().anyMatch(Character::isLowerCase))  types++;
        if (rawPassword.chars().anyMatch(Character::isDigit))      types++;
        if (rawPassword.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) types++;

        if (types < 3)
            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos 3 tipos de caracteres: " +
                    "mayúsculas, minúsculas, números o caracteres especiales.");
    }

    public void changePassword(String newHash) {
        if (newHash == null || newHash.isBlank())
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía.");
        this.passwordHash = newHash;
    }

    public Long   getId()           { return id; }
    public void   setId(Long id)    { this.id = id; }
    public String getUsername()     { return username; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole()         { return role; }
    public void   setRole(String r) { this.role = r; }
}
