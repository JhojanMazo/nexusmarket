package application.adapters.security;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.User;
import application.domain.ports.out.AuthorizationPort;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter bridging user credentials and role evaluation to enforce domain authorization policies.
 */
@Component
public class NexusMarketUserDetailsAdapter implements AuthorizationPort {

    private final UserRepositoryPort userRepositoryPort;

    public NexusMarketUserDetailsAdapter(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public boolean isAuthorized(UUID userId, UserRole requiredRole) {
        if (userId == null || requiredRole == null) {
            return false;
        }
        Optional<User> userOpt = userRepositoryPort.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }
        return user.getRole() == requiredRole || user.getRole() == UserRole.ADMINISTRATOR;
    }

    @Override
    public boolean hasPermission(UUID userId, String permission) {
        if (userId == null || permission == null) {
            return false;
        }
        Optional<User> userOpt = userRepositoryPort.findById(userId);
        return userOpt.isPresent() && userOpt.get().getStatus() == UserStatus.ACTIVE;
    }
}
