package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ValidateSupervisorAuthorizationService {

    public void validateSupervisorRole(User actor) {
        Objects.requireNonNull(actor, "Actor must not be null");
        if (actor.getRole() != UserRole.SUPERVISOR && actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new UnauthorizedOperationException("Supervisor privileges required. Current role: " + actor.getRole());
        }
    }
}
