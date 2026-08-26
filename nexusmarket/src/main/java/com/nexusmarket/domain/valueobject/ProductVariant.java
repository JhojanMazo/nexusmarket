package com.nexusmarket.domain.valueobject;

import java.util.Objects;

/**
 * Value object: a variation dimension of a Product (color, size, model, etc.).
 * Has no identity of its own outside the product that contains it.
 */
public final class ProductVariant {

    private final String attribute; // e.g. "Color"
    private final String value;     // e.g. "Red"

    public ProductVariant(String attribute, String value) {
        if (attribute == null || attribute.isBlank()) {
            throw new IllegalArgumentException("The variant attribute cannot be blank.");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The variant value cannot be blank.");
        }
        this.attribute = attribute;
        this.value = value;
    }

    public String getAttribute() { return attribute; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant)) return false;
        ProductVariant that = (ProductVariant) o;
        return attribute.equalsIgnoreCase(that.attribute) && value.equalsIgnoreCase(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(attribute.toLowerCase(), value.toLowerCase()); }

    @Override
    public String toString() { return attribute + ": " + value; }
}
