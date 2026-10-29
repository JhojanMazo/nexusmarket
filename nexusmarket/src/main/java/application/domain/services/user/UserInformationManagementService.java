package application.domain.services.user;

import application.domain.models.User;
import application.domain.valueobjects.Email;
import java.util.Objects;

/**
 * 1.1 Handles profile management and coordinate user profile updates.
 */
public class UserInformationManagementService {

    public User updateUserInfo(User user, String newFullName, Email newEmail) {
        Objects.requireNonNull(user, "User must not be null");
        if (newFullName != null && !newFullName.isBlank()) {
            user.setFullName(newFullName.trim());
        }
        if (newEmail != null) {
            user.setEmail(newEmail);
        }
        return user;
    }
}
