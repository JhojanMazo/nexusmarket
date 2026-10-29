package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import java.util.Objects;
public class AuthorizeProductOperationService {

    public void validateCanManageProduct(User actor) {
        Objects.requireNonNull(actor, "Actor must not be null");
        if (actor.getRole() != UserRole.SELLER && actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new UnauthorizedOperationException("Only sellers and administrators can manage products. Current role: " + actor.getRole());
        }
    }
}
