package application.domain.services.product;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.services.product.ProductStateControlService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductStateControlServiceTest {

    private ProductStateControlService service;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        service = new ProductStateControlService();
        sampleProduct = new Product(
                UUID.randomUUID(),
                "Ultra Gaming Laptop",
                new ProductVariant("Color", "Graphite Black"),
                new Money(new BigDecimal("3500.00"), Currency.getInstance("USD")),
                ProductStatus.SUSPENDED,
                ProductType.PHYSICAL
        );
    }

    @Test
    void shouldPublishProduct() {
        Product published = service.publishProduct(sampleProduct);
        assertEquals(ProductStatus.PUBLISHED, published.getStatus());
        assertTrue(published.isPublished());
    }

    @Test
    void shouldSuspendProduct() {
        Product suspended = service.suspendProduct(sampleProduct.withStatus(ProductStatus.PUBLISHED));
        assertEquals(ProductStatus.SUSPENDED, suspended.getStatus());
        assertFalse(suspended.isPublished());
    }

    @Test
    void shouldDiscontinueProduct() {
        Product discontinued = service.discontinueProduct(sampleProduct);
        assertEquals(ProductStatus.DISCONTINUED, discontinued.getStatus());
    }
}
