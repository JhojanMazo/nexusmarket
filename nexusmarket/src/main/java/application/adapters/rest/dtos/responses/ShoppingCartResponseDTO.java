package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShoppingCartResponseDTO(
        UUID cartId,
        UUID buyerId,
        String buyerName,
        List<CartItemResponseDTO> items,
        BigDecimal subtotal,
        String currency
) {
}
