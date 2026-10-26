package application.adapters.security.dtos;

import java.util.UUID;

/**
 * Principal representation of an authenticated user in the security context.
 */
public record AuthenticatedUserPrincipal(
        UUID userId,
        String email,
        String role
) {
}
