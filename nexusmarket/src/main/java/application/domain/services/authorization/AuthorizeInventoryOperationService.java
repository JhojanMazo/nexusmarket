package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import java.util.Objects;
public class AuthorizeInventoryOperationService {

    public void validateCanManageInventory(User actor) {
        Objects.requireNonNull(actor, "Actor must not be null");
        if (actor.getRole() != UserRole.LOGISTICS_OPERATOR && actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new UnauthorizedOperationException("Only logistics operators and administrators can manage inventory. Current role: " + actor.getRole());
        }
    }
}
