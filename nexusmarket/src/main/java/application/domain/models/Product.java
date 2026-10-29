package application.domain.models;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
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
    private final UUID sellerId;
    private final String name;
    private final ProductVariant variant;
    private final Money price;
    private final ProductStatus status;
    private final ProductType type;

    public Product(UUID productId, UUID sellerId, String name, ProductVariant variant, Money price,
                   ProductStatus status, ProductType type) {
        this.productId = Objects.requireNonNull(productId);
        this.sellerId = sellerId;
        this.name = Objects.requireNonNull(name);
        this.variant = Objects.requireNonNull(variant);
        this.price = Objects.requireNonNull(price);
        this.status = Objects.requireNonNull(status);
        this.type = Objects.requireNonNull(type);
    }

    public Product(UUID productId, String name, ProductVariant variant, Money price,
                   ProductStatus status, ProductType type) {
        this(productId, null, name, variant, price, status, type);
    }

    public UUID getProductId() { return productId; }
    public UUID getSellerId() { return sellerId; }
    public String getName() { return name; }
    public ProductVariant getVariant() { return variant; }
    public Money getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public ProductType getType() { return type; }

    public boolean isPublished() {
        return this.status == ProductStatus.PUBLISHED;
    }

    public boolean requiresShipping() {
        return this.type == ProductType.PHYSICAL;
    }

    public Product withStatus(ProductStatus newStatus) {
        return new Product(this.productId, this.sellerId, this.name, this.variant, this.price, Objects.requireNonNull(newStatus), this.type);
    }

    public Product withPrice(Money newPrice) {
        return new Product(this.productId, this.sellerId, this.name, this.variant, Objects.requireNonNull(newPrice), this.status, this.type);
    }
}
