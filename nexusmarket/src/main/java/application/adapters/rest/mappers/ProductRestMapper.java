package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.domain.models.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductRestMapper {

    public ProductRestMapper() {
    }

    public static ProductResponseDTO toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponseDTO(
                product.getProductId(),
                product.getName(),
                product.getVariant() != null ? product.getVariant().attribute() : null,
                product.getVariant() != null ? product.getVariant().value() : null,
                product.getPrice().amount(),
                product.getPrice().currency().getCurrencyCode(),
                product.getStatus().name(),
                product.getType().name()
        );
    }

    public static List<ProductResponseDTO> toResponseList(List<Product> products) {
        if (products == null) {
            return Collections.emptyList();
        }
        return products.stream().map(ProductRestMapper::toResponse).toList();
    }
}
