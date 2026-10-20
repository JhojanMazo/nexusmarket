package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.ProductVariant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a product in the catalog.
 *
 * This is a minimal implementation sufficient for compilation and subsequent
 * domain logic. Additional attributes (description, stock, etc.) can be added
 * later following the same pattern.
 */
public class Product {
    private final UUID productId;
    private final String name;
    private final ProductVariant variant;
    private final Money price;
    private final ProductStatus status;
    private final ProductType type;

    public Product(UUID productId, String name, ProductVariant variant, Money price,
                   ProductStatus status, ProductType type) {
        this.productId = Objects.requireNonNull(productId);
        this.name = Objects.requireNonNull(name);
        this.variant = Objects.requireNonNull(variant);
        this.price = Objects.requireNonNull(price);
        this.status = Objects.requireNonNull(status);
        this.type = Objects.requireNonNull(type);
    }

    public UUID getProductId() { return productId; }
    public String getName() { return name; }
    public ProductVariant getVariant() { return variant; }
    public Money getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public ProductType getType() { return type; }
}
