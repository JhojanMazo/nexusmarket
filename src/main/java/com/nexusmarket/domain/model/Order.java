package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.OrderItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an Order.
 *
 * - Created from a {@link ShoppingCart}; items are copied so later cart changes do not affect the order.
 * - Subtotal and total are calculated from the immutable list of {@link OrderItem}s.
 * - Status transitions must follow the exact sequence defined in the spec:
 *   CART → PENDING_PAYMENT → PAID → SHIPPED → DELIVERED_COMPLETED.
 *   Invalid transitions throw {@link IllegalStateException}.
 */
public class Order {
    private final UUID orderId;
    private final Buyer buyer;
    private final List<OrderItem> items; // immutable copy
    private Address deliveryAddress; // may be changed until order is finalized
    private OrderStatus status;
    private final LocalDate creationDate;

    private Order(UUID orderId, Buyer buyer, List<OrderItem> items, Address deliveryAddress) {
        this.orderId = Objects.requireNonNull(orderId);
        this.buyer = Objects.requireNonNull(buyer);
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.deliveryAddress = Objects.requireNonNull(deliveryAddress);
        this.status = OrderStatus.CART;
        this.creationDate = LocalDate.now();
    }

    /** Factory method to create an Order from a ShoppingCart. */
    public static Order createFromCart(ShoppingCart cart, Address deliveryAddress) {
        Objects.requireNonNull(cart, "ShoppingCart must not be null");
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an Order from an empty ShoppingCart");
        }
        return new Order(UUID.randomUUID(), cart.getBuyer(), cart.getItems(), deliveryAddress);
    }

    public UUID getOrderId() { return orderId; }
    public Buyer getBuyer() { return buyer; }
    public List<OrderItem> getItems() { return items; }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(Address address) { this.deliveryAddress = Objects.requireNonNull(address); }
    public OrderStatus getStatus() { return status; }
    public LocalDate getCreationDate() { return creationDate; }

    /** Calculates the subtotal (sum of item subtotals). */
    public Money subtotal() {
        Money total = items.get(0).subtotal();
        for (int i = 1; i < items.size(); i++) {
            total = total.add(items.get(i).subtotal());
        }
        return total;
    }

    /** Total equals subtotal for now (no tax/fees). */
    public Money total() { return subtotal(); }

    /**
     * Advances the order status to the given next status, verifying the allowed transition sequence.
     *
     * @throws IllegalStateException if the transition is not permitted.
     */
    public void advanceTo(OrderStatus next) {
        Objects.requireNonNull(next, "Next status must not be null");
        if (!isValidTransition(this.status, next)) {
            throw new IllegalStateException("Invalid order status transition from " + this.status + " to " + next);
        }
        this.status = next;
    }

    private static boolean isValidTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case CART: return next == OrderStatus.PENDING_PAYMENT;
            case PENDING_PAYMENT: return next == OrderStatus.PAID;
            case PAID: return next == OrderStatus.SHIPPED;
            case SHIPPED: return next == OrderStatus.DELIVERED_COMPLETED;
            default: return false; // DELIVERED_COMPLETED has no further transitions
        }
    }
}
