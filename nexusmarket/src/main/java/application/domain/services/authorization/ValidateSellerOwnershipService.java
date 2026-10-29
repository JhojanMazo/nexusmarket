package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import java.util.Objects;
import java.util.UUID;
public class ValidateSellerOwnershipService {

    public void validateOwnership(UUID sellerId, UUID productSellerId) {
        Objects.requireNonNull(sellerId, "Seller ID must not be null");
        Objects.requireNonNull(productSellerId, "Product seller ID must not be null");
        if (!sellerId.equals(productSellerId)) {
            throw new UnauthorizedOperationException("Product does not belong to seller: " + sellerId);
        }
    }
}
