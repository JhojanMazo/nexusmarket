package application.domain.services.product;

import application.domain.models.Product;
import application.domain.valueobjects.ProductVariant;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 3.5 Manages product SKUs/variants.
 */
@Service
public class ProductVariantManagementService {

    public Product updateVariant(Product product, ProductVariant newVariant) {
        Objects.requireNonNull(product, "Product must not be null");
        Objects.requireNonNull(newVariant, "Variant must not be null");
        return new Product(
                product.getProductId(),
                product.getName(),
                newVariant,
                product.getPrice(),
                product.getStatus(),
                product.getType()
        );
    }
}
