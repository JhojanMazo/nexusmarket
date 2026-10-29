package application.domain.events;

import application.domain.valueobjects.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published when an order is cancelled (e.g. due to payment TTL expiration)
 * so reserved stock release and notifications can be tracked and dispatched.
 */
public record OrderCancelledEvent(
        UUID orderId,
        String reason,
        List<OrderItem> items,
        Instant timestamp
) {
    public OrderCancelledEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        reason = (reason != null && !reason.isBlank()) ? reason : "Orden cancelada por expiración de tiempo de pago (TTL)";
        items = items != null ? List.copyOf(items) : List.of();
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public OrderCancelledEvent(UUID orderId, String reason, List<OrderItem> items) {
        this(orderId, reason, items, Instant.now());
    }

    public UUID getOrderId() { return orderId; }
    public String getReason() { return reason; }
    public String motivo() { return reason; }
    public String getMotivo() { return reason; }
    public List<OrderItem> getItems() { return items; }
    public Instant getTimestamp() { return timestamp; }
}
