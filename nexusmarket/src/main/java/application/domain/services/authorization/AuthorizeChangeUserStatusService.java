package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import java.util.Objects;
import java.util.UUID;
public class AuthorizeChangeUserStatusService {

    public void validateStatusChange(User actor, UUID targetUserId, UserStatus newStatus) {
        Objects.requireNonNull(actor, "Actor user must not be null");
        Objects.requireNonNull(newStatus, "New status must not be null");
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new UnauthorizedOperationException("Only administrators can modify user status");
        }
        if (actor.getUserId().equals(targetUserId) && newStatus != UserStatus.ACTIVE) {
            throw new UnauthorizedOperationException("Administrators cannot deactivate or suspend their own account");
        }
    }
}
