package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import java.util.Objects;
public class AuthorizeUserRegistrationService {

    public void validateAdminCanRegisterUser(User actor, UserRole targetRole) {
        Objects.requireNonNull(actor, "Actor user must not be null");
        Objects.requireNonNull(targetRole, "Target role must not be null");
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new UnauthorizedOperationException("Only administrators can register internal users or staff. Current role: " + actor.getRole());
        }
    }
}
