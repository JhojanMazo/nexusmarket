package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.OrderItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Formal commercial commitment between a Buyer and Seller(s); the central
 * process of the system.
 * Lifecycle: CART -> PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED_COMPLETED.
 * Validation: a completed order can never be modified, under any circumstance.
 */
public class Order {

    private static final List<OrderStatus> LIFECYCLE_ORDER = List.of(
            OrderStatus.CART,
            OrderStatus.PENDING_PAYMENT,
            OrderStatus.PAID,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED_COMPLETED
    );

    private final String orderId;
    private final Buyer buyer;
    private final List<OrderItem> items = new ArrayList<>();
    private Address deliveryAddress;
    private OrderStatus status;
    private final LocalDate creationDate;

    public Order(String orderId, Buyer buyer, List<OrderItem> items,
                 Address deliveryAddress, LocalDate creationDate) {
        this.orderId = requireNotBlank(orderId, "orderId");
        this.buyer = Objects.requireNonNull(buyer, "buyer is required");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("The order must contain at least one item.");
        }
        this.items.addAll(items);
        this.deliveryAddress = Objects.requireNonNull(deliveryAddress, "deliveryAddress is required");
        this.creationDate = Objects.requireNonNull(creationDate, "creationDate is required");
        this.status = OrderStatus.CART;
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    /** Advances the order to the next lifecycle status, in strict order. */
    public void advanceTo(OrderStatus newStatus) {
        if (status == OrderStatus.DELIVERED_COMPLETED) {
            throw new IllegalStateException("A completed order can never be modified, under any circumstance.");
        }
        int current = LIFECYCLE_ORDER.indexOf(status);
        int target = LIFECYCLE_ORDER.indexOf(newStatus);
        if (target != current + 1) {
            throw new IllegalStateException("Invalid transition from " + status + " to " + newStatus + ".");
        }
        this.status = newStatus;
    }

    public Money total() {
        Money accumulated = null;
        for (OrderItem item : items) {
            Money subtotal = item.subtotal();
            accumulated = (accumulated == null) ? subtotal : accumulated.add(subtotal);
        }
        return accumulated;
    }

    public String getOrderId() { return orderId; }
    public Buyer getBuyer() { return buyer; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public OrderStatus getStatus() { return status; }
    public LocalDate getCreationDate() { return creationDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        return orderId.equals(((Order) o).orderId);
    }

    @Override
    public int hashCode() { return Objects.hash(orderId); }
}
