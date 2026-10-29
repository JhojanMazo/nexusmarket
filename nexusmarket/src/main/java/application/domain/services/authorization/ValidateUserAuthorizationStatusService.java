package application.domain.services.authorization;

import application.domain.enums.UserStatus;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import java.util.Objects;
public class ValidateUserAuthorizationStatusService {

    public void validateActiveStatus(User user) {
        Objects.requireNonNull(user, "User must not be null");
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedOperationException("User account is not active. Current status: " + user.getStatus());
        }
    }
}
