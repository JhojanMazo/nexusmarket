package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class ValidateSellerOwnershipService {

    public void validateOwnership(UUID sellerId, UUID productSellerId) {
        Objects.requireNonNull(sellerId, "Seller ID must not be null");
        Objects.requireNonNull(productSellerId, "Product seller ID must not be null");
        if (!sellerId.equals(productSellerId)) {
            throw new UnauthorizedOperationException("Product does not belong to seller: " + sellerId);
        }
    }
}
