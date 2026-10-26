package application.domain.models;

import application.domain.enums.UserRole;

import java.util.UUID;

/**
 * Domain model representing the result of a successful user authentication.
 */
public record AuthenticationResult(
        UUID userId,
        String fullName,
        String email,
        UserRole role,
        String token,
        long expiresIn
) {
    public AuthenticationResult(UUID userId, String fullName, String email, UserRole role, String token) {
        this(userId, fullName, email, role, token, 86400L);
    }
}
