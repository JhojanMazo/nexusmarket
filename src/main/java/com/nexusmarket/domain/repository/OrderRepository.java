package com.nexusmarket.domain.repository;

import com.nexusmarket.domain.model.Order;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Order aggregates.
 *
 * Implementation will be provided by the persistence layer (e.g., JPA, in‑memory).
 */
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
}
