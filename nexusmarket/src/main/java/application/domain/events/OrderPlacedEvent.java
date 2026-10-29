package application.domain.events;

import application.domain.models.Order;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published when an order is placed from a shopping cart.
 */
public record OrderPlacedEvent(
        UUID orderId,
        UUID buyerId,
        Money totalAmount,
        List<OrderItem> items,
        Instant timestamp
) {
    public OrderPlacedEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        items = items != null ? List.copyOf(items) : List.of();
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public OrderPlacedEvent(UUID orderId, UUID buyerId, Money totalAmount, List<OrderItem> items) {
        this(orderId, buyerId, totalAmount, items, Instant.now());
    }

    public static OrderPlacedEvent from(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        return new OrderPlacedEvent(
                order.getOrderId(),
                order.getBuyer().getUserId(),
                order.total(),
                order.getItems(),
                Instant.now()
        );
    }

    public UUID getOrderId() { return orderId; }
    public UUID getBuyerId() { return buyerId; }
    public Money getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public Instant getTimestamp() { return timestamp; }
}
