package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.ProductStatus;

public record ProductStatusUpdateRequestDTO(
        ProductStatus status
) {
}
