package com.nexusmarket.domain.valueobject;

import com.nexusmarket.domain.model.Product;

import java.util.Objects;

/**
 * Value object: a line item of an Order or ShoppingCart.
 * Immutable; preserves the unit price in effect at the time of selection.
 */
public final class OrderItem {

    private final Product product;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(Product product, int quantity, Money unitPrice) {
        if (product == null) {
            throw new IllegalArgumentException("The order item's product is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("The quantity must be greater than zero.");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("The unit price is required.");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money subtotal() {
        return new Money(
                unitPrice.getAmount().multiply(java.math.BigDecimal.valueOf(quantity)),
                unitPrice.getCurrency()
        );
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return quantity == that.quantity
                && product.equals(that.product)
                && unitPrice.equals(that.unitPrice);
    }

    @Override
    public int hashCode() { return Objects.hash(product, quantity, unitPrice); }
}
