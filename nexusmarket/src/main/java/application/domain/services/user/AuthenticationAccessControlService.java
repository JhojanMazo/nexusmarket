package application.domain.services.user;

import application.domain.enums.UserStatus;
import application.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 1.2 Manages operational checks for account authentication.
 */
@Service
public class AuthenticationAccessControlService {

    public void validateOperationalAccess(User user) {
        Objects.requireNonNull(user, "User must not be null");
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("Acceso denegado: la cuenta se encuentra BLOQUEADA");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("Acceso denegado: la cuenta se encuentra INACTIVA");
        }
    }
}
