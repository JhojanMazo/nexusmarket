package application.domain.valueobjects;

import application.domain.models.Product;
import java.util.Objects;

/**
 * Immutable value object representing an item in an order or shopping cart.
 * Contains a reference to a {@link Product}, a positive quantity and a unit price.
 * Provides {@code subtotal()} = quantity * unitPrice.
 */
public final class OrderItem {
    private final Product product;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(Product product, int quantity, Money unitPrice) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price must not be null");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Product product() { return product; }
    public int quantity() { return quantity; }
    public Money unitPrice() { return unitPrice; }

    public Money subtotal() {
        // subtotal = unitPrice * quantity
        java.math.BigDecimal totalAmount = unitPrice.amount().multiply(java.math.BigDecimal.valueOf(quantity));
        return new Money(totalAmount, unitPrice.currency());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return quantity == that.quantity &&
                product.equals(that.product) &&
                unitPrice.equals(that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
