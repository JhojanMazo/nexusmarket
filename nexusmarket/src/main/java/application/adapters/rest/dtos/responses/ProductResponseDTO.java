package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.models.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String variantAttribute,
        String variantValue,
        BigDecimal price,
        String currency,
        String status,
        String type
) {
    public static ProductResponseDTO from(Product product) {
        return new ProductResponseDTO(
                product.getProductId(),
                product.getName(),
                product.getVariant().attribute(),
                product.getVariant().value(),
                product.getPrice().amount(),
                product.getPrice().currency().getCurrencyCode(),
                product.getStatus().name(),
                product.getType().name()
        );
    }
}
