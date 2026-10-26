package application.domain.services.product;

import application.domain.enums.ProductStatus;
import application.domain.models.Product;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 4.1 Controls product catalog visibility state transitions.
 */
@Service
public class ProductStateControlService {

    public Product publishProduct(Product product) {
        Objects.requireNonNull(product, "Product must not be null");
        return product.withStatus(ProductStatus.PUBLISHED);
    }

    public Product suspendProduct(Product product) {
        Objects.requireNonNull(product, "Product must not be null");
        return product.withStatus(ProductStatus.SUSPENDED);
    }

    public Product discontinueProduct(Product product) {
        Objects.requireNonNull(product, "Product must not be null");
        return product.withStatus(ProductStatus.DISCONTINUED);
    }
}
