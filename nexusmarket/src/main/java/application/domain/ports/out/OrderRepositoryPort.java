package application.domain.ports.out;

import application.domain.models.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Order aggregates.
 */
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    List<Order> findByBuyerId(UUID buyerId);
    List<Order> findAll();
}
