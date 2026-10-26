package application.adapters.security.dtos;

import java.util.UUID;

/**
 * Data transfer object representing extracted JWT claims.
 */
public record JwtClaimsDTO(
        UUID userId,
        String email,
        String role,
        long issuedAt,
        long expiresAt
) {
}
