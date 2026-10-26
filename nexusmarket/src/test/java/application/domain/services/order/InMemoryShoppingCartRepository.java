package application.domain.services.order;

import application.domain.models.ShoppingCart;
import application.domain.ports.out.ShoppingCartRepositoryPort;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryShoppingCartRepository implements ShoppingCartRepositoryPort {

    private final Map<UUID, ShoppingCart> storage = new ConcurrentHashMap<>();

    @Override
    public ShoppingCart save(ShoppingCart cart) {
        storage.put(cart.getCartId(), cart);
        return cart;
    }

    @Override
    public Optional<ShoppingCart> findById(UUID cartId) {
        return Optional.ofNullable(storage.get(cartId));
    }

    @Override
    public Optional<ShoppingCart> findByBuyerId(UUID buyerId) {
        return storage.values().stream()
                .filter(c -> c.getBuyer() != null && c.getBuyer().getUserId().equals(buyerId))
                .findFirst();
    }

    @Override
    public void deleteById(UUID cartId) {
        storage.remove(cartId);
    }

    public void clear() {
        storage.clear();
    }
}
