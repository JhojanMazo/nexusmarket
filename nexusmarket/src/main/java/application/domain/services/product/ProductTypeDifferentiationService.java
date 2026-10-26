package application.domain.services.product;

import application.domain.enums.ProductType;
import application.domain.models.Product;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 3.4 Customizes workflow handling based on product nature.
 */
@Service
public class ProductTypeDifferentiationService {

    public boolean requiresShipping(Product product) {
        Objects.requireNonNull(product, "Product must not be null");
        return product.getType() == ProductType.PHYSICAL;
    }

    public boolean isDigital(Product product) {
        Objects.requireNonNull(product, "Product must not be null");
        return product.getType() == ProductType.DIGITAL;
    }
}
