package application.domain.services.product;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import java.util.UUID;

/**
 * 3.3 Governs creation and basic attributes of catalog products.
 */
public class ProductCatalogManagementService {

    public Product createProduct(String name, ProductVariant variant, Money price, ProductStatus status, ProductType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        return new Product(
                UUID.randomUUID(),
                name.trim(),
                variant,
                price,
                status,
                type
        );
    }
}
