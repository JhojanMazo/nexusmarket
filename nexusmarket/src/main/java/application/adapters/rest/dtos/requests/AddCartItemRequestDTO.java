package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record AddCartItemRequestDTO(
        UUID productId,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {
}
