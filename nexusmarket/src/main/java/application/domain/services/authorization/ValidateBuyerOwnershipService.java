package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class ValidateBuyerOwnershipService {

    public void validateOwnership(UUID buyerId, UUID resourceOwnerId) {
        Objects.requireNonNull(buyerId, "Buyer ID must not be null");
        Objects.requireNonNull(resourceOwnerId, "Resource owner ID must not be null");
        if (!buyerId.equals(resourceOwnerId)) {
            throw new UnauthorizedOperationException("Resource does not belong to buyer: " + buyerId);
        }
    }
}
