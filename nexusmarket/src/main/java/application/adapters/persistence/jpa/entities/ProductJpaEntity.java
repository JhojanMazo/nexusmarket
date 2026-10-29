package application.adapters.persistence.jpa.entities;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "seller_id")
    private UUID sellerId;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "variant_attribute", nullable = false, length = 100)
    private String variantAttribute;

    @Column(name = "variant_value", nullable = false, length = 100)
    private String variantValue;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_variants", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductVariantEmbeddable> variants = new ArrayList<>();

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

    public UUID getSellerId() { return sellerId; }
    public void setSellerId(UUID sellerId) { this.sellerId = sellerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVariantAttribute() { return variantAttribute; }
    public void setVariantAttribute(String value) { this.variantAttribute = value; }

    public String getVariantValue() { return variantValue; }
    public void setVariantValue(String value) { this.variantValue = value; }

    public List<ProductVariantEmbeddable> getVariants() { return variants; }
    public void setVariants(List<ProductVariantEmbeddable> variants) { this.variants = variants; }

    public BigDecimal getPriceAmount() { return priceAmount; }
    public void setPriceAmount(BigDecimal value) { this.priceAmount = value; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String value) { this.currencyCode = value; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
