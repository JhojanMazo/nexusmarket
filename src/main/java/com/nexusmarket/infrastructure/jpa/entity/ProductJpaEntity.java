package com.nexusmarket.infrastructure.jpa.entity;

import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @Column(name = "product_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID productId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "variant_attribute", nullable = false, length = 100)
    private String variantAttribute;

    @Column(name = "variant_value", nullable = false, length = 100)
    private String variantValue;

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType type;

    public ProductJpaEntity() {
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVariantAttribute() { return variantAttribute; }
    public void setVariantAttribute(String value) { this.variantAttribute = value; }

    public String getVariantValue() { return variantValue; }
    public void setVariantValue(String value) { this.variantValue = value; }

    public BigDecimal getPriceAmount() { return priceAmount; }
    public void setPriceAmount(BigDecimal value) { this.priceAmount = value; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String value) { this.currencyCode = value; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }
}
