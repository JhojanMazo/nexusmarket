package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.ProductVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Physical or digital good offered by a Seller within the catalog.
 * Physical products require inventory and shipping; digital products are
 * delivered immediately after payment.
 */
public class Product {

    private final String productId;
    private String name;
    private final ProductType type;
    private final List<ProductVariant> variants = new ArrayList<>();
    private ProductStatus status;
    private Money price;
    private final Seller seller;

    public Product(String productId, String name, ProductType type,
                    ProductStatus status, Money price, Seller seller) {
        this.productId = requireNotBlank(productId, "productId");
        this.name = requireNotBlank(name, "name");
        this.type = Objects.requireNonNull(type, "type is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.price = Objects.requireNonNull(price, "price is required");
        this.seller = Objects.requireNonNull(seller, "seller is required");
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public boolean isPhysical() { return type == ProductType.PHYSICAL; }
    public boolean isPublished() { return status == ProductStatus.PUBLISHED; }

    public void addVariant(ProductVariant variant) {
        variants.add(Objects.requireNonNull(variant, "variant is required"));
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requireNotBlank(name, "name"); }
    public ProductType getType() { return type; }
    public List<ProductVariant> getVariants() { return List.copyOf(variants); }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
    }
    public Money getPrice() { return price; }
    public void setPrice(Money price) { this.price = Objects.requireNonNull(price, "price is required"); }
    public Seller getSeller() { return seller; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        return productId.equals(((Product) o).productId);
    }

    @Override
    public int hashCode() { return Objects.hash(productId); }
}
