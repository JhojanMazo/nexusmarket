package application.domain.ports.out;

import java.util.UUID;

/**
 * Outbound port for cryptographic JWT token operations.
 */
public interface JwtServicePort {
    String generateToken(UUID userId, String email, String role);
    boolean validateToken(String token);
    UUID extractUserId(String token);
    String extractEmail(String token);
    String extractRole(String token);
}
