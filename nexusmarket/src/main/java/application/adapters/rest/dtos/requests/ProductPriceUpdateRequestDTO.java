package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.math.BigDecimal;

public record ProductPriceUpdateRequestDTO(
        BigDecimal price,
        String currency
) {
}
