package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 1.3 Enforces role permissions and privileges.
 */
@Service
public class RolePermissionManagementService {

    public void validateActionPermission(User user, UserRole requiredRole) {
        Objects.requireNonNull(user, "User must not be null");
        Objects.requireNonNull(requiredRole, "Required role must not be null");
        if (user.getRole() != requiredRole) {
            throw new SecurityException("Permiso denegado: el usuario no posee el rol requerido " + requiredRole);
        }
    }
}
