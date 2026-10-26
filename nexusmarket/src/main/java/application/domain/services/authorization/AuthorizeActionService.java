package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.ports.out.AuthorizationPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain service providing granular action authorization checks for operations.
 */
@Service
public class AuthorizeActionService {

    private final AuthorizationPort authorizationPort;

    public AuthorizeActionService(AuthorizationPort authorizationPort) {
        this.authorizationPort = authorizationPort;
    }

    public void validateRole(UUID userId, UserRole requiredRole) {
        if (!authorizationPort.isAuthorized(userId, requiredRole)) {
            throw new UnauthorizedOperationException("User " + userId + " does not hold required role: " + requiredRole);
        }
    }

    public void validatePermission(UUID userId, String permission) {
        if (!authorizationPort.hasPermission(userId, permission)) {
            throw new UnauthorizedOperationException("User " + userId + " lacks required permission: " + permission);
        }
    }
}
