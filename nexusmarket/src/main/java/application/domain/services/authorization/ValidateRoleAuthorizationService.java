package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ValidateRoleAuthorizationService {

    public void validateRole(User user, UserRole requiredRole) {
        Objects.requireNonNull(user, "User must not be null");
        Objects.requireNonNull(requiredRole, "Required role must not be null");
        if (user.getRole() != requiredRole) {
            throw new UnauthorizedOperationException("User does not have required role: " + requiredRole + ". Current: " + user.getRole());
        }
    }
}
