package application.domain.ports.out;

import application.domain.enums.UserRole;

import java.util.UUID;

/**
 * Outbound port for evaluating security and business authorization policies.
 */
public interface AuthorizationPort {
    boolean isAuthorized(UUID userId, UserRole requiredRole);
    boolean hasPermission(UUID userId, String permission);
}
