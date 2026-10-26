package application.domain.valueobjects;

import java.util.Objects;

/**
 * Immutable value object for a product variant.
 * Both attribute and value must be non‑blank.
 */
public final class ProductVariant {
    private final String attribute;
    private final String value;

    public ProductVariant(String attribute, String value) {
        if (isBlank(attribute) || isBlank(value)) {
            throw new IllegalArgumentException("ProductVariant fields must not be blank");
        }
        this.attribute = attribute.trim();
        this.value = value.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public String attribute() { return attribute; }
    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant)) return false;
        ProductVariant that = (ProductVariant) o;
        return attribute.equalsIgnoreCase(that.attribute) && value.equalsIgnoreCase(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute.toLowerCase(), value.toLowerCase());
    }

    @Override
    public String toString() {
        return attribute + ": " + value;
    }
}
