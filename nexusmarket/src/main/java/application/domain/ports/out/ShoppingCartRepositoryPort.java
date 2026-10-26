package application.domain.ports.out;

import application.domain.models.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for ShoppingCart aggregates.
 */
public interface ShoppingCartRepositoryPort {
    ShoppingCart save(ShoppingCart cart);
    Optional<ShoppingCart> findById(UUID cartId);
    Optional<ShoppingCart> findByBuyerId(UUID buyerId);
    void deleteById(UUID cartId);
}
