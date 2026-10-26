package application.domain.models;

import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Shopping cart belonging to a {@link Buyer}. Holds a mutable collection of {@link OrderItem}.
 *
 * - Items can be added or removed.
 * - The internal list is not exposed for direct modification.
 * - {@code subtotal()} returns the sum of all item subtotals.
 * - An empty cart has a subtotal of zero (USD).
 */
public class ShoppingCart {
    private final UUID cartId;
    private final Buyer buyer;
    private final List<OrderItem> items = new ArrayList<>();

    public ShoppingCart(UUID cartId, Buyer buyer) {
        this.cartId = Objects.requireNonNull(cartId);
        this.buyer = Objects.requireNonNull(buyer);
    }

    public UUID getCartId() {
        return cartId;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    /** Adds an {@link OrderItem} to the cart. */
    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "OrderItem must not be null");
        items.add(item);
    }

    /** Removes the first occurrence of the given {@link OrderItem}. */
    public void removeItem(OrderItem item) {
        Objects.requireNonNull(item, "OrderItem must not be null");
        items.remove(item);
    }

    /** Returns an immutable view of the items in the cart. */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Calculates the subtotal of the cart (sum of each item's {@code subtotal()}).
     * An empty cart returns zero Money in USD.
     */
    public Money subtotal() {
        if (items.isEmpty()) {
            return new Money(BigDecimal.ZERO, Currency.getInstance("USD"));
        }
        Money total = items.get(0).subtotal();
        for (int i = 1; i < items.size(); i++) {
            total = total.add(items.get(i).subtotal());
        }
        return total;
    }
}
