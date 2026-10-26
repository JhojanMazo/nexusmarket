package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthorizeOrderOperationService {

    public void validateCanCreateOrModifyOrder(User actor, UUID orderBuyerId) {
        Objects.requireNonNull(actor, "Actor must not be null");
        if (actor.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }
        if (actor.getRole() != UserRole.BUYER || !actor.getUserId().equals(orderBuyerId)) {
            throw new UnauthorizedOperationException("Only the order owner or administrator can perform this order operation");
        }
    }
}
