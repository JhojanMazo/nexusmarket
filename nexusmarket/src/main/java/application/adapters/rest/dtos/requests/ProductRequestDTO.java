package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import java.math.BigDecimal;

public record ProductRequestDTO(
        String name,
        String variantAttribute,
        String variantValue,
        BigDecimal price,
        String currency,
        ProductStatus status,
        ProductType type
) {
}
