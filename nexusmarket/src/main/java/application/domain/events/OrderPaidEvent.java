package application.domain.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published when an order payment is confirmed.
 */
public record OrderPaidEvent(
        UUID orderId,
        UUID buyerId,
        String paymentReference,
        Instant timestamp
) {
    public OrderPaidEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        paymentReference = paymentReference != null ? paymentReference : "PAYMENT-" + orderId;
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public OrderPaidEvent(UUID orderId, UUID buyerId, String paymentReference) {
        this(orderId, buyerId, paymentReference, Instant.now());
    }

    public UUID getOrderId() { return orderId; }
    public UUID getBuyerId() { return buyerId; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getTimestamp() { return timestamp; }
}
