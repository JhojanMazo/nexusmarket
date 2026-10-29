package application.domain.services.user;

import application.domain.enums.UserStatus;
import application.domain.models.User;
import java.util.Objects;

/**
 * 1.4 Controls user account operational state lifecycle.
 */
public class UserOperationalStateService {

    public void blockUser(User user, String reason) {
        Objects.requireNonNull(user, "User must not be null");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Debe especificarse el motivo del bloqueo");
        }
        user.setStatus(UserStatus.BLOCKED);
    }

    public void activateUser(User user) {
        Objects.requireNonNull(user, "User must not be null");
        user.setStatus(UserStatus.ACTIVE);
    }
}
