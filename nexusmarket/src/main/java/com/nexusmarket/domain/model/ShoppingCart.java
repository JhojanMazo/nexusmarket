package com.nexusmarket.domain.model;

import com.nexusmarket.domain.valueobject.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingCart {

    private final String cartId;
    private final Buyer buyer;
    private final List<OrderItem> items = new ArrayList<>();

    public ShoppingCart(String cartId, Buyer buyer) {
        this.cartId = requireNotBlank(cartId, "cartId");
        this.buyer = Objects.requireNonNull(buyer, "buyer is required");
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public void addItem(OrderItem item) {
        items.add(Objects.requireNonNull(item, "item is required"));
    }

    public void clear() {
        items.clear();
    }

    public String getCartId() { return cartId; }
    public Buyer getBuyer() { return buyer; }
    public List<OrderItem> getItems() { return List.copyOf(items); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingCart)) return false;
        return cartId.equals(((ShoppingCart) o).cartId);
    }

    @Override
    public int hashCode() { return Objects.hash(cartId); }
}
