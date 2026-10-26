package application.domain.services.order;

import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepositoryPort {

    private final Map<UUID, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        storage.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(storage.get(orderId));
    }

    @Override
    public List<Order> findByBuyerId(UUID buyerId) {
        return storage.values().stream()
                .filter(o -> o.getBuyer() != null && o.getBuyer().getUserId().equals(buyerId))
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}
